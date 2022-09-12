package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ejemplo.purplechat.NuevoMensajeEquipos.Companion.USER_KEYE
import com.ejemplo.purplechat.NuevoMensajeGrupos.Companion.USER_KEYG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class Equipos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipos)

        val imageButtonNuevoMensaje=findViewById<ImageButton>(R.id.imageButton_Equipos_NuevoMensaje)
        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_Equipos_FlechaRegresar)
        val recyclerViewUltimosMensajesChats=findViewById<RecyclerView>(R.id.recyclerView_Equipos_UltimosMensajes)
        recyclerViewUltimosMensajesChats.layoutManager = LinearLayoutManager(this)

        val textviewPrueba=findViewById<TextView>(R.id.textView_equipos_prueba)

        //val valor = intent.extras!!.getString("grupoUID")
        //Toast.makeText( baseContext,"me recibo $valor", Toast.LENGTH_SHORT).show()
        //    textviewPrueba.text = valor

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }


        imageButtonNuevoMensaje.setOnClickListener{
            val intent = Intent(this,NuevoMensajeEquipos::class.java)
            intent.putExtra(USER_KEYG, groupName)
            startActivity(intent)
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,GrupoMensaje::class.java)
            intent.putExtra(NuevoMensajeGrupos.USER_KEYG, groupName)
            startActivity(intent)
            finish()
        }

        recyclerViewUltimosMensajesChats.adapter = adapter
        escuchaUltimosEquipos()

        adapter.setOnItemClickListener{ item,view->

            val intent = Intent(view.context,EquipoMensaje::class.java)
            val row = item as ultimoMensajeListaEquipo
            intent.putExtra(USER_KEYE, row.chatPartnetEquipo)
            intent.putExtra(USER_KEYG, groupName)
            startActivity(intent)
            finish()
        }


    }

    val adapter = GroupAdapter<ViewHolder>()
    val ultimoMensajeEquipoMap = HashMap<String, EquipoMiembros>()

    private fun refreshRecyclerEquipoViewMessages(){
        ultimoMensajeEquipoMap.values.forEach{
            Log.d("ErrorVariosEquipo","Entro una vez con ${it.uidEquipo}")
            adapter.add(ultimoMensajeListaEquipo(it))
        }
    }

    private fun escuchaUltimosEquipos(){

        val usuarioLogeado = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/equipo-miembros/${usuarioLogeado}")
        val recyclerViewUltimosMensajesEquipo=findViewById<RecyclerView>(R.id.recyclerView_Equipos_UltimosMensajes)

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        val groupId = groupName!!.uid

        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1:String?){


                val equipo = p0.getValue(EquipoMiembros::class.java) ?: return

                if (groupId == equipo.uidGroup) {
                    adapter.add(ultimoMensajeListaEquipo(equipo))
                }


            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?){

                val equipo = p0.getValue(EquipoMiembros::class.java) ?: return

                if (groupId == equipo.uidGroup) {
                    adapter.add(ultimoMensajeListaEquipo(equipo))
                }

            }
            override fun onCancelled(p0: DatabaseError){}
            override fun onChildMoved(p0: DataSnapshot, p1:String?){}
            override fun onChildRemoved(p0: DataSnapshot){}

        })

    }

    class ultimoMensajeListaEquipo(val EquipoMiembro: EquipoMiembros): Item<ViewHolder>(){

        var chatPartnetEquipo : Equipo?=null


        override fun bind(viewHolder: ViewHolder, position: Int){

            val refUltimoMensaje = FirebaseDatabase.getInstance().getReference("/equipo-ultimo-mensaje/${EquipoMiembro.uidEquipo}")

            refUltimoMensaje.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val UltimoMensaje = p0.getValue(EquipoMensaje.EquipoMessage::class.java)
                    if (UltimoMensaje?.text == null){
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatUltMenChat_Mensaje).text = "Aún no hay mensajes"
                    } else {

                        var text:String= UltimoMensaje.text

                        if(UltimoMensaje.encriptado == "activado"){
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

                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatUltMenChat_Mensaje).text = text
                    }
                }
                override fun onCancelled(p0: DatabaseError){}
            })

            val chatPartnerId : String

            val ref = FirebaseDatabase.getInstance().getReference("/equipo/${EquipoMiembro.uidEquipo}")
            Log.d("ErrorGrupoPartnerID","Esto manda ${chatPartnetEquipo}")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val mensajeChatEquipo = p0.getValue(Equipo::class.java)

                    viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatUltMenChat_Usuario).text= mensajeChatEquipo?.equiponame
                    val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutChatUltMenChat)
                    Picasso.get().load(mensajeChatEquipo?.profileImageUrl).into(targetImageView)

                    chatPartnetEquipo = mensajeChatEquipo

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