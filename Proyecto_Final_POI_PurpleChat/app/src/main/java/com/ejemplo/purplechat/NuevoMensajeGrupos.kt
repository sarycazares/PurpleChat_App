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
import com.google.android.gms.common.util.ArrayUtils.removeAll
import com.google.common.collect.Iterables.removeAll
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.layout_usuario_new_mensaje_group.view.*
import java.util.*
import kotlin.collections.ArrayList


class NuevoMensajeGrupos : AppCompatActivity() {

    var imagenCreada = false

    companion object {
        var selectedUserList: ArrayList<Registrarse.User>? = ArrayList()
        var seleccionaUsuariolistaDinamica: MutableList<String> = mutableListOf()
        val USER_KEYG = "USER_KEYG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_mensaje_grupos)


        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_NewMenGroup_FlechaRegresar)
        val editText_NombreGrupo = findViewById<EditText>(R.id.editText_NewMenGroup_NombreGrupo)
        val botonCrearGrupo=findViewById<Button>(R.id.button_NewMenGroup_CrearGrupo)

        val recyclerViewUltimosListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_NewMenGroup_ListaUsuarios)
        recyclerViewUltimosListaUsuarios.layoutManager = LinearLayoutManager(this)

        val boton_SeleccionFoto= findViewById<ImageButton>(R.id.imageButton_NewMenGroup_SeleccionaImagen)

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,Grupos::class.java)
            startActivity(intent)
            finish()
        }

        boton_SeleccionFoto.setOnClickListener{

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }

        botonCrearGrupo.setOnClickListener {

            val nombreGrupo = editText_NombreGrupo.text.toString()

            if (nombreGrupo.isEmpty() || imagenCreada == false) {

                Toast.makeText(this, "Favor de llenar los campos necesarios", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Log.d("NuevoMensajeListaTamaño","Size: ${seleccionaUsuariolistaDinamica.size}")

            if (seleccionaUsuariolistaDinamica.size < 2 && seleccionaUsuariolistaDinamica.size <= 6) {
                Toast.makeText(this, "Favor de seleccionar al menos 1 miembro más y máximo 7", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            subirImagenStoragePerfilGrupo()

            val uidUsuarioLog = FirebaseAuth.getInstance().uid
            val referenceRecompensa = FirebaseDatabase.getInstance().getReference("/recompensas/$uidUsuarioLog").push()
            val crearrecompensa = Registrarse.Recompensa(
                referenceRecompensa.key!!,
                uidUsuarioLog.toString(),
                "Logro: Nuevo Grupo Creado",
                System.currentTimeMillis() / 1000
            )
            referenceRecompensa.setValue(crearrecompensa)
            Toast.makeText( baseContext,"Logro Desbloqueado: Nuevo Grupo Creado", Toast.LENGTH_SHORT).show()


            val intent = Intent(this,Grupos::class.java)
            startActivity(intent)
            finish()

        }

         //val adapter = GroupAdapter<ViewHolder>()

        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())


       // recyclerViewUltimosListaUsuarios.adapter = adapter

        eliminarListaUsuariosGrupo()
        buscaUsuariosDataBase()

    }


    private fun eliminarListaUsuariosGrupo() {
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid


        var i = seleccionaUsuariolistaDinamica.size
        var tamañoArregloMiembros = seleccionaUsuariolistaDinamica.size

        Log.d("PorqueMarcaError", "Tamaño ${tamañoArregloMiembros}")


        if (tamañoArregloMiembros > 0){

            Log.d("PorqueMarcaError", "Entra aqui ${tamañoArregloMiembros}")
            Log.d("PorqueMarcaError", "Valor de i: ${i}")


            seleccionaUsuariolistaDinamica.clear()

            Log.d("PorqueMarcaError", "Despues de eliminar Size: ${seleccionaUsuariolistaDinamica.size}")
          // do{
          //      i -= 1
          //  Log.d("PorqueMarcaError", "Valor de i en el while: ${i}")
          //      Log.d("PorqueMarcaError", "Elimino ${seleccionaUsuariolistaDinamica[i]}")
           //     seleccionaUsuariolistaDinamica.remove(seleccionaUsuariolistaDinamica[i])
          //  } while (i < 0)
        }


    }

    private fun buscaUsuariosDataBase(){

        val recyclerViewUltimosListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_NewMenGroup_ListaUsuarios)
        val checkBoxIntegranteGrupo=findViewById<CheckBox>(R.id.checkBox_layoutNewMenGroup_IntegranteGrupo)

        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){

                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach{
                    Log.d("NuevoMensajeGroup", it.toString())
                    val user = it.getValue(Registrarse.User::class.java)
                        if (user != null) {
                            if(user.uid != refUsuarioLoggeado) {
                                adapter.add(UserItem(user))
                            } else{
                                seleccionaUsuariolistaDinamica.add(user.uid)
                            }
                        }



                    adapter.setOnItemClickListener { item, view ->

                        Log.d("NuevoMensajeGruposCheck","Entro aqui")

                        val userItem = item as UserItem

                        onCheckboxClicked(view.checkBox_layoutNewMenGroup_IntegranteGrupo, view.textView_LayUsNewMenGroup_ID)

                        // val userItem = item as UserItem
                        //val intent = Intent(view.context,ChatMensaje::class.java)
                        //intent.putExtra(USER_KEY, userItem.user?.username)
                        //intent.putExtra(NuevoMensaje.USER_KEY, userItem.user)
                        //startActivity(intent)
                        //finish()
                    }

                    recyclerViewUltimosListaUsuarios.adapter= adapter
                }
            }

            override fun onCancelled(p0: DatabaseError){

            }
        })


    }

    private fun guardadoGrupoDataBase(profileImageUrl: String){

        val editText_NombreGrupo = findViewById<EditText>(R.id.editText_NewMenGroup_NombreGrupo).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val reference = FirebaseDatabase.getInstance().getReference("/group").push()
        val crearGrupo= Group(reference.key!!,editText_NombreGrupo.toString(),profileImageUrl,System.currentTimeMillis() / 1000)


        reference.setValue(crearGrupo)
            .addOnSuccessListener {
                Log.d("GrupoGuardadoConExito", "Grupo guardado: ${reference.key}")
            }

        Log.d("OtroErrorCargaMiembro", "Entra aqui ${seleccionaUsuariolistaDinamica.size}")

        for (indice in seleccionaUsuariolistaDinamica.indices){

            val referenceMiembrosGroup = FirebaseDatabase.getInstance().getReference("/group/${reference.key}/miembros/${seleccionaUsuariolistaDinamica[indice]}")

                if(refUsuarioLoggeado != seleccionaUsuariolistaDinamica[indice]) {
                    val miembrosGrupo = GroupMiembros(seleccionaUsuariolistaDinamica[indice],reference.key!!,"Usuarios",System.currentTimeMillis() / 1000)
                    referenceMiembrosGroup.setValue(miembrosGrupo)
                        .addOnSuccessListener {
                            Log.d(ChatMensaje.TAG, "Grupo guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamica[indice]}")
                        }

                }
                else{
                    val miembrosGrupo = GroupMiembros(seleccionaUsuariolistaDinamica[indice],reference.key!!,"Administrador",System.currentTimeMillis() / 1000)
                    referenceMiembrosGroup.setValue(miembrosGrupo)
                        .addOnSuccessListener {
                            Log.d(ChatMensaje.TAG, "Grupo guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamica[indice]}")
                        }
                }

            val refMiembrosGroup = FirebaseDatabase.getInstance().getReference("/group-miembros/${seleccionaUsuariolistaDinamica[indice]}/${reference.key}")

            if(refUsuarioLoggeado != seleccionaUsuariolistaDinamica[indice]) {
                val miembrosGrupo = GroupMiembros(seleccionaUsuariolistaDinamica[indice],reference.key!!,"Usuarios",System.currentTimeMillis() / 1000)
                refMiembrosGroup.setValue(miembrosGrupo)
                    .addOnSuccessListener {
                        Log.d(ChatMensaje.TAG, "Miembro-Grupo guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamica[indice]}")
                    }

            }
            else{
                val miembrosGrupo = GroupMiembros(seleccionaUsuariolistaDinamica[indice],reference.key!!,"Administrador",System.currentTimeMillis() / 1000)
                refMiembrosGroup.setValue(miembrosGrupo)
                    .addOnSuccessListener {
                        Log.d(ChatMensaje.TAG, "Miembro-guardado: ${reference.key} con miembro: ${seleccionaUsuariolistaDinamica[indice]}")
                    }
            }


            Log.d("NuevoMensajeGruposCheck","En el índice ${indice} tenemos a ${seleccionaUsuariolistaDinamica[indice]}")


        }


        //Toast.makeText( baseContext,"Entra aqui", Toast.LENGTH_SHORT).show()

        val fromId= reference.key
        val adminId = FirebaseAuth.getInstance().uid
        val refEncriptado = FirebaseDatabase.getInstance().getReference("/encriptado/mensajegrupo/$fromId")
        val mensajeEncriptadoGrupo= GrupoMensaje.grupoMessageEncriptacion("algo",refEncriptado.key!!,fromId.toString(),adminId.toString(),System.currentTimeMillis() / 1000)
        refEncriptado.setValue(mensajeEncriptadoGrupo)


    }

    private fun subirImagenStoragePerfilGrupo(){

        if(selectedPhotoUri == null){
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener{
            Log.d("NuevoMensajeGrupo","Successfully uploaded image:${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener{
                Log.d("NuevoMensajeGrupo","File Location: $it")
                guardadoGrupoDataBase(it.toString())
            }
        }

    }

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val boton_SeleccionPhoto = findViewById<ImageButton>(R.id.imageButton_NewMenGroup_SeleccionaImagen)
        val circle_View = findViewById<CircleImageView>(R.id.circle_NewMenGroup_PhotoView)

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

    class UserItem(val user: Registrarse.User?): Item<ViewHolder>(){

        override fun bind(viewHolder: ViewHolder, position: Int){

            if (user != null) {
                viewHolder.itemView.findViewById<TextView>(R.id.textView_LayUsNewMenGroup_ID).text = user.uid
                viewHolder.itemView.findViewById<TextView>(R.id.textView_LayUsNewMenGroup_Usuario).text = user.username
                Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsNewMenGroup_Imagen))
            }

        }

        override fun getLayout(): Int{
            return R.layout.layout_usuario_new_mensaje_group
        }

    }

    fun onCheckboxClicked(view: View, view02: View) {

            Log.d("NuevoMensajeGruposCheck", "Entro aqui onCheckboxClicked")

            if (view is CheckBox) {
                val checked: Boolean = view.isChecked

                when (view.id) {
                    R.id.checkBox_layoutNewMenGroup_IntegranteGrupo -> {
                        if (checked) {
                            Log.d("NuevoMensajeGruposCheck", "Se deselecciono usuario")
                            view.checkBox_layoutNewMenGroup_IntegranteGrupo.isChecked = false
                            //view.checkBox_layoutNewMenGroup_IntegranteGrupo.alpha = 0f
                            seleccionaUsuariolistaDinamica.remove(view02.textView_LayUsNewMenGroup_ID.text.toString())

                            for (indice in 0 until seleccionaUsuariolistaDinamica.size){
                                Log.d("NuevoMensajeGruposCheck","En el índice ${indice} tenemos a ${seleccionaUsuariolistaDinamica[indice]}")
                            }

                        } else {
                            Log.d("NuevoMensajeGruposCheck", "Se selecciono usuario")
                            view.checkBox_layoutNewMenGroup_IntegranteGrupo.isChecked = true
                            //view.checkBox_layoutNewMenGroup_IntegranteGrupo.alpha = 1f
                            seleccionaUsuariolistaDinamica.add(view02.textView_LayUsNewMenGroup_ID.text.toString())

                            for (indice in 0 until seleccionaUsuariolistaDinamica.size){
                                Log.d("NuevoMensajeGruposCheck","En el índice ${indice} tenemos a ${seleccionaUsuariolistaDinamica[indice]}")
                            }
                        }
                    }
                }
            }
        }


}

@Parcelize
class Group(val uid: String, val groupname: String, val profileImageUrl: String, val timestamp: Long): Parcelable {
    constructor(): this("","","",-1)
}

@Parcelize
class GroupMiembros(val uid: String, val uidGroup : String, val role: String, val timestamp: Long): Parcelable {
    constructor(): this("","","",-1)
}


