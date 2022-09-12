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
import kotlinx.android.parcel.Parcelize
import java.util.*

class MuroArchivo : AppCompatActivity() {

    var archivoCreado = false
    val PDF: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muro_archivo)

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

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_MuroArchivo_FlechaRegresar)
        val boton_SeleccionArchivo= findViewById<ImageButton>(R.id.imageButton_MuroArchivo_Archivo)
        val botonCrearArchivo = findViewById<Button>(R.id.button_MuroArchivo_CrearPublicacion)
        val editText_texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroArchivo_Publicacion)
        val editText_NombreArchivo = findViewById<EditText>(R.id.editText_MuroArchivo_NombreArchivo)



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

        boton_SeleccionArchivo.setOnClickListener{

            val intent = Intent()
            intent.setType("application/pdf")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent,"Selecciona PDF"),PDF)

        }

        botonCrearArchivo.setOnClickListener {

            if (chatName != null) {

                val NombreArchivo = editText_NombreArchivo.text.toString()

                if (NombreArchivo.isEmpty() || archivoCreado == false) {

                    Toast.makeText(
                        this,
                        "Favor de llenar los campos de nombre de archivo",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                subirArchivoStorageMuroArchivo("chat")

                val intent = Intent(this, ChatMuro::class.java)
                intent.putExtra(USER_KEY, chatName)
                startActivity(intent)
                finish()
            }

            if (groupName != null) {

                val NombreArchivo = editText_NombreArchivo.text.toString()

                if (NombreArchivo.isEmpty() || archivoCreado == false) {

                    Toast.makeText(
                        this,
                        "Favor de llenar los campos de nombre de archivo",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                subirArchivoStorageMuroArchivo("grupo")

                val intent = Intent(this, GrupoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                startActivity(intent)
                finish()
            }

            if (groupName != null && equipoName != null) {

                val NombreArchivo = editText_NombreArchivo.text.toString()

                if (NombreArchivo.isEmpty() || archivoCreado == false) {

                    Toast.makeText(
                        this,
                        "Favor de llenar los campos de nombre de archivo",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                subirArchivoStorageMuroArchivo("equipo")

                val intent = Intent(this,EquipoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                intent.putExtra(USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }

    }

    lateinit var selectedArchivoUri : Uri

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val boton_SeleccionArchivo = findViewById<ImageButton>(R.id.imageButton_MuroArchivo_Archivo)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null )
        {
            selectedArchivoUri = data.data!!

            boton_SeleccionArchivo.setBackgroundResource(R.drawable.rounded_circle_selectphoto)
            archivoCreado = true
            Toast.makeText( baseContext,"Se eligio un archivo pdf", Toast.LENGTH_SHORT).show()

        }
    }

    private fun subirArchivoStorageMuroArchivo(tipoMuro: String){

        if(selectedArchivoUri == null){
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/archivo/$filename")

        ref.putFile(selectedArchivoUri!!).addOnSuccessListener{
            Log.d("Registrarse","Successfully uploaded archivo:${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener{
                Log.d("Registrarse","File Location: $it")
                if (tipoMuro == "chat") {
                    guardadoArchivoChatDataBase(it.toString())
                }
                if (tipoMuro == "grupo") {
                    guardadoArchivoGrupoDataBase(it.toString())
                }
                if (tipoMuro == "equipo") {
                    guardadoArchivoEquipoDataBase(it.toString())
                }
            }
        }

    }

    private fun guardadoArchivoChatDataBase(ArchivoUrl: String) {

        val editText_Texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroArchivo_Publicacion).text
        val editText_NombreArchivo = findViewById<EditText>(R.id.editText_MuroArchivo_NombreArchivo).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
        val toId = chatName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroarchivo/chat/$refUsuarioLoggeado/$toId").push()
        val creararchivo = ArchivoClassMuro(reference.key!!, editText_Texto.toString(),ArchivoUrl,editText_NombreArchivo.toString(),refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(creararchivo)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroarchivo/chat/$toId/$refUsuarioLoggeado").push()
        val creararchivoToId = ArchivoClassMuro(reference.key!!, editText_Texto.toString(),ArchivoUrl,editText_NombreArchivo.toString(),refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(creararchivoToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroimagen = ChatMuro.MuroChat(ref.key!!,reference.key!!,"archivo", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        ref.setValue(crearMuroimagen)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$toId/$refUsuarioLoggeado")
                .push()
        val crearMuroarchivoToId = ChatMuro.MuroChat(ref.key!!,reference.key!!,"archivo", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        referencia.setValue(crearMuroarchivoToId)

    }

    private fun guardadoArchivoGrupoDataBase(ArchivoUrl: String) {

        val editText_Texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroArchivo_Publicacion).text
        val editText_NombreArchivo = findViewById<EditText>(R.id.editText_MuroArchivo_NombreArchivo).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        val toId = groupName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroarchivo/grupo/$refUsuarioLoggeado/$toId").push()
        val creararchivo = ArchivoClassMuro(reference.key!!, editText_Texto.toString(),ArchivoUrl,editText_NombreArchivo.toString(),refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(creararchivo)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroarchivo/grupo/$toId").push()
        val creararchivoToId = ArchivoClassMuro(reference.key!!, editText_Texto.toString(),ArchivoUrl,editText_NombreArchivo.toString(),refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(creararchivoToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroimagen = GrupoMuro.MuroGrupo(ref.key!!,reference.key!!,"archivo", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        ref.setValue(crearMuroimagen)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$toId")
                .push()
        val crearMuroarchivoToId = GrupoMuro.MuroGrupo(ref.key!!,reference.key!!,"archivo", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        referencia.setValue(crearMuroarchivoToId)

    }

    private fun guardadoArchivoEquipoDataBase(ArchivoUrl: String) {

        val editText_Texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroArchivo_Publicacion).text
        val editText_NombreArchivo = findViewById<EditText>(R.id.editText_MuroArchivo_NombreArchivo).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        val toId = equipoName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroarchivo/equipo/$refUsuarioLoggeado/$toId").push()
        val creararchivo = ArchivoClassMuro(reference.key!!, editText_Texto.toString(),ArchivoUrl,editText_NombreArchivo.toString(),refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(creararchivo)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroarchivo/equipo/$toId").push()
        val creararchivoToId = ArchivoClassMuro(reference.key!!, editText_Texto.toString(),ArchivoUrl,editText_NombreArchivo.toString(),refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(creararchivoToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroimagen = EquipoMuro.MuroEquipo(ref.key!!,reference.key!!,"archivo", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        ref.setValue(crearMuroimagen)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$toId")
                .push()
        val crearMuroarchivoToId = EquipoMuro.MuroEquipo(ref.key!!,reference.key!!,"archivo", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        referencia.setValue(crearMuroarchivoToId)

    }

    @Parcelize
    class ArchivoClassMuro(val id: String,val text: String,val archivoUrl: String,val nombreArchivo: String,val fromId: String,val toId: String,val timestamp: Long) :
        Parcelable {
        constructor() : this("","", "","", "", "", -1)
    }
}