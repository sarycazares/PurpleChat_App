package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class Chat : AppCompatActivity() {

    companion object{
        var currentUser: Registrarse.User?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val imageButtonNuevoMensaje=findViewById<ImageButton>(R.id.imageButton_Chat_NuevoMensaje)
        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_Chat_FlechaRegresar)
        val recyclerViewUltimosMensajesChats=findViewById<RecyclerView>(R.id.recyclerView_Chat_UltimosMensajesChats)

        recyclerViewUltimosMensajesChats.layoutManager = LinearLayoutManager(this)


        imageButtonNuevoMensaje.setOnClickListener{
            val intent = Intent(this,NuevoMensaje::class.java)
            startActivity(intent)
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,PaginaPrincipal::class.java)
            startActivity(intent)
            finish()
        }



        recyclerViewUltimosMensajesChats.adapter = adapter
        //recyclerViewUltimosMensajesChats.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setOnItemClickListener{ item,view->

            val intent = Intent(view.context,ChatMensaje::class.java)
            val row = item as ultimoMensajeLista
            intent.putExtra(NuevoMensaje.USER_KEY, row.chatPartnetUser)
            startActivity(intent)
            finish()
        }

        escuchaUltimosMensajes()

    }

    val ultimoMensajeMap = HashMap<String, ChatMensaje.chatMessage>()

    private fun refreshRecyclerViewMessages(){
        ultimoMensajeMap.values.forEach{
            adapter.add(ultimoMensajeLista(it))
        }
    }

    private fun escuchaUltimosMensajes(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/ultimo-mensaje/$fromId")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1:String?){
                val mensaje = p0.getValue(ChatMensaje.chatMessage::class.java) ?: return

                adapter.add(ultimoMensajeLista(mensaje))
                //ultimoMensajeMap[p0.key!!]= mensaje
                //refreshRecyclerViewMessages()

               // adapter.add(ultimoMensajesLista(mensaje))
            }
            override fun onChildChanged(p0: DataSnapshot,p1: String?){
                val mensaje = p0.getValue(ChatMensaje.chatMessage::class.java) ?: return
                adapter.add(ultimoMensajeLista(mensaje))

                //ultimoMensajeMap[p0.key!!]= mensaje
                //refreshRecyclerViewMessages()

                // adapter.add(ultimoMensajesLista(mensaje))
            }
            override fun onCancelled(p0: DatabaseError){}
            override fun onChildMoved(p0: DataSnapshot, p1:String?){}
            override fun onChildRemoved(p0: DataSnapshot){}

        })
    }

    val adapter = GroupAdapter<ViewHolder>()

   // private fun configurarListas(){
   //     val recyclerViewUltimosMensajesChats=findViewById<RecyclerView>(R.id.recyclerView_Chat_UltimosMensajesChats)
   //     val adapter = GroupAdapter<ViewHolder>()

    //    adapter.add(ultimoMensajesLista())
    //    adapter.add(ultimoMensajesLista())
    //    adapter.add(ultimoMensajesLista())

   // }

    class ultimoMensajeLista(val chatMensaje: ChatMensaje.chatMessage): Item<ViewHolder>(){
        var chatPartnetUser : Registrarse.User?=null

        override fun bind(viewHolder: ViewHolder, position: Int){

            var text:String= chatMensaje.text

            if(chatMensaje.encriptado == "activado"){
                val ALFABETO:String = "abcdefghijklmnopqrstuvwxyz"
                var decifradoCesar:String=""
                var desplazamiento:Int=3

                for( i in 0..text.length-1){
                    if(esLetraEnviado(text.toLowerCase().get(i).toString())){
                        var posicionActual:Int = ALFABETO.indexOf(text.toLowerCase().get(i))
                        var nuevaPosicion:Int = ((posicionActual - desplazamiento) % 26)
                        if (nuevaPosicion < 0)
                        {
                            nuevaPosicion = ALFABETO.length + nuevaPosicion
                        }
                        decifradoCesar += ALFABETO.get(nuevaPosicion)
                    }
                    else{//ignora y concatena en mensaje
                        decifradoCesar += text.toLowerCase().get(i).toString()
                    }
                }

                text = decifradoCesar
            }

            viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatUltMenChat_Mensaje).text= text

            val chatPartnerId : String
            if(chatMensaje.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId  = chatMensaje.toId
            } else{
                chatPartnerId  = chatMensaje.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            Log.d("ErrorChatPartnerID","Esto manda ${chatPartnerId}")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnetUser = p0.getValue(Registrarse.User::class.java)
                    viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatUltMenChat_Usuario).text= chatPartnetUser?.username

                    val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutChatUltMenChat)
                    Picasso.get().load(chatPartnetUser?.profileImageUrl).into(targetImageView)

                }
                override fun onCancelled(p0: DatabaseError){}
            })

        }
        override fun getLayout():Int{
            return R.layout.layout_chat_ultimos_mensajes_chats
        }

        fun esLetraEnviado(texto:String):Boolean{
            return texto.matches("[a-z]+".toRegex())
        }
    }





}