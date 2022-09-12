package com.ejemplo.purplechat

import android.R.attr
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ejemplo.purplechat.NuevoMensaje.Companion.USER_KEY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.layout_muro_archivo.view.*
import kotlinx.android.synthetic.main.layout_muro_ubicacion.view.*
import kotlinx.android.synthetic.main.layout_pagina_principal_estado_conexion.view.*
import android.R.attr.label

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import kotlinx.android.synthetic.main.layout_muro_archivo.view.imageButton_LayoutMuroArchivo_Archivo
import kotlinx.android.synthetic.main.layout_muro_texto.view.*


class ChatMuro : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    companion object {
        val USER_KEYChatMuro = "USER_KEYChatMuro"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_muro)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_ChatMuro_FlechaRegresar)
        val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_ChatMuro_UltimosMensajes)

        val botonrecargar=findViewById<ImageButton>(R.id.imageButton_ChatMuro_NuevoMensaje)

        botonrecargar.setOnClickListener{
            val uidUsuarioLog = FirebaseAuth.getInstance().uid
            val referenceRecompensa = FirebaseDatabase.getInstance().getReference("/recompensas/$uidUsuarioLog").push()
            val crearrecompensa = Registrarse.Recompensa(
                referenceRecompensa.key!!,
                uidUsuarioLog.toString(),
                "Hay un curioso en el equipo",
                System.currentTimeMillis() / 1000
            )
            referenceRecompensa.setValue(crearrecompensa)
            Toast.makeText( baseContext,"Logro Desbloqueado: Hay un curioso en el equipo", Toast.LENGTH_SHORT).show()

        }

        recyclerViewListaMensajes.layoutManager = LinearLayoutManager(this)

        recyclerViewListaMensajes.adapter = adapter

        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
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


        val menunavigationChat: NavigationView = findViewById(R.id.navigationChatMuro)
        menunavigationChat.setNavigationItemSelectedListener { menuseleccionado ->

            when (menuseleccionado.itemId) {

                R.id.opc_Muro_Publicacion -> {
                    Log.d("EncriptarMenu","Entra en el menu publicacion")
                    val intent = Intent(this,MuroPublicacion::class.java)
                    intent.putExtra(USER_KEY, chatName)
                    startActivity(intent)
                }
                R.id.opc_Muro_Imagen -> {
                    Log.d("EncriptarMenu","Entra en el menu imagen")
                    val intent = Intent(this,MuroImagen::class.java)
                    intent.putExtra(USER_KEY, chatName)
                    startActivity(intent)
                }
                R.id.opc_Muro_Archivo -> {
                    Log.d("EncriptarMenu","Entra en el menu archivos")
                    val intent = Intent(this,MuroArchivo::class.java)
                    intent.putExtra(USER_KEY, chatName)
                    startActivity(intent)

                }
                R.id.opc_Muro_Ubicacion->{
                    Log.d("EncriptarMenu","Entra en el menu ubicacion")
                    val intent = Intent(this,MuroUbicacion::class.java)
                    intent.putExtra(USER_KEY, chatName)
                    startActivity(intent)
                }

            }
            true

        }

        escuchaMensajesPublicaciones()

    }


    lateinit var selectedArchivoUri : Uri

    private fun escuchaMensajesPublicaciones() {

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        val toId = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/muro/chat/$fromId/$toId")
        Log.d("PruebaMensajeCarpeta", "personas ${fromId} y ${toId}")


        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1:String?){
                val recyclerViewListaMensajes = findViewById<RecyclerView>(R.id.recyclerView_ChatMuro_UltimosMensajes)

                    val publicacionMuro = p0.getValue(MuroChat::class.java)
                    if (publicacionMuro != null) {

                        val publicacionID = publicacionMuro.tipoId

                        //Toast.makeText( baseContext,"Entra a verificar tipo ${publicacionMuro.tipo}", Toast.LENGTH_SHORT).show()

                        if (publicacionMuro.tipo == "publicacion") {

                            escuchaPublicaciones(publicacionID,fromId.toString(),toId.toString())

                        }

                        if (publicacionMuro.tipo == "imagen") {

                            escuchaImagenes(publicacionID,fromId.toString(),toId.toString())

                        }

                        if (publicacionMuro.tipo == "archivo") {

                            escuchaArchivo(publicacionID,fromId.toString(),toId.toString())

                        }

                        if (publicacionMuro.tipo == "ubicacion") {

                            escuchaUbicacion(publicacionID,fromId.toString(),toId.toString())

                        }


                    }



                    recyclerViewListaMensajes.scrollToPosition(adapter.itemCount - 1)
                }
            override fun onCancelled(p0: DatabaseError){}
            override fun onChildChanged(p0: DataSnapshot,p1: String?){}
            override fun onChildMoved(p0: DataSnapshot, p1:String?){}
            override fun onChildRemoved(p0: DataSnapshot){}

        })


    }

    @Parcelize
    class MuroChat(val id: String, val tipoId: String, val tipo: String, val fromId: String, val toId: String, val timestamp: Long): Parcelable {
        constructor():this("","","","","",-1)
    }

    private fun escuchaPublicaciones(publicacionId: String, fromId: String,toId:String){

        val reference = FirebaseDatabase.getInstance().getReference("/muropublicacion/chat/$fromId/$toId/$publicacionId")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                val publicacion = p0.getValue(MuroPublicacion.PublicacionClassMuro::class.java)
                if (publicacion != null) {
                    val publiID = publicacion.id

                    //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                    if (publiID == publicacionId) {

                        adapter.add(
                            PublicacionIteam(
                                publicacion,
                                toId.toString()
                            )
                        )

                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {}
        })

    }

    private fun escuchaImagenes(publicacionId: String, fromId: String,toId:String){

        //Toast.makeText( baseContext,"Entra a verificar ${publicacionId}", Toast.LENGTH_SHORT).show()

        val reference = FirebaseDatabase.getInstance().getReference("/muroimagen/chat/$fromId/$toId/$publicacionId")


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

               // Toast.makeText( baseContext,"Entra a antes de verificar", Toast.LENGTH_SHORT).show()

                val publicacion = p0.getValue(MuroImagen.ImagenClassMuro::class.java)
                if (publicacion != null) {
                    val publiID = publicacion.id

                    //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                    if (publiID == publicacionId) {

                        adapter.add(
                            ImagenIteam(
                                publicacion,
                                toId.toString()
                            )
                        )

                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText( baseContext,"Presenta error", Toast.LENGTH_SHORT).show()
            }
        })


    }

    private fun escuchaArchivo(publicacionId: String, fromId: String,toId:String){

        //Toast.makeText( baseContext,"Entra a verificar ${publicacionId}", Toast.LENGTH_SHORT).show()

        val reference = FirebaseDatabase.getInstance().getReference("/muroarchivo/chat/$fromId/$toId/$publicacionId")


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                // Toast.makeText( baseContext,"Entra a antes de verificar", Toast.LENGTH_SHORT).show()

                val archivo = p0.getValue(MuroArchivo.ArchivoClassMuro::class.java)
                if (archivo != null) {
                    val publiID = archivo.id

                    //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                    if (publiID == publicacionId) {

                        adapter.add(
                            ArchivoIteam(
                                archivo,
                                toId.toString()
                            )
                        )

                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText( baseContext,"Presenta error", Toast.LENGTH_SHORT).show()
            }
        })

        adapter.setOnItemClickListener { item, view ->

            //onCheckboxClickedEstado(view.checkBox_layout_PagiPrin_EstadoConexion, view.textView_layout_PagPrin_EstadoConexion)

            //Toast.makeText(baseContext,"Entra aqui",Toast.LENGTH_SHORT).show()


            var View = view.imageView_layout_archivo

            if (View is ImageView) {

               // Toast.makeText(baseContext,"Entra al if",Toast.LENGTH_SHORT).show()

                when (View.id) {
                    R.id.imageView_layout_archivo -> {

                      //  Toast.makeText(baseContext,"Entra al imageButton",Toast.LENGTH_SHORT).show()

                        val referenceArchivo = FirebaseDatabase.getInstance()
                            .getReference("/muroarchivo/chat/$fromId/$toId/$publicacionId")

                        referenceArchivo.addListenerForSingleValueEvent(object :
                            ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot) {

                                val archivo = p0.getValue(MuroArchivo.ArchivoClassMuro::class.java)

                               // Toast.makeText(baseContext,"Entra al ${archivo!!.nombreArchivo}",Toast.LENGTH_SHORT).show()

                                if (archivo != null) {
                                    val publiID = archivo.id

                                    if (publiID == publicacionId) {

                                        val archivoUrl = archivo.archivoUrl

                                        val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText(label.toString(), archivoUrl)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(baseContext,"Se copio al portapapeles: ${archivo.nombreArchivo}",Toast.LENGTH_SHORT).show()

                                        val uriUrl = Uri.parse(archivoUrl)
                                        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
                                        startActivity(launchBrowser)

                                    }
                                }


                            }

                            override fun onCancelled(p0: DatabaseError) {}
                        })
                    }
                }


            }




        }


    }

    private fun escuchaUbicacion(publicacionId: String, fromId: String,toId:String){

        //Toast.makeText( baseContext,"Entra a verificar ${publicacionId}", Toast.LENGTH_SHORT).show()

        val reference = FirebaseDatabase.getInstance().getReference("/muroubicacion/chat/$fromId/$toId/$publicacionId")


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                // Toast.makeText( baseContext,"Entra a antes de verificar", Toast.LENGTH_SHORT).show()

                val ubicacion = p0.getValue(MuroUbicacion.UbicacionClassMuro::class.java)
                if (ubicacion != null) {
                    val publiID = ubicacion.id

                    //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                    if (publiID == publicacionId) {

                        adapter.add(
                            UbicacionIteam(
                                ubicacion,
                                toId.toString()
                            )
                        )

                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText( baseContext,"Presenta error", Toast.LENGTH_SHORT).show()
            }
        })

        adapter.setOnItemClickListener { item, view ->

            //onCheckboxClickedEstado(view.checkBox_layout_PagiPrin_EstadoConexion, view.textView_layout_PagPrin_EstadoConexion)

            //Toast.makeText(baseContext,"Entra aqui",Toast.LENGTH_SHORT).show()


            var View = view.imageView_layout_ubicacion

            if (View is ImageView) {

                //Toast.makeText(baseContext,"Entra al if",Toast.LENGTH_SHORT).show()

                when (View.id) {
                    R.id.imageView_layout_ubicacion ->{

                        val referenceArchivo = FirebaseDatabase.getInstance()
                            .getReference("/muroubicacion/chat/$fromId/$toId/$publicacionId")

                        referenceArchivo.addListenerForSingleValueEvent(object :
                            ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot) {

                                val ubicacion = p0.getValue(MuroUbicacion.UbicacionClassMuro::class.java)
                                if (ubicacion != null) {
                                    val publiID = ubicacion.id

                                    if (publiID == publicacionId) {

                                        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
                                        if (chatName != null) {
                                            Log.d("gruporecibimiento","$chatName")
                                            // Toast.makeText( baseContext,"me recibo $chatName", Toast.LENGTH_SHORT).show()
                                        }

                                        val intent = Intent(this@ChatMuro,MapaView::class.java)
                                        intent.putExtra(USER_KEY, chatName)
                                        intent.putExtra(USER_KEYChatMuro, ubicacion)
                                        startActivity(intent)



                                    }
                                }


                            }

                            override fun onCancelled(p0: DatabaseError) {}
                        })
                    }

                }


            }





        }


    }


}


class PublicacionIteam(val publicacion: MuroPublicacion.PublicacionClassMuro, val usuarioToId: String): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId

        if (fromId == publicacion.fromId){
            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroTexto_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroTexto_Usuario)
                        Picasso.get().load(uri).into(targetImageView)

                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })

        }
        else {
            val ref = FirebaseDatabase.getInstance().getReference("/users/$toId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroTexto_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroTexto_Usuario)
                        Picasso.get().load(uri).into(targetImageView)
                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }



        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroTexto_texto).text=publicacion.text

    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_texto
    }


}


class ImagenIteam(val imagen: MuroImagen.ImagenClassMuro, val usuarioToId: String): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId

        if (fromId == imagen.fromId){
            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroImagen_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroImagen_Usuario)
                        Picasso.get().load(uri).into(targetImageView)

                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })

        }
        else {
            val ref = FirebaseDatabase.getInstance().getReference("/users/$toId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroImagen_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroImagen_Usuario)
                        Picasso.get().load(uri).into(targetImageView)
                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }



        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroImagen_texto).text=imagen.text

        val uri = imagen.imagenUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroImagen_imagen)
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_imagen
    }


}


class ArchivoIteam(val archivo: MuroArchivo.ArchivoClassMuro, val usuarioToId: String): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId

        if (fromId == archivo.fromId){
            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroArchivo_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroArchivo_Usuario)
                        Picasso.get().load(uri).into(targetImageView)

                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })

        }
        else {
            val ref = FirebaseDatabase.getInstance().getReference("/users/$toId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroArchivo_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroArchivo_Usuario)
                        Picasso.get().load(uri).into(targetImageView)
                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }



        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroArchivo_texto).text=archivo.text
        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroArchivo_Archivo).text=archivo.nombreArchivo



    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_archivo
    }


}


class UbicacionIteam(val ubicacion: MuroUbicacion.UbicacionClassMuro, val usuarioToId: String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId

        if (fromId == ubicacion.fromId){
            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroUbicacion_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroUbicacion_Usuario)
                        Picasso.get().load(uri).into(targetImageView)

                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })

        }
        else {
            val ref = FirebaseDatabase.getInstance().getReference("/users/$toId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {
                        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroUbicacion_Usuario).text=user.username

                        val uri = user.profileImageUrl
                        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroUbicacion_Usuario)
                        Picasso.get().load(uri).into(targetImageView)
                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }


        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroUbicacion_texto).text=ubicacion.text




    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_ubicacion
    }



}





