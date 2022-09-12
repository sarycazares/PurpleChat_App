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
import com.ejemplo.purplechat.NuevoMensaje.Companion.USER_KEY
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatParticipantes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_participantes)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_ChatParticipantes_FlechaRegresar)
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_ChatParticipantes_ListaUsuarios)

        val chatName = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        if (chatName != null) {
            Log.d("gruporecibimiento","$chatName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,ChatMensaje::class.java)
            intent.putExtra(USER_KEY, chatName)
            startActivity(intent)
            finish()
        }

        recyclerViewListaUsuarios.layoutManager = LinearLayoutManager(this)

        buscaUsuariosDataBaseChat()


    }

    private fun buscaUsuariosDataBaseChat(){

        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid
        val chatName = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        val toId = chatName!!.uid
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_ChatParticipantes_ListaUsuarios)

        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){


                val adapter = GroupAdapter<ViewHolder>()
                adapter.clear()

                p0.children.forEach{

                    val user = it.getValue(Registrarse.User::class.java)
                    if (user!= null){
                        if(user.uid == refUsuarioLoggeado || user.uid == toId) {
                        adapter.add(UserParticipantesChat(user))
                        }
                    }

                    recyclerViewListaUsuarios.adapter= adapter
                }
            }

            override fun onCancelled(p0: DatabaseError){}
        })


    }

}

class UserParticipantesChat(val user: Registrarse.User?): Item<ViewHolder>(){

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