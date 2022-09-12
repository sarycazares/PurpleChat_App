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

class NuevoMensaje : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_mensaje)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_NewMen_FlechaRegresar)
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_NewMen_ListaUsuarios)

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,Chat::class.java)
            startActivity(intent)
            finish()
        }

        recyclerViewListaUsuarios.layoutManager = LinearLayoutManager(this)

       // val adapter = GroupAdapter<ViewHolder>()

        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())


       //recyclerViewListaUsuarios.adapter = adapter

        buscaUsuariosDataBase()

    }

    companion object{
        val USER_KEY = "USER_KEY"
    }


    private fun buscaUsuariosDataBase(){

        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_NewMen_ListaUsuarios)

        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){

                val adapter = GroupAdapter<ViewHolder>()

                p0.children.forEach{
                    Log.d("NuevoMensaje", it.toString())
                    val user = it.getValue(Registrarse.User::class.java)
                    if (user!= null){
                        if(user.uid != refUsuarioLoggeado) {
                            adapter.add(UserItem(user))
                        }
                    }

                    adapter.setOnItemClickListener { item, view ->

                        val userItem = item as UserItem
                        val fromId = FirebaseAuth.getInstance().uid
                        val toId= userItem.user?.uid
                        val refEncriptado = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$fromId/$toId")
                        val mensajeEncriptadoChat= ChatMensaje.chatMessageEncriptacion("algo", refEncriptado.key!!, fromId.toString(),toId.toString(),System.currentTimeMillis() / 1000)
                        refEncriptado.setValue(mensajeEncriptadoChat)

                        val refEncripta = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$toId/$fromId")
                        refEncripta.setValue(mensajeEncriptadoChat)

                        val intent = Intent(view.context,ChatMensaje::class.java)
                        //intent.putExtra(USER_KEY, userItem.user?.username)
                        intent.putExtra(USER_KEY, userItem.user)
                        startActivity(intent)
                        finish()
                    }

                    recyclerViewListaUsuarios.adapter= adapter
                }
            }

            override fun onCancelled(p0: DatabaseError){

            }
        })


    }


}


class UserItem(val user: Registrarse.User?): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        if (user != null) {
            viewHolder.itemView.findViewById<TextView>(R.id.textView_LayNewMen_Usuario).text = user.username
            Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayUsNewMen_Imagen))
        }

    }

    override fun getLayout(): Int{
        return R.layout.layout_usuario_new_mensaje
    }

}