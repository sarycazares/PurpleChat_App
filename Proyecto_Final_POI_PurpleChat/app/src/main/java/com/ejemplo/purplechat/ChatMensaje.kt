package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.layout_usuario_new_mensaje_group.view.*

class ChatMensaje : AppCompatActivity() {

    companion object{
        val TAG = "ChatMensaje"
    }

    val adapter = GroupAdapter<ViewHolder>()
    val uidDelUsuarioPara = "h"
    //var toUser: Registrarse.User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_mensaje)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_ChatMen_FlechaRegresa)
        val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_ChatMen_ListaMensajes)
        val textViewUsuarioTitulo=findViewById<TextView>(R.id.textView_ChatMen_UsuarioTitulo)
        val imageButtonEnviarMensaje=findViewById<ImageButton>(R.id.imageButton_ChatMen_EnviarMensaje)
        val imageviewImagenUsuario=findViewById<ImageView>(R.id.imageView_ChatMen_ImagenUsuarip)

        recyclerViewListaMensajes.adapter = adapter

        val chatName = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        if (chatName != null) {
            Log.d("gruporecibimiento","$chatName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        // starts chat from bottom
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewListaMensajes.layoutManager = layoutManager

        // pushes up recycler view when softkeyboard popups up
        recyclerViewListaMensajes.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerViewListaMensajes.postDelayed(Runnable {
                    recyclerViewListaMensajes.scrollToPosition(
                        recyclerViewListaMensajes.adapter!!.itemCount -1)
                }, 100)
            }
        }

        //val userName = intent.getStringExtra(NuevoMensaje.USER_KEY)

        val user = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        if (user != null) {
            textViewUsuarioTitulo.text = user.username

            val targetImageView = imageviewImagenUsuario
            Picasso.get().load(user?.profileImageUrl).into(targetImageView)
        }

        recyclerViewListaMensajes.layoutManager = LinearLayoutManager(this)

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,Chat::class.java)
            startActivity(intent)
            finish()
        }

        //subirInformacionMensajes()
        escuchaMensajes()

        imageButtonEnviarMensaje.setOnClickListener{
            Log.d(TAG,"Envio un mensaje")
            performEnvioMensajes()
        }



        val menunavigationChat: NavigationView = findViewById(R.id.navigationChatMensaje)
        menunavigationChat.setNavigationItemSelectedListener { menuseleccionado ->

            when (menuseleccionado.itemId) {

                R.id.opc_ChatMen_Encriptar -> {
                    val fromId = FirebaseAuth.getInstance().uid
                    val usuario = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
                    val toId=usuario!!.uid

                    val refEncriptado = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$fromId/$toId")
                    var encriptado = "algo"

                    Log.d("EncriptareMenudo","Entra en el menu encriptar")

                    refEncriptado.addListenerForSingleValueEvent(object: ValueEventListener {

                        override fun onDataChange(p0: DataSnapshot){

                            val menEncriptacion = p0.getValue(chatMessageEncriptacion::class.java)

                            if (menEncriptacion != null) {
                                if (menEncriptacion.fromId == fromId.toString()) {

                                    if (menEncriptacion.encriptado == "activado") {

                                        val reference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$fromId/$toId")
                                        val toReference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$toId/$fromId")

                                        val mensajeEncriptadoChat = ChatMensaje.chatMessageEncriptacion("desactivado",reference.key!!,fromId.toString(),toId,System.currentTimeMillis() / 1000)
                                        reference.setValue(mensajeEncriptadoChat)
                                        toReference.setValue(mensajeEncriptadoChat)
                                        Toast.makeText( baseContext,"Encriptado Desactivado", Toast.LENGTH_SHORT).show()


                                    }
                                    else if (menEncriptacion.encriptado == "desactivado") {
                                        val reference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$fromId/$toId")
                                        val toReference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$toId/$fromId")

                                        val mensajeEncriptadoChat = ChatMensaje.chatMessageEncriptacion("activado",reference.key!!,fromId.toString(),toId,System.currentTimeMillis() / 1000)
                                        reference.setValue(mensajeEncriptadoChat)
                                        toReference.setValue(mensajeEncriptadoChat)
                                        Toast.makeText( baseContext, "Encriptado Activado",Toast.LENGTH_SHORT).show()
                                    }
                                    else {



                                        val reference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$fromId/$toId")
                                        val toReference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$toId/$fromId")

                                        val mensajeEncriptadoChat = ChatMensaje.chatMessageEncriptacion("activado",reference.key!!,fromId.toString(),toId,System.currentTimeMillis() / 1000)
                                        reference.setValue(mensajeEncriptadoChat)
                                        toReference.setValue(mensajeEncriptadoChat)
                                        Toast.makeText(baseContext,"Encriptado Activado",Toast.LENGTH_SHORT).show()
                                    }

                                }
                            }

                        }

                        override fun onCancelled(p0: DatabaseError){

                        }
                    })


                }
                R.id.opc_ChatMen_Muro -> {
                    Log.d("EncriptarMenu","Entra en el menu archivos")
                    val intent = Intent(this,ChatMuro::class.java)
                    intent.putExtra(NuevoMensaje.USER_KEY, chatName)
                    startActivity(intent)

                }
                R.id.opc_ChatMen_Participantes -> {
                    Log.d("EncriptarMenu","Entra en el menu archivos")
                    val intent = Intent(this,ChatParticipantes::class.java)
                    intent.putExtra(NuevoMensaje.USER_KEY, chatName)
                    startActivity(intent)
                }


            }
            true

        }

    }

    private fun escuchaMensajes(){

        //val ref = FirebaseDatabase.getInstance().getReference("/mensajes")
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        val toId = user?.uid
        val ref= FirebaseDatabase.getInstance().getReference("/user-mensajes/$fromId/$toId")
        Log.d("PruebaMensajeCarpeta","personas ${fromId} y ${toId}")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1:String?){
                val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_ChatMen_ListaMensajes)

                val mensaje = p0.getValue(chatMessage::class.java)
                if(mensaje != null){

                    Log.d(TAG, mensaje!!.text)

                    if (mensaje.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = PaginaPrincipal.currentUser ?: return

                        adapter.add(ChatItemEnviado(mensaje?.text, mensaje?.encriptado, currentUser))
                    } else{
                        val toUser = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
                        adapter.add(ChatItemRecibido(mensaje?.text, mensaje?.encriptado,toUser))
                    }
                }

                recyclerViewListaMensajes.scrollToPosition(adapter.itemCount-1)
            }
            override fun onCancelled(p0: DatabaseError){}
            override fun onChildChanged(p0: DataSnapshot,p1: String?){}
            override fun onChildMoved(p0: DataSnapshot, p1:String?){}
            override fun onChildRemoved(p0: DataSnapshot){}

        })
    }

    class chatMessage(val id: String, val text: String, val fromId: String, val toId: String, val encriptado: String, val timestamp: Long){
        constructor():this("","","","","",-1)
    }

    class chatMessageEncriptacion( val encriptado: String , val id: String, val fromId: String, val toId: String, val timestamp: Long){
        constructor():this("","","","",-1)
    }

    private fun performEnvioMensajes(){

        val editTextMensaje=findViewById<EditText>(R.id.editText_ChatMen_Mensaje)
        val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_ChatMen_ListaMensajes)

        var text = editTextMensaje.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        val toId=user!!.uid

        if (fromId == null) {
            return
        }

        val refEncriptado = FirebaseDatabase.getInstance().getReference("/encriptado/mensajechat/$fromId/$toId")
        var encriptadoEstado = "algo"

        refEncriptado.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot){

                val menEncriptacion = p0.getValue(chatMessageEncriptacion::class.java)

                if (menEncriptacion != null) {
                    if (menEncriptacion.fromId == fromId.toString()) {

                        if (menEncriptacion.encriptado == "activado") {
                            encriptadoEstado = "activado"

                            val ALFABETO:String = "abcdefghijklmnopqrstuvwxyz"

                            var desplazamiento:Int=3
                            var textoCesar:String=""
                            for( i in 0..text.length-1){
                                      if(esLetra(text.toLowerCase().get(i).toString())){
                                         //Obtiene posicion del caracter "i" del mensaje en el alfabeto
                                         var posicionActual:Int = ALFABETO.indexOf(text.toLowerCase().get(i))
                                         //Obtiene nueva posicion
                                         var nuevaPosicion:Int = ((desplazamiento + posicionActual) % 26)
                                         //Obtiene nuevo caracter y concatena en mensaje
                                         textoCesar +=  ALFABETO.get(nuevaPosicion)
                                      }else{//ignora y concatena caracter original en mensaje
                                         textoCesar += text.toLowerCase().get(i).toString()
                                      }
                                    }

                            text = textoCesar
                        }
                        else if (menEncriptacion.encriptado == "desactivado"){
                            encriptadoEstado = "desactivado"
                        }
                        else{
                            encriptadoEstado = "desactivado"
                        }
                    }
                }

                //val reference= FirebaseDatabase.getInstance().getReference("/mensajes").push()
                val reference = FirebaseDatabase.getInstance().getReference("/user-mensajes/$fromId/$toId").push()
                val toReference = FirebaseDatabase.getInstance().getReference("/user-mensajes/$toId/$fromId").push()

                val mensajeChat= chatMessage(reference.key!!, text, fromId, toId, encriptadoEstado, System.currentTimeMillis()/1000)
                reference.setValue(mensajeChat)
                    .addOnSuccessListener{
                        Log.d(TAG,"Mensaje guardado: ${reference.key}")
                        editTextMensaje.text.clear()
                        recyclerViewListaMensajes.scrollToPosition(adapter.itemCount - 1)
                    }

                toReference.setValue(mensajeChat)

                val ultimoMensajeRef = FirebaseDatabase.getInstance().getReference("/ultimo-mensaje/$fromId/$toId")
                ultimoMensajeRef.setValue(mensajeChat)

                val ultimoMensajeToRef = FirebaseDatabase.getInstance().getReference("/ultimo-mensaje/$toId/$fromId")
                ultimoMensajeToRef.setValue(mensajeChat)

            }
            override fun onCancelled(p0: DatabaseError){}
        })
        //Toast.makeText(baseContext,"Sale al if $encriptadoEstado",Toast.LENGTH_SHORT).show()



    }


    fun esLetra(texto:String):Boolean{
        return texto.matches("[a-z]+".toRegex())
    }

    //private fun subirInformacionMensajes(){

    //    val recyclerViewListaMensajes= findViewById<RecyclerView>(R.id.recyclerView_ChatMen_ListaMensajes)
    //    val adapter = GroupAdapter<ViewHolder>()

    //    adapter.add(ChatItemRecibido("te extraño vuelve"))
    //    adapter.add(ChatItemEnviado("no te preocupes nunca me fui"))

    //    recyclerViewListaMensajes.adapter = adapter

    //}
}




class ChatItemRecibido(var text: String, val encriptaEstado:String, val user: Registrarse.User?): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        if(encriptaEstado == "activado"){
            val ALFABETO:String = "abcdefghijklmnopqrstuvwxyz"
            var decifradoCesar:String=""
            var desplazamiento:Int=3

            for( i in 0..text.length-1){
                if(esLetraRecibido(text.toLowerCase().get(i).toString())){
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

        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatMen_Recibido).text=text

        val uri = user?.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutChatMen_Recibido)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int{
        return R.layout.layout_chat_mensaje_recibido
    }

    fun esLetraRecibido(texto:String):Boolean{
        return texto.matches("[a-z]+".toRegex())
    }

}

class ChatItemEnviado(var text: String, val encriptaEstado:String, val user: Registrarse.User?): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        if(encriptaEstado == "activado"){
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

        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatMen_Enviado).text=text

        val uri = user?.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutChatMen_Enviado)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int{
        return R.layout.layout_chat_mensaje_enviado
    }

    fun esLetraEnviado(texto:String):Boolean{
        return texto.matches("[a-z]+".toRegex())
    }
}

