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
import com.ejemplo.purplechat.NuevoMensajeEquipos.Companion.USER_KEYE
import com.ejemplo.purplechat.NuevoMensajeGrupos.Companion.USER_KEYG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class EquipoParticipantes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipo_participantes)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_EquipoParticipantes_FlechaRegresar)
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_EquipoParticipantes_ListaUsuarios)

        val groupName = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        if (equipoName != null) {
            Log.d("equiporecibimiento","$equipoName")
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,EquipoMensaje::class.java)
            intent.putExtra(USER_KEYE, equipoName)
            intent.putExtra(USER_KEYG, groupName)
            startActivity(intent)
            finish()
        }

        recyclerViewListaUsuarios.layoutManager = LinearLayoutManager(this)

        buscaUsuariosDataBaseEquipo()

    }

    private fun buscaUsuariosDataBaseEquipo(){

        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_EquipoParticipantes_ListaUsuarios)


        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid
        val EquipoNameID = intent.getParcelableExtra<Equipo>(USER_KEYE)
        val toId = EquipoNameID!!.uid


        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){


                val adapter = GroupAdapter<ViewHolder>()
                adapter.clear()

                p0.children.forEach{

                    val user = it.getValue(Registrarse.User::class.java)
                    if (user!= null){

                        val userId = user.uid

                        val reference= FirebaseDatabase.getInstance().getReference("/equipo/$toId/miembros/$userId")

                        reference.addListenerForSingleValueEvent(object: ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot){

                                val miembroEquipo = p0.getValue(EquipoMiembros::class.java)
                                if (miembroEquipo != null) {
                                    if (miembroEquipo.uidEquipo == toId) {
                                        if(miembroEquipo.uid == user.uid) {

                                            adapter.add(UserParticipantesEquipo(user))

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

class UserParticipantesEquipo(val user: Registrarse.User?): Item<ViewHolder>(){

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