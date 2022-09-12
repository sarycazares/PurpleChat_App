package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class Recompensas : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recompensas)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_Recompensas_FlechaRegresar)
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_Recompensas_ListaUsuarios)

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,PaginaPrincipal::class.java)
            startActivity(intent)
            finish()
        }

        recyclerViewListaUsuarios.layoutManager = LinearLayoutManager(this)

        buscaUsuariosDataBase()

    }

    private fun buscaUsuariosDataBase(){

        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid
        val recyclerViewListaUsuarios=findViewById<RecyclerView>(R.id.recyclerView_Recompensas_ListaUsuarios)

        val ref= FirebaseDatabase.getInstance().getReference("/recompensas/$refUsuarioLoggeado")
        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot){


                val adapter = GroupAdapter<ViewHolder>()
                adapter.clear()

                p0.children.forEach{

                    val recompensa = it.getValue(Registrarse.Recompensa::class.java)
                    if (recompensa!= null){

                        adapter.add(RecompensaIteam(recompensa))
                        //}
                    }

                    recyclerViewListaUsuarios.adapter= adapter
                }
            }

            override fun onCancelled(p0: DatabaseError){}
        })


    }

}

class RecompensaIteam(val recompensa: Registrarse.Recompensa): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){


            viewHolder.itemView.findViewById<TextView>(R.id.textView_LayUsRecompensas_Usuario).text = recompensa.recompensa


    }

    override fun getLayout(): Int{
        return R.layout.layout_recompensas
    }

}