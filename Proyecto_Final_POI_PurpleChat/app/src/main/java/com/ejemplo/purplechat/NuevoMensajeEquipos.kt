package com.ejemplo.purplechat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.layout_usuario_new_mensaje_equipos.view.*
import kotlinx.android.synthetic.main.layout_usuario_new_mensaje_group.view.*
import java.util.*
import kotlin.collections.ArrayList

class NuevoMensajeEquipos : AppCompatActivity() {

    var imagenCreada = false

    companion object {
        var selectedUserList: ArrayList<Registrarse.User>? = ArrayList()
        var seleccionaUsuariolistaDinamicaEquipo: MutableList<String> = mutableListOf()
        val USER_KEYE = "USER_KEYE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_mensaje_equipos)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_NewMenEquipo_FlechaRegresar)
        val editText_NombreEquipo = findViewById<EditText>(R.id.editText_NewMenEquipo_NombreEquipo)
        val botonCrearEquipo=findViewById<Button>(R.id.button_NewMenEquipo_CrearEquipo)

        val recyclerViewUltimosListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_NewMenEquipo_ListaUsuarios)
        recyclerViewUltimosListaUsuarios.layoutManager = LinearLayoutManager(this)

        val boton_SeleccionFoto= findViewById<ImageButton>(R.id.imageButton_NewMenEquipo_SeleccionaImagen)


        val groupName = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,Equipos::class.java)
            intent.putExtra(NuevoMensajeGrupos.USER_KEYG, groupName)
            startActivity(intent)
            finish()
        }

        boton_SeleccionFoto.setOnClickListener{

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }

        botonCrearEquipo.setOnClickListener {

            val nombreEquipo = editText_NombreEquipo.text.toString()

            if (nombreEquipo.isEmpty() || imagenCreada == false) {

                Toast.makeText(this, "Favor de llenar los campos necesarios", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Log.d("NuevoMensajeListaTamaño","Size: ${seleccionaUsuariolistaDinamicaEquipo.size}")

            if (seleccionaUsuariolistaDinamicaEquipo.size < 2 && seleccionaUsuariolistaDinamicaEquipo.size <= 6) {
                Toast.makeText(this, "Favor de seleccionar al menos 1 miembro más y máximo 7", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            subirImagenStoragePerfilEquipo()


            val uidUsuarioLog = FirebaseAuth.getInstance().uid
            val referenceRecompensa = FirebaseDatabase.getInstance().getReference("/recompensas/$uidUsuarioLog").push()
            val crearrecompensa = Registrarse.Recompensa(
                referenceRecompensa.key!!,
                uidUsuarioLog.toString(),
                "Logro: Nuevo Equipo Creado",
                System.currentTimeMillis() / 1000
            )
            referenceRecompensa.setValue(crearrecompensa)
            Toast.makeText( baseContext,"Logro Desbloqueado: Nuevo Equipo Creado", Toast.LENGTH_SHORT).show()

            val intent = Intent(this,Equipos::class.java)
            intent.putExtra(NuevoMensajeGrupos.USER_KEYG, groupName)
            startActivity(intent)
            finish()

        }

        eliminarListaUsuariosEquipo()
        buscaUsuariosDataBaseEquipo()

    }


    private fun eliminarListaUsuariosEquipo() {
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid


        var i = seleccionaUsuariolistaDinamicaEquipo.size
        var tamañoArregloMiembros = seleccionaUsuariolistaDinamicaEquipo.size

        Log.d("PorqueMarcaError", "Tamaño ${tamañoArregloMiembros}")


        if (tamañoArregloMiembros > 0){

            Log.d("PorqueMarcaError", "Entra aqui ${tamañoArregloMiembros}")
            Log.d("PorqueMarcaError", "Valor de i: ${i}")


            seleccionaUsuariolistaDinamicaEquipo.clear()

            Log.d("PorqueMarcaError", "Despues de eliminar Size: ${seleccionaUsuariolistaDinamicaEquipo.size}")
            // do{
            //      i -= 1
            //  Log.d("PorqueMarcaError", "Valor de i en el while: ${i}")
            //      Log.d("PorqueMarcaError", "Elimino ${seleccionaUsuariolistaDinamica[i]}")
            //     seleccionaUsuariolistaDinamica.remove(seleccionaUsuariolistaDinamica[i])
            //  } while (i < 0)
        }


    }

    private fun buscaUsuariosDataBaseEquipo(){

        val recyclerViewUltimosListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_NewMenEquipo_ListaUsuarios)
        val checkBoxIntegranteGrupo=findViewById<CheckBox>(R.id.checkBox_layoutNewMenEquipo_IntegranteEquipo)

        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid
        val groupNameID = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        val toId = groupNameID!!.uid
        val adapter = GroupAdapter<ViewHolder>()


        val reference= FirebaseDatabase.getInstance().getReference("/group/$toId/miembros")

        reference.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){


                p0.children.forEach{
                    val miembroGrupo = it.getValue(GroupMiembros::class.java)

                    if (miembroGrupo != null) {


                        if (miembroGrupo.uidGroup == toId){


                            var uidUsuario = miembroGrupo.uid
                            val ref= FirebaseDatabase.getInstance().getReference("/users/$uidUsuario")

                            ref.addListenerForSingleValueEvent(object: ValueEventListener {

                                override fun onDataChange(p0: DataSnapshot){

                                        val user = p0.getValue(Registrarse.User::class.java)
                                        if (user != null) {
                                            if(uidUsuario == user.uid) {

                                                if (user.uid != refUsuarioLoggeado) {
                                                    adapter.add(UserItemEquipo(user))
                                                }
                                                else {
                                                    seleccionaUsuariolistaDinamicaEquipo.add(user.uid)
                                                }
                                            }
                                        }



                                        adapter.setOnItemClickListener { item, view ->

                                            Log.d("NuevoMensajeGruposCheck","Entro aqui")

                                            val userItem = item as UserItemEquipo

                                            onCheckboxClicked(view.checkBox_layoutNewMenEquipo_IntegranteEquipo, view.textView_LayUsNewMenEquipo_ID)

                                            // val userItem = item as UserItem
                                            //val intent = Intent(view.context,ChatMensaje::class.java)
                                            //intent.putExtra(USER_KEY, userItem.user?.username)
                                            //intent.putExtra(NuevoMensaje.USER_KEY, userItem.user)
                                            //startActivity(intent)
                                            //finish()
                                        }

                                        recyclerViewUltimosListaUsuarios.adapter= adapter
                                    }

                                override fun onCancelled(p0: DatabaseError){}
                            })
                        }

                    }
                }



            }

            override fun onCancelled(p0: DatabaseError){}

        })

    }

    private fun guardadoEquipoDataBase(profileImageUrl: String){

        val editText_NombreEquipo = findViewById<EditText>(R.id.editText_NewMenEquipo_NombreEquipo).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid
        val group = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        val fromIdGroup = group?.uid

        val reference = FirebaseDatabase.getInstance().getReference("/equipo").push()
        val crearEquipo= Equipo(reference.key!!,fromIdGroup.toString(),editText_NombreEquipo.toString(),profileImageUrl,System.currentTimeMillis() / 1000)


        reference.setValue(crearEquipo)
            .addOnSuccessListener {
                Log.d("EquipoGuardadoConExito", "Equipo guardado: ${reference.key}")
            }

        Log.d("OtroErrorCargaMiembro", "Entra aqui ${seleccionaUsuariolistaDinamicaEquipo.size}")

        for (indice in seleccionaUsuariolistaDinamicaEquipo.indices){

            val referenceMiembrosEquipo = FirebaseDatabase.getInstance().getReference("/equipo/${reference.key}/miembros/${seleccionaUsuariolistaDinamicaEquipo[indice]}")

            if(refUsuarioLoggeado != seleccionaUsuariolistaDinamicaEquipo[indice]) {
                val miembrosEquipo = EquipoMiembros(seleccionaUsuariolistaDinamicaEquipo[indice],reference.key!!,fromIdGroup.toString(),"Usuarios",System.currentTimeMillis() / 1000)
                referenceMiembrosEquipo.setValue(miembrosEquipo)
                    .addOnSuccessListener {
                        Log.d(ChatMensaje.TAG, "Equipo guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamicaEquipo[indice]}")
                    }

            }
            else{
                val miembrosEquipo = EquipoMiembros(seleccionaUsuariolistaDinamicaEquipo[indice],reference.key!!,fromIdGroup.toString(),"Administrador",System.currentTimeMillis() / 1000)
                referenceMiembrosEquipo.setValue(miembrosEquipo)
                    .addOnSuccessListener {
                        Log.d(ChatMensaje.TAG, "Equipo guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamicaEquipo[indice]}")
                    }
            }

            val refMiembrosEquipo = FirebaseDatabase.getInstance().getReference("/equipo-miembros/${seleccionaUsuariolistaDinamicaEquipo[indice]}/${reference.key}")

            if(refUsuarioLoggeado != seleccionaUsuariolistaDinamicaEquipo[indice]) {
                val miembrosEquipo = EquipoMiembros(seleccionaUsuariolistaDinamicaEquipo[indice],reference.key!!,fromIdGroup.toString(),"Usuarios",System.currentTimeMillis() / 1000)
                refMiembrosEquipo.setValue(miembrosEquipo)
                    .addOnSuccessListener {
                        Log.d(ChatMensaje.TAG, "Miembro-Equipo guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamicaEquipo[indice]}")
                    }

            }
            else{
                val miembrosEquipo = EquipoMiembros(seleccionaUsuariolistaDinamicaEquipo[indice],reference.key!!,fromIdGroup.toString(),"Administrador",System.currentTimeMillis() / 1000)
                refMiembrosEquipo.setValue(miembrosEquipo)
                    .addOnSuccessListener {
                        Log.d(ChatMensaje.TAG, "Miembro-guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamicaEquipo[indice]}")
                    }
            }


            Log.d("NuevoMensajeEquipoCheck","En el índice ${indice} tenemos a ${seleccionaUsuariolistaDinamicaEquipo[indice]}")


        }


        //Toast.makeText( baseContext,"Entra aqui", Toast.LENGTH_SHORT).show()

        val fromId= reference.key
        val adminId = FirebaseAuth.getInstance().uid
        val refEncriptado = FirebaseDatabase.getInstance().getReference("/encriptado/mensajeequipo/$fromId")
        val mensajeEncriptadoEquipo= EquipoMensaje.EquipoMessageEncriptacion("algo",refEncriptado.key!!,fromId.toString(),adminId.toString(),System.currentTimeMillis() / 1000)
        refEncriptado.setValue(mensajeEncriptadoEquipo)


    }

    private fun subirImagenStoragePerfilEquipo(){

        if(selectedPhotoUri == null){
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener{
            Log.d("NuevoMensajeGrupo","Successfully uploaded image:${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener{
                Log.d("NuevoMensajeGrupo","File Location: $it")
                guardadoEquipoDataBase(it.toString())
            }
        }

    }

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val boton_SeleccionPhoto = findViewById<ImageButton>(R.id.imageButton_NewMenEquipo_SeleccionaImagen)
        val circle_View = findViewById<CircleImageView>(R.id.circle_NewMenEquipo_PhotoView)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null)
        {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circle_View.setImageBitmap(bitmap)
            boton_SeleccionPhoto.alpha = 0f

            imagenCreada = true

            //val bitmapDrawable = BitmapDrawable(bitmap)
            //boton_SeleccionPhoto.setBackgroundDrawable(bitmapDrawable)
        }
    }

    class UserItemEquipo(val user: Registrarse.User?): Item<ViewHolder>(){

        override fun bind(viewHolder: ViewHolder, position: Int){

            if (user != null) {
                viewHolder.itemView.findViewById<TextView>(R.id.textView_LayUsNewMenEquipo_ID).text = user.uid
                viewHolder.itemView.findViewById<TextView>(R.id.textView_LayUsNewMenEquipo_Usuario).text = user.username
                Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsNewMenEquipo_Imagen))
            }

        }

        override fun getLayout(): Int{
            return R.layout.layout_usuario_new_mensaje_equipos
        }

    }

    fun onCheckboxClicked(view: View, view02: View) {

        Log.d("NuevoMensajeEquipoCheck", "Entro aqui onCheckboxClicked")

        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.checkBox_layoutNewMenEquipo_IntegranteEquipo -> {
                    if (checked) {
                        Log.d("NuevoMensajeEquipoCheck", "Se deselecciono usuario")
                        view.checkBox_layoutNewMenEquipo_IntegranteEquipo.isChecked = false
                        //view.checkBox_layoutNewMenEquipo_IntegranteEquipo.alpha = 0f
                        seleccionaUsuariolistaDinamicaEquipo.remove(view02.textView_LayUsNewMenEquipo_ID.text.toString())

                        for (indice in 0 until seleccionaUsuariolistaDinamicaEquipo.size){
                            Log.d("NuevoMensajeGruposCheck","En el índice ${indice} tenemos a ${seleccionaUsuariolistaDinamicaEquipo[indice]}")
                        }

                    } else {
                        Log.d("NuevoMensajeGruposCheck", "Se selecciono usuario")
                        view.checkBox_layoutNewMenEquipo_IntegranteEquipo.isChecked = true
                        //view.checkBox_layoutNewMenEquipo_IntegranteEquipo.alpha = 1f
                        seleccionaUsuariolistaDinamicaEquipo.add(view02.textView_LayUsNewMenEquipo_ID.text.toString())

                        for (indice in 0 until seleccionaUsuariolistaDinamicaEquipo.size){
                            Log.d("NuevoMensajeEquipoCheck","En el índice ${indice} tenemos a ${seleccionaUsuariolistaDinamicaEquipo[indice]}")
                        }
                    }
                }
            }
        }
    }

}

@Parcelize
class Equipo(val uid: String, val uidGroup: String, val equiponame: String, val profileImageUrl: String, val timestamp: Long):
    Parcelable {
    constructor(): this("","","","",-1)
}

@Parcelize
class EquipoMiembros(val uid: String, val uidEquipo: String, val uidGroup: String, val role: String, val timestamp: Long):
    Parcelable {
    constructor(): this("","","","",-1)
}