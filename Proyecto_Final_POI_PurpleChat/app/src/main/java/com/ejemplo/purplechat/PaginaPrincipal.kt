package com.ejemplo.purplechat

import android.R.attr
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.layout_pagina_principal_estado_conexion.view.*
import kotlinx.android.synthetic.main.layout_usuario_new_mensaje_group.view.*
import kotlinx.android.synthetic.main.layout_usuario_new_mensaje_group.view.checkBox_layoutNewMenGroup_IntegranteGrupo
import android.R.attr.data




class PaginaPrincipal : AppCompatActivity() {

    companion object{
        var currentUser: Registrarse.User?= null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagina_principal)

        verificarUsuarioLoggeado()
        fetchCurrentUser()
        subirEstadosDeConexion()


        val recyclerViewEstadoConexion= findViewById<RecyclerView>(R.id.recyclerView_PagPrin_EstadoConexion)

        recyclerViewEstadoConexion.layoutManager = LinearLayoutManager(this)



        val adapter = GroupAdapter<ViewHolder>()


        val minavigation: NavigationView = findViewById(R.id.minavigation)
        minavigation.setNavigationItemSelectedListener { menuseleccionado ->

            when (menuseleccionado.itemId) {

                R.id.opcchats -> {
                    val intent = Intent(this, Chat::class.java)
                    startActivity(intent)
                }
                R.id.opcgrupos -> {
                    val intent = Intent(this, Grupos::class.java)
                    startActivity(intent)
                }
                R.id.opc_Participantes -> {
                    val intent = Intent(this, Participantes::class.java)
                    startActivity(intent)
                }
                R.id.opclogros -> {
                    val intent = Intent(this, Recompensas::class.java)
                    startActivity(intent)
                }
                R.id.opccerrarsesion -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
            true

        }




    }

    private fun fetchCurrentUser(){

        val imagenPefil = findViewById<CircleImageView>(R.id.imageView_PagPrin_ImagenPerfil)
        val nombreUsuario = findViewById<TextView>(R.id.textView_PagPrin_NombreUsuario)
        val imagenEstadoconexion = findViewById<ImageView>(R.id.imageView_PagPrin_EstadoUsuario)

        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){
                currentUser= p0.getValue(Registrarse.User::class.java)
                Log.d("PaginaPrincipal", "Current user ${currentUser?.profileImageUrl}")
                Picasso.get().load(currentUser?.profileImageUrl).into(imagenPefil)
                nombreUsuario.text = currentUser!!.username

                var estado = currentUser!!.estadoConexion

                when (estado) {
                    "Disponible" -> {
                        imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_disponible)
                    }
                    "Ocupado" -> {
                        imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_ocupado)
                    }
                    "Ausente" -> {
                        imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_ausente)
                    }
                    "Desconectado" -> {
                        imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_desconectado)
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError){}

        })
    }

    private fun verificarUsuarioLoggeado() {

        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
    }

    private fun cambiarpantallas(pantallanueva: Fragment, etiqueta: String){
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor, pantallanueva, etiqueta)
            .commit()
    }


    private fun subirEstadosDeConexion(){

       val recyclerViewEstadoConexion= findViewById<RecyclerView>(R.id.recyclerView_PagPrin_EstadoConexion)
        val imagenEstadoconexion = findViewById<ImageView>(R.id.imageView_PagPrin_EstadoUsuario)
        val adapter = GroupAdapter<ViewHolder>()

        val fromId = FirebaseAuth.getInstance().uid
        val reference = FirebaseDatabase.getInstance().getReference("/users/$fromId")
        val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){
                adapter.clear()

                val usuario = p0.getValue(Registrarse.User::class.java)

                if (usuario != null) {
                    if (usuario.uid == fromId) {

                        val estadoConexion = usuario.estadoConexion
                        adapter.add(NuevoEstadoConexion("Disponible",estadoConexion, R.drawable.color_estadoconexion_disponible))
                        adapter.add(NuevoEstadoConexion("Ocupado",estadoConexion, R.drawable.color_estadoconexion_ocupado))
                        adapter.add(NuevoEstadoConexion("Ausente",estadoConexion, R.drawable.color_estadoconexion_ausente))
                        adapter.add(NuevoEstadoConexion("Desconectado",estadoConexion, R.drawable.color_estadoconexion_desconectado))

                    }
                }
            }

            override fun onCancelled(p0: DatabaseError){}
        })




        adapter.setOnItemClickListener { item, view ->
            Log.d("PaginaPrincipalCheck", "Entro aqui onCheckboxClicked")
            //onCheckboxClickedEstado(view.checkBox_layout_PagiPrin_EstadoConexion, view.textView_layout_PagPrin_EstadoConexion)


            reference.addListenerForSingleValueEvent(object: ValueEventListener {

                override fun onDataChange(p0: DataSnapshot){
                    val usuario = p0.getValue(Registrarse.User::class.java)

                    if (usuario != null) {
                        if (usuario.uid == fromId) {
                            val estado = view.textView_layout_PagPrin_EstadoConexion.text.toString()
                            val user = Registrarse.User(usuario.uid,usuario.username,estado,usuario.profileImageUrl)
                            reference.setValue(user)

                            when (estado) {
                                "Disponible" -> {
                                    imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_disponible)
                                }
                                "Ocupado" -> {
                                    imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_ocupado)
                                }
                                "Ausente" -> {
                                    imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_ausente)
                                }
                                "Desconectado" -> {
                                    imagenEstadoconexion.setBackgroundResource(R.drawable.color_estadoconexion_desconectado)
                                }
                            }


                            view.checkBox_layout_PagiPrin_EstadoConexion.isChecked = true
                            view.checkBox_layout_PagiPrin_EstadoConexion.alpha = 1f
                        }
                    }

                }

                override fun onCancelled(p0: DatabaseError){}
            })


        }

        recyclerViewEstadoConexion.adapter = adapter

    }
}



class NuevoEstadoConexion(val text: String, val estadoConexion:String,val drawable: Int): Item<ViewHolder>(){

    @SuppressLint("ResourceAsColor", "CutPasteId")
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.findViewById<TextView>(R.id.textView_layout_PagPrin_EstadoConexion).text=text

        //val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_layout_PagPrin_ColorConexion)
        //Picasso.get().load(R.color.purple_broadwayBaby).into(targetImageView)

        viewHolder.itemView.findViewById<ImageView>(R.id.imageView_layout_PagPrin_ColorConexion).setBackgroundResource(drawable)

        if(estadoConexion == text){
            viewHolder.itemView.findViewById<CheckBox>(R.id.checkBox_layout_PagiPrin_EstadoConexion).isChecked = true
            viewHolder.itemView.findViewById<CheckBox>(R.id.checkBox_layout_PagiPrin_EstadoConexion).alpha = 1f

        } else{
            viewHolder.itemView.findViewById<CheckBox>(R.id.checkBox_layout_PagiPrin_EstadoConexion).isChecked = false
            viewHolder.itemView.findViewById<CheckBox>(R.id.checkBox_layout_PagiPrin_EstadoConexion).alpha = 0f
        }


    }

    override fun getLayout(): Int{
        return R.layout.layout_pagina_principal_estado_conexion
    }

}


fun onCheckboxClickedEstado(view: View, view02: View) {

    Log.d("NuevoMensajeGruposCheck", "Entro aqui onCheckboxClicked")

    if (view is CheckBox) {
        val checked: Boolean = view.isChecked

        when (view.id) {
            R.id.checkBox_layout_PagiPrin_EstadoConexion -> {
                if (checked) {
                    Log.d("NuevoMensajeGruposCheck", "Se deselecciono usuario")
                    view.checkBox_layout_PagiPrin_EstadoConexion.isChecked = false
                    //view.checkBox_layout_PagiPrin_EstadoConexion.alpha = 0f

                } else {

                    view.checkBox_layout_PagiPrin_EstadoConexion.isChecked = true
                    //view.checkBox_layout_PagiPrin_EstadoConexion.alpha = 1f

                }
            }
        }
    }
}


