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
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import com.google.firebase.database.DataSnapshot


private val DatabaseReference.exist: Boolean
    get() { return true}

class Grupos : AppCompatActivity() {

    companion object {
        var seleccionaGrupolistaDinamica= ArrayList<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grupos)

        val imageButtonNuevoMensaje=findViewById<ImageButton>(R.id.imageButton_Grupos_NuevoMensaje)
        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_Grupos_FlechaRegresar)
        val recyclerViewUltimosMensajesChats=findViewById<RecyclerView>(R.id.recyclerView_Grupos_UltimosMensajes)
        recyclerViewUltimosMensajesChats.layoutManager = LinearLayoutManager(this)



        imageButtonNuevoMensaje.setOnClickListener{
            val intent = Intent(this,NuevoMensajeGrupos::class.java)
            startActivity(intent)
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,PaginaPrincipal::class.java)
            startActivity(intent)
            finish()
        }

        recyclerViewUltimosMensajesChats.adapter = adapter
        escuchaUltimosGrupos()

        adapter.setOnItemClickListener{ item,view->

            val intent = Intent(view.context,GrupoMensaje::class.java)
            val row = item as ultimoMensajeListaGroup
            intent.putExtra(NuevoMensajeGrupos.USER_KEYG, row.chatPartnetGroup)
            startActivity(intent)
            finish()
        }


    }

    val adapter = GroupAdapter<ViewHolder>()
    val ultimoMensajeGroupMap = HashMap<String, GroupMiembros>()

    private fun refreshRecyclerGroupViewMessages(){
        ultimoMensajeGroupMap.values.forEach{
            Log.d("ErrorVariosGrupos","Entro una vez con ${it.uidGroup}")
            adapter.add(ultimoMensajeListaGroup(it))
        }
    }

    private fun escuchaUltimosGrupos(){

        val usuarioLogeado = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/group-miembros/${usuarioLogeado}")
        val recyclerViewUltimosMensajesChats=findViewById<RecyclerView>(R.id.recyclerView_Grupos_UltimosMensajes)


        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1:String?){


                val grupo = p0.getValue(GroupMiembros::class.java) ?: return
                adapter.add(ultimoMensajeListaGroup(grupo))
                //Log.d("ErrorVariosGrupos","Entro una vez con ${grupo.uidGroup}")




                //val refMiembrosGrupo = FirebaseDatabase.getInstance().getReference("/group/${grupo.uid}/miembros")
                //val grupoMiembro = p0.getValue(GroupMiembros::class.java) ?: return


                //if (usuarioLogeado != null) {
                    //if(ref.child("/miembros").child(usuarioLogeado.toString()).exist){
                       // adapter.add(ultimoMensajeListaGroup(grupo))
                    //}
                //}


            }
            override fun onChildChanged(p0: DataSnapshot,p1: String?){

                val grupo = p0.getValue(GroupMiembros::class.java) ?: return
                adapter.add(ultimoMensajeListaGroup(grupo))
                //ultimoMensajeGroupMap[p0.key!!]= grupo
                //refreshRecyclerGroupViewMessages()
            }
            override fun onCancelled(p0: DatabaseError){}
            override fun onChildMoved(p0: DataSnapshot, p1:String?){}
            override fun onChildRemoved(p0: DataSnapshot){}

        })

    }

    class ultimoMensajeListaGroup(val grupoMiembro: GroupMiembros): Item<ViewHolder>(){

        var chatPartnetGroup : Group?=null


        override fun bind(viewHolder: ViewHolder, position: Int){

            val refUltimoMensaje = FirebaseDatabase.getInstance().getReference("/group-ultimo-mensaje/${grupoMiembro.uidGroup}")

            refUltimoMensaje.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    val UltimoMensaje = p0.getValue(GrupoMensaje.grupoMessage::class.java)
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

            val ref = FirebaseDatabase.getInstance().getReference("/group/${grupoMiembro.uidGroup}")
            Log.d("ErrorGrupoPartnerID","Esto manda ${chatPartnetGroup}")

            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val mensajeChatGroup = p0.getValue(Group::class.java)

                    viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutChatUltMenChat_Usuario).text= mensajeChatGroup?.groupname
                    val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutChatUltMenChat)
                    Picasso.get().load(mensajeChatGroup?.profileImageUrl).into(targetImageView)

                    chatPartnetGroup = mensajeChatGroup

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