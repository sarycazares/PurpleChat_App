package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.layout_usuario_new_mensaje_equipos.view.*

class GrupoParticipantes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grupo_participantes)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_GrupoParticipantes_FlechaRegresar)
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_GrupoParticipantes_ListaUsuarios)

        val groupName = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,GrupoMensaje::class.java)
            intent.putExtra(NuevoMensajeGrupos.USER_KEYG, groupName)
            startActivity(intent)
            finish()
        }

        recyclerViewListaUsuarios.layoutManager = LinearLayoutManager(this)

        buscaUsuariosDataBaseGrupo()

    }

    private fun buscaUsuariosDataBaseGrupo(){

        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_GrupoParticipantes_ListaUsuarios)


        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid
        val groupNameID = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        val toId = groupNameID!!.uid


        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){


                val adapter = GroupAdapter<ViewHolder>()
                adapter.clear()

                p0.children.forEach{

                    val user = it.getValue(Registrarse.User::class.java)
                    if (user!= null){

                        val userId = user.uid

                        val reference= FirebaseDatabase.getInstance().getReference("/group/$toId/miembros/$userId")

                        reference.addListenerForSingleValueEvent(object: ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot){

                                val miembroGrupo = p0.getValue(GroupMiembros::class.java)
                                if (miembroGrupo != null) {
                                    if (miembroGrupo.uidGroup == toId) {
                                        if(miembroGrupo.uid == user.uid) {

                                            adapter.add(UserParticipantesGrupo(user))

                                        }

                                    }
                                }


                            }
                            override fun onCancelled(p0: DatabaseError){}
                        })

                    }

                    recyclerViewListaUsuarios.adapter= adapter
                }
            }

            override fun onCancelled(p0: DatabaseError){}
        })




    }


}

class UserParticipantesGrupo(val user: Registrarse.User?): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        if (user != null) {
            viewHolder.itemView.findViewById<TextView>(R.id.textView_LayUsParti_Usuario).text = user.username
            viewHolder.itemView.findViewById<TextView>(R.id.textView_LayUsParti_Conexion).text = user.estadoConexion
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsParti_Imagen))

            val estadoConexion = user.estadoConexion

            when (estadoConexion) {
                "Disponible" -> {
                    viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsParti_Estado).setBackgroundResource(R.drawable.color_estadoconexion_disponible)
                }
                "Ocupado" -> {
                    viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsParti_Estado).setBackgroundResource(R.drawable.color_estadoconexion_ocupado)
                }
                "Ausente" -> {
                    viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsParti_Estado).setBackgroundResource(R.drawable.color_estadoconexion_ausente)
                }
                "Desconectado" -> {
                    viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsParti_Estado).setBackgroundResource(R.drawable.color_estadoconexion_desconectado)
                }
            }

        }

    }

    override fun getLayout(): Int{
        return R.layout.layout_participantes_lista_usuarios
    }

}