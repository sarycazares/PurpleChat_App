package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ejemplo.purplechat.NuevoMensajeEquipos.Companion.USER_KEYE
import com.ejemplo.purplechat.NuevoMensajeGrupos.Companion.USER_KEYG
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class EquipoMensaje : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipo_mensaje)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_EquipoMen_FlechaRegresa)
        val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_EquipoMen_ListaMensajes)
        val textViewEquipoTitulo=findViewById<TextView>(R.id.textView_EquipoMen_UsuarioTitulo)
        val imageButtonEnviarMensaje=findViewById<ImageButton>(R.id.imageButton_EquipoMen_EnviarMensaje)
        val imageviewImagenEquipo=findViewById<ImageView>(R.id.imageView_EquipoMen_ImagenGrupo)

        recyclerViewListaMensajes.adapter = adapter
        recyclerViewListaMensajes.layoutManager = LinearLayoutManager(this)

        // starts chat from bottom
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewListaMensajes.layoutManager = layoutManager

        val groupName = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        if (equipoName != null) {
            textViewEquipoTitulo.text = equipoName.equiponame

            val targetImageView = imageviewImagenEquipo
            Picasso.get().load(equipoName.profileImageUrl).into(targetImageView)
        }

        // pushes up recycler view when softkeyboard popups up
        recyclerViewListaMensajes.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                recyclerViewListaMensajes.postDelayed(Runnable {
                    recyclerViewListaMensajes.scrollToPosition(
                        recyclerViewListaMensajes.adapter!!.itemCount -1)
                }, 100)
            }
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,Equipos::class.java)
            intent.putExtra(NuevoMensajeGrupos.USER_KEYG, groupName)
            startActivity(intent)
            finish()
        }

        escuchaMensajesEquipo()

        imageButtonEnviarMensaje.setOnClickListener{
            Log.d("GrupoMensaje","Envio un mensaje")
            performEnvioMensajesEquipo()
        }

        val menunavigationGrupo: NavigationView = findViewById(R.id.navigationEquipoMensaje)
        menunavigationGrupo.setNavigationItemSelectedListener { menuseleccionado ->

            when (menuseleccionado.itemId) {

                R.id.opc_EquipoMen_Muro -> {
                    Log.d("EncriptarMenu","Entra en el menu archivos")
                    val intent = Intent(this,EquipoMuro::class.java)
                    intent.putExtra(USER_KEYE, equipoName)
                    intent.putExtra(USER_KEYG, groupName)
                    startActivity(intent)
                }
                R.id.opc_EquipoMen_Encriptar -> {

                    val equipo = intent.getParcelableExtra<Equipo>(USER_KEYE)
                    val fromId=equipo!!.uid
                    val adminId = FirebaseAuth.getInstance().uid

                    val refEncriptado = FirebaseDatabase.getInstance().getReference("/encriptado/mensajeequipo/$fromId")
                    var encriptado = "algo"

                    Log.d("EncriptareMenudo","Entra en el menu encriptar")

                    refEncriptado.addListenerForSingleValueEvent(object: ValueEventListener {

                        override fun onDataChange(p0: DataSnapshot){

                            val menEncriptacion = p0.getValue(EquipoMessageEncriptacion::class.java)

                            if (menEncriptacion != null) {
                                if (menEncriptacion.adminId == adminId.toString()) {

                                    if (menEncriptacion.encriptado == "activado") {

                                        val reference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajeequipo/$fromId")


                                        val mensajeEncriptadoEquipo = EquipoMessageEncriptacion("desactivado",reference.key!!,fromId,adminId.toString(),System.currentTimeMillis() / 1000)
                                        reference.setValue(mensajeEncriptadoEquipo)

                                        Toast.makeText( baseContext,"Encriptado Desactivado", Toast.LENGTH_SHORT).show()


                                    }
                                    else if (menEncriptacion.encriptado == "desactivado") {
                                        val reference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajeequipo/$fromId")

                                        val mensajeEncriptadoEquipo = EquipoMessageEncriptacion("activado", reference.key!!,fromId,adminId.toString(),System.currentTimeMillis() / 1000)
                                        reference.setValue(mensajeEncriptadoEquipo)
                                        Toast.makeText( baseContext, "Encriptado Activado", Toast.LENGTH_SHORT).show()
                                    }
                                    else {

                                        val reference = FirebaseDatabase.getInstance().getReference("/encriptado/mensajeequipo/$fromId")

                                        val mensajeEncriptadoEquipo = EquipoMessageEncriptacion("activado",reference.key!!,fromId,adminId.toString(),System.currentTimeMillis() / 1000)
                                        reference.setValue(mensajeEncriptadoEquipo)
                                        Toast.makeText(baseContext,"Encriptado Activado", Toast.LENGTH_SHORT).show()
                                    }

                                } else{
                                    Toast.makeText(baseContext,"No esta autorizado para encriptar equipo", Toast.LENGTH_SHORT).show()
                                    val uidUsuarioLog = FirebaseAuth.getInstance().uid
                                    val referenceRecompensa = FirebaseDatabase.getInstance().getReference("/recompensas/$uidUsuarioLog").push()
                                    val crearrecompensa = Registrarse.Recompensa(
                                        referenceRecompensa.key!!,
                                        uidUsuarioLog.toString(),
                                        "Logro: ¡Oh no! No puedes encriptar :c",
                                        System.currentTimeMillis() / 1000
                                    )
                                    referenceRecompensa.setValue(crearrecompensa)
                                    Toast.makeText( baseContext,"Logro Desbloqueado: ¡Oh no! No puedes encriptar :c", Toast.LENGTH_SHORT).show()
                                }
                            }

                        }

                        override fun onCancelled(p0: DatabaseError){

                        }
                    })

                }
                R.id.opc_EquipoMen_Participantes->{
                    Log.d("EncriptarMenu","Entra en el menu archivos")
                    val intent = Intent(this,EquipoParticipantes::class.java)
                    intent.putExtra(USER_KEYE, equipoName)
                    intent.putExtra(USER_KEYG, groupName)
                    startActivity(intent)
                }

            }
            true

        }

    }


    private fun escuchaMensajesEquipo(){

        //val ref = FirebaseDatabase.getInstance().getReference("/mensajes")
        val fromId = FirebaseAuth.getInstance().uid
        val equipo = intent.getParcelableExtra<Equipo>(USER_KEYE)
        val toId = equipo?.uid
        val ref= FirebaseDatabase.getInstance().getReference("/equipo-mensajes/$toId")


        Log.d("PruebaMensajeCarpeta","personas ${fromId} y ${toId}")

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1:String?){
                val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_EquipoMen_ListaMensajes)

                val mensaje = p0.getValue(EquipoMessage::class.java)
                if(mensaje != null){

                    Log.d("EnvioMensajeLista", mensaje!!.text)

                    if (mensaje.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = PaginaPrincipal.currentUser ?: return
                        adapter.add(EquipoItemEnviado(mensaje?.text, mensaje.encriptado, currentUser))

                        Log.d("ErrorGrupoLeerMensajes", "Entro a Mensaje Propio")
                    } else{

                        Log.d("ErrorGrupoLeerMensajes", "Entro aqui")

                        val refUser= FirebaseDatabase.getInstance().getReference("/users/${mensaje.fromId}")

                        refUser.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(p0: DataSnapshot) {

                                Log.d("ErrorGrupoLeerMensajes", "Entro al listener")

                                val mensajeEquipo = p0.getValue(Registrarse.User::class.java)

                                Log.d("ErrorGrupoLeerMensajes", "Entro despues del listener ${mensajeEquipo?.uid}")

                                if(mensajeEquipo?.uid == mensaje.fromId) {

                                    Log.d("ErrorGrupoLeerMensajes", "Entro al adapter")
                                    adapter.add(EquipoItemRecibido(mensaje?.text,mensaje.encriptado, mensajeEquipo))
                                }

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Log.d("ErrorGrupoLeerMensajes", "Marco error al traer datos del usuario al grupo")
                            }
                        })

                    }
                }

                recyclerViewListaMensajes.scrollToPosition(adapter.itemCount-1)
            }
            override fun onCancelled(p0: DatabaseError){}
            override fun onChildChanged(p0: DataSnapshot, p1: String?){}
            override fun onChildMoved(p0: DataSnapshot, p1:String?){}
            override fun onChildRemoved(p0: DataSnapshot){}

        })
    }

    class EquipoMessage(val id: String, val text: String, val fromId: String, val toId: String,val encriptado: String, val timestamp: Long){
        constructor():this("","","","","",-1)
    }

    class EquipoMessageEncriptacion( val encriptado: String , val id: String, val fromId: String, val adminId: String, val timestamp: Long){
        constructor():this("","","","",-1)
    }

    private fun performEnvioMensajesEquipo(){

        val editTextMensaje=findViewById<EditText>(R.id.editText_EquipoMen_Mensaje)
        val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_EquipoMen_ListaMensajes)

        var text = editTextMensaje.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val equipo = intent.getParcelableExtra<Equipo>(USER_KEYE)
        val toId=equipo!!.uid

        if (fromId == null) {
            return
        }

        val refEncriptado = FirebaseDatabase.getInstance().getReference("/encriptado/mensajeequipo/$toId")
        var encriptadoEstado = "algo"

        refEncriptado.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot){

                val menEncriptacion = p0.getValue(EquipoMessageEncriptacion::class.java)

                if (menEncriptacion != null) {
                    if (menEncriptacion.fromId == toId) {

                        //Toast.makeText(baseContext,"Entra al if",Toast.LENGTH_SHORT).show()

                        if (menEncriptacion.encriptado == "activado") {
                            //Toast.makeText(baseContext,"Entra a la encriptacion",Toast.LENGTH_SHORT).show()

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
                val reference = FirebaseDatabase.getInstance().getReference("/equipo-mensajes/$fromId/$toId").push()
                val toReference = FirebaseDatabase.getInstance().getReference("/equipo-mensajes/$toId").push()

                val mensajeEquipo= EquipoMessage(reference.key!!,text,fromId,toId,encriptadoEstado,System.currentTimeMillis() / 1000)
                reference.setValue(mensajeEquipo)
                    .addOnSuccessListener{
                        Log.d(ChatMensaje.TAG,"Mensaje guardado: ${reference.key}")
                        editTextMensaje.text.clear()
                        recyclerViewListaMensajes.scrollToPosition(adapter.itemCount - 1)
                    }

                toReference.setValue(mensajeEquipo)

                val ultimoMensajeRef = FirebaseDatabase.getInstance().getReference("/equipo-ultimo-mensaje/$fromId/$toId")
                ultimoMensajeRef.setValue(mensajeEquipo)

                val ultimoMensajeToRef = FirebaseDatabase.getInstance().getReference("/equipo-ultimo-mensaje/$toId")
                ultimoMensajeToRef.setValue(mensajeEquipo)

            }
            override fun onCancelled(p0: DatabaseError){}
        })
        //Toast.makeText(baseContext,"Sale al if $encriptadoEstado",Toast.LENGTH_SHORT).show()

    }

    fun esLetra(texto:String):Boolean{
        return texto.matches("[a-z]+".toRegex())
    }

}


class EquipoItemRecibido(var text: String, val encriptaEstado:String, val user: Registrarse.User?): Item<ViewHolder>(){

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

        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutGroupMen_Recibido).text=text

        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutGroupMen_Usuario).text=user?.username

        val uri = user?.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutGroupMen_Recibido)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int{
        return R.layout.layout_grupo_mensaje_recibido
    }

    fun esLetraRecibido(texto:String):Boolean{
        return texto.matches("[a-z]+".toRegex())
    }

}

class EquipoItemEnviado(var text: String, val encriptaEstado:String, val user: Registrarse.User?): Item<ViewHolder>(){

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

        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutGroupMen_Enviado).text=text

        val uri = user?.profileImageUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutGroupMen_Enviado)
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int{
        return R.layout.layout_grupo_mensaje_enviado
    }

    fun esLetraEnviado(texto:String):Boolean{
        return texto.matches("[a-z]+".toRegex())
    }

}