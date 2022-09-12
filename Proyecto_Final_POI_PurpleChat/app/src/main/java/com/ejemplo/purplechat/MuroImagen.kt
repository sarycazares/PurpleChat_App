package com.ejemplo.purplechat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.ejemplo.purplechat.NuevoMensaje.Companion.USER_KEY
import com.ejemplo.purplechat.NuevoMensajeEquipos.Companion.USER_KEYE
import com.ejemplo.purplechat.NuevoMensajeGrupos.Companion.USER_KEYG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import java.util.*

class MuroImagen : AppCompatActivity() {

    var imagenCreada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muro_imagen)

        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
        if (chatName != null) {
            Log.d("gruporecibimiento","$chatName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        if (equipoName != null) {
            Log.d("gruporecibimiento","$equipoName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_MuroImagen_FlechaRegresar)
        val boton_SeleccionFoto= findViewById<ImageButton>(R.id.imageButton_MuroImagen_SubirImagen)
        val botonCrearPublicacion = findViewById<Button>(R.id.button_MuroImagen_CrearPublicacion)
        val editText_texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroImagen_Publicacion)


        boton_SeleccionFoto.setOnClickListener{

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }

        imageButtonFlechaRegresar.setOnClickListener{

            if (chatName != null) {
                val intent = Intent(this, ChatMuro::class.java)
                intent.putExtra(USER_KEY, chatName)
                startActivity(intent)
                finish()
            }

            if (groupName != null) {
                val intent = Intent(this, GrupoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                startActivity(intent)
                finish()
            }

            if(groupName != null && equipoName != null){
                val intent = Intent(this,EquipoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                intent.putExtra(USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }

        botonCrearPublicacion.setOnClickListener {

            if (chatName != null) {

                val textoPublicacion = editText_texto.text.toString()

                if ( imagenCreada == false) {

                    Toast.makeText(
                        this,
                        "Favor de agregar imagen",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                subirImagenStorageMuroImagen("chat")

                val intent = Intent(this, ChatMuro::class.java)
                intent.putExtra(USER_KEY, chatName)
                startActivity(intent)
                finish()
            }

            if (groupName != null) {

                val textoPublicacion = editText_texto.text.toString()

                if ( imagenCreada == false) {

                    Toast.makeText(
                        this,
                        "Favor de agregar imagen",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                subirImagenStorageMuroImagen("grupo")

                val intent = Intent(this, GrupoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                startActivity(intent)
                finish()
            }

            if (groupName != null && equipoName != null) {

                val textoPublicacion = editText_texto.text.toString()

                if ( imagenCreada == false) {

                    Toast.makeText(
                        this,
                        "Favor de agregar imagen",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                subirImagenStorageMuroImagen("equipo")

                val intent = Intent(this,EquipoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                intent.putExtra(USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }

    }

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val boton_SeleccionPhoto = findViewById<ImageButton>(R.id.imageButton_MuroImagen_SubirImagen)
        val ImagenView = findViewById<ImageView>(R.id.imageView_MuroImagen_Imagen)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null)
        {
            Log.d("Registrarse","Se selecciono foto")
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            ImagenView.setImageBitmap(bitmap)
            boton_SeleccionPhoto.alpha = 0f
            imagenCreada = true

            //val bitmapDrawable = BitmapDrawable(bitmap)
            //boton_SeleccionPhoto.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun subirImagenStorageMuroImagen(tipoMuro: String){

        if(selectedPhotoUri == null){
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener{
            Log.d("Registrarse","Successfully uploaded image:${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener{
                Log.d("Registrarse","File Location: $it")
                if (tipoMuro == "chat") {
                    guardadoImagenChatDataBase(it.toString())
                }
                if (tipoMuro == "grupo") {
                    guardadoImagenGrupoDataBase(it.toString())
                }
                if (tipoMuro == "equipo") {
                    guardadoImagenEquipoDataBase(it.toString())
                }
            }
        }

    }

    private fun guardadoImagenChatDataBase(ImageUrl: String) {

        val editText_Texto =
            findViewById<EditText>(R.id.editTextTextMultiLine_MuroImagen_Publicacion).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
        val toId = chatName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroimagen/chat/$refUsuarioLoggeado/$toId").push()
        val crearimagen = ImagenClassMuro(reference.key!!,editText_Texto.toString(),ImageUrl,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(crearimagen)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroimagen/chat/$toId/$refUsuarioLoggeado").push()
        val crearimagenToId = ImagenClassMuro(reference.key!!,editText_Texto.toString(),ImageUrl,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(crearimagenToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroimagen = ChatMuro.MuroChat(
            ref.key!!,
            reference.key!!,
            "imagen",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        ref.setValue(crearMuroimagen)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$toId/$refUsuarioLoggeado")
                .push()
        val crearMuroimagenToId = ChatMuro.MuroChat(
            ref.key!!,
            reference.key!!,
            "imagen",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        referencia.setValue(crearMuroimagenToId)

    }

    private fun guardadoImagenGrupoDataBase(ImageUrl: String) {

        val editText_Texto =
            findViewById<EditText>(R.id.editTextTextMultiLine_MuroImagen_Publicacion).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        val toId = groupName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroimagen/grupo/$refUsuarioLoggeado/$toId").push()
        val crearimagen = ImagenClassMuro(reference.key!!,editText_Texto.toString(),ImageUrl,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(crearimagen)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroimagen/grupo/$toId").push()
        val crearimagenToId = ImagenClassMuro(reference.key!!,editText_Texto.toString(),ImageUrl,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(crearimagenToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroimagen = GrupoMuro.MuroGrupo(
            ref.key!!,
            reference.key!!,
            "imagen",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        ref.setValue(crearMuroimagen)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$toId")
                .push()
        val crearMuroimagenToId = GrupoMuro.MuroGrupo(
            ref.key!!,
            reference.key!!,
            "imagen",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        referencia.setValue(crearMuroimagenToId)

    }

    private fun guardadoImagenEquipoDataBase(ImageUrl: String) {

        val editText_Texto =
            findViewById<EditText>(R.id.editTextTextMultiLine_MuroImagen_Publicacion).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        val toId = equipoName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroimagen/equipo/$refUsuarioLoggeado/$toId").push()
        val crearimagen = ImagenClassMuro(reference.key!!,editText_Texto.toString(),ImageUrl,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(crearimagen)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroimagen/equipo/$toId").push()
        val crearimagenToId = ImagenClassMuro(reference.key!!,editText_Texto.toString(),ImageUrl,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(crearimagenToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroimagen = EquipoMuro.MuroEquipo(
            ref.key!!,
            reference.key!!,
            "imagen",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        ref.setValue(crearMuroimagen)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$toId")
                .push()
        val crearMuroimagenToId = EquipoMuro.MuroEquipo(
            ref.key!!,
            reference.key!!,
            "imagen",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        referencia.setValue(crearMuroimagenToId)

    }


    @Parcelize
    class ImagenClassMuro(val id: String,val text: String,val imagenUrl: String,val fromId: String,val toId: String,val timestamp: Long) :
        Parcelable {
        constructor() : this("","", "", "", "", -1)
    }


}