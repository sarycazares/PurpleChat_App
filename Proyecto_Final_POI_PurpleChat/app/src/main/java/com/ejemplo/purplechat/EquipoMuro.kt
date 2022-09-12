package com.ejemplo.purplechat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.layout_muro_archivo.view.*
import kotlinx.android.synthetic.main.layout_muro_ubicacion.view.*

class EquipoMuro : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    companion object {
        val USER_KEYEquipoMuro = "USER_KEYChatMuro"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equipo_muro)

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_EquipoMuro_FlechaRegresar)
        val recyclerViewListaMensajes=findViewById<RecyclerView>(R.id.recyclerView_EquipoMuro_UltimosMensajes)

        recyclerViewListaMensajes.layoutManager = LinearLayoutManager(this)

        recyclerViewListaMensajes.adapter = adapter

        val groupName = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        if (equipoName != null) {
            Log.d("gruporecibimiento","$equipoName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        imageButtonFlechaRegresar.setOnClickListener{
            val intent = Intent(this,EquipoMensaje::class.java)
            intent.putExtra(USER_KEYG, groupName)
            intent.putExtra(USER_KEYE, equipoName)
            startActivity(intent)
            finish()
        }

        val botonrecargar=findViewById<ImageButton>(R.id.imageButton_EquipoMuro_NuevoMensaje)

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

        val menunavigationEquipo: NavigationView = findViewById(R.id.navigationEquipoMuro)
        menunavigationEquipo.setNavigationItemSelectedListener { menuseleccionado ->

            when (menuseleccionado.itemId) {

                R.id.opc_Muro_Publicacion -> {
                    Log.d("EncriptarMenu","Entra en el menu publicacion")
                    val intent = Intent(this,MuroPublicacion::class.java)
                    intent.putExtra(USER_KEYG, groupName)
                    intent.putExtra(USER_KEYE, equipoName)
                    startActivity(intent)
                }
                R.id.opc_Muro_Imagen -> {
                    Log.d("EncriptarMenu","Entra en el menu imagen")
                    val intent = Intent(this,MuroImagen::class.java)
                    intent.putExtra(USER_KEYG, groupName)
                    intent.putExtra(USER_KEYE, equipoName)
                    startActivity(intent)
                }
                R.id.opc_Muro_Archivo -> {
                    Log.d("EncriptarMenu","Entra en el menu archivos")
                    val intent = Intent(this,MuroArchivo::class.java)
                    intent.putExtra(USER_KEYG, groupName)
                    intent.putExtra(USER_KEYE, equipoName)
                    startActivity(intent)

                }
                R.id.opc_Muro_Ubicacion->{
                    Log.d("EncriptarMenu","Entra en el menu ubicacion")
                    val intent = Intent(this,MuroUbicacion::class.java)
                    intent.putExtra(USER_KEYG, groupName)
                    intent.putExtra(USER_KEYE, equipoName)
                    startActivity(intent)
                }

            }
            true

        }

        escuchaMensajesPublicaciones()

    }

    private fun escuchaMensajesPublicaciones() {

        val equipoName = intent.getParcelableExtra<Equipo>(NuevoMensajeEquipos.USER_KEYE)
        val fromId = equipoName!!.uid
        val toId = FirebaseAuth.getInstance().uid

        val ref = FirebaseDatabase.getInstance().getReference("/muro/equipo/$fromId")


        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1:String?){
                val recyclerViewListaMensajes = findViewById<RecyclerView>(R.id.recyclerView_EquipoMuro_UltimosMensajes)

                val publicacionMuro = p0.getValue(MuroEquipo::class.java)
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
            override fun onChildChanged(p0: DataSnapshot, p1: String?){}
            override fun onChildMoved(p0: DataSnapshot, p1:String?){}
            override fun onChildRemoved(p0: DataSnapshot){}

        })


    }

    lateinit var selectedArchivoUri : Uri

    private fun escuchaPublicaciones(publicacionId: String, fromId: String,toId:String){

        val reference = FirebaseDatabase.getInstance().getReference("/muropublicacion/equipo/$fromId")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                p0.children.forEach {
                    val publicacion = it.getValue(MuroPublicacion.PublicacionClassMuro::class.java)
                    if (publicacion != null) {
                        val publiID = publicacion.id

                        //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                        if (publiID == publicacionId) {

                            adapter.add(
                                PublicacionIteamEquipo(
                                    publicacion,
                                    toId.toString()
                                )
                            )

                        }
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    private fun escuchaImagenes(publicacionId: String, fromId: String,toId:String){

        //Toast.makeText( baseContext,"Entra a verificar ${publicacionId}", Toast.LENGTH_SHORT).show()

        val reference = FirebaseDatabase.getInstance().getReference("/muroimagen/equipo/$fromId")


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                // Toast.makeText( baseContext,"Entra a antes de verificar", Toast.LENGTH_SHORT).show()
                p0.children.forEach {
                    val publicacion = it.getValue(MuroImagen.ImagenClassMuro::class.java)
                    if (publicacion != null) {
                        val publiID = publicacion.id

                        //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                        if (publiID == publicacionId) {

                            adapter.add(
                                ImagenIteamEquipo(
                                    publicacion,
                                    toId.toString()
                                )
                            )

                        }
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

        val reference = FirebaseDatabase.getInstance().getReference("/muroarchivo/equipo/$fromId")


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                // Toast.makeText( baseContext,"Entra a antes de verificar", Toast.LENGTH_SHORT).show()

                p0.children.forEach {
                    val archivo = it.getValue(MuroArchivo.ArchivoClassMuro::class.java)
                    if (archivo != null) {
                        val publiID = archivo.id

                        //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                        if (publiID == publicacionId) {

                            adapter.add(
                                ArchivoIteamEquipo(
                                    archivo,
                                    toId.toString()
                                )
                            )

                        }
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


            val View = view.imageButton_LayoutMuroArchivo_Archivo
            if (View is ImageButton) {

                //Toast.makeText(baseContext,"Entra al if",Toast.LENGTH_SHORT).show()

                when (View.id) {
                    R.id.imageButton_LayoutMuroArchivo_Archivo -> {

                        //Toast.makeText(baseContext,"Entra al imageButton",Toast.LENGTH_SHORT).show()

                        val referenceArchivo = FirebaseDatabase.getInstance()
                            .getReference("/muroarchivo/chat/$fromId/$toId")

                        referenceArchivo.addListenerForSingleValueEvent(object :
                            ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot) {

                                p0.children.forEach {

                                    val archivo =
                                        it.getValue(MuroArchivo.ArchivoClassMuro::class.java)
                                    if (archivo != null) {
                                        val publiID = archivo.id

                                        if (publiID == publicacionId) {

                                            val archivoUrl = archivo.archivoUrl
                                            //Toast.makeText(baseContext,"Archivo ${archivoUrl}",Toast.LENGTH_SHORT).show()

                                            val clipboard: ClipboardManager =
                                                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText(
                                                android.R.attr.label.toString(),
                                                archivoUrl
                                            )
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(
                                                baseContext,
                                                "Se copio al portapapeles: ${archivo.nombreArchivo}",
                                                Toast.LENGTH_SHORT
                                            ).show()


                                            val uriUrl = Uri.parse(archivoUrl)
                                            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
                                            startActivity(launchBrowser)

                                        }
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

        val reference = FirebaseDatabase.getInstance().getReference("/muroubicacion/equipo/$fromId")


        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                // Toast.makeText( baseContext,"Entra a antes de verificar", Toast.LENGTH_SHORT).show()

                p0.children.forEach {
                    val ubicacion = it.getValue(MuroUbicacion.UbicacionClassMuro::class.java)
                    if (ubicacion != null) {
                        val publiID = ubicacion.id

                        //Toast.makeText( baseContext,"Entra a verificar ${publicacion.text}", Toast.LENGTH_SHORT).show()

                        if (publiID == publicacionId) {

                            adapter.add(
                                UbicacionIteamEquipo(
                                    ubicacion,
                                    toId.toString()
                                )
                            )

                        }
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText( baseContext,"Presenta error", Toast.LENGTH_SHORT).show()
            }
        })

        adapter.setOnItemClickListener { item, view ->


            val View = view.imageView_LayoutMuroUbicacion_Mapa
            if (View is ImageView) {

                //Toast.makeText(baseContext,"Entra al if",Toast.LENGTH_SHORT).show()

                when (View.id) {
                    R.id.imageView_LayoutMuroUbicacion_Mapa -> {

                        //Toast.makeText(baseContext,"Entra al imageButton",Toast.LENGTH_SHORT).show()

                        val referenceArchivo = FirebaseDatabase.getInstance()
                            .getReference("/muroubicacion/equipo/$fromId")

                        referenceArchivo.addListenerForSingleValueEvent(object :
                            ValueEventListener {

                            override fun onDataChange(p0: DataSnapshot) {

                                p0.children.forEach {
                                    val ubicacion =
                                        it.getValue(MuroUbicacion.UbicacionClassMuro::class.java)
                                    if (ubicacion != null) {
                                        val publiID = ubicacion.id

                                        if (publiID == publicacionId) {

                                            val groupName =
                                                intent.getParcelableExtra<Group>(USER_KEYG)
                                            if (groupName != null) {
                                                Log.d("gruporecibimiento", "$groupName")
                                                //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
                                            }

                                            val equipoName =
                                                intent.getParcelableExtra<Equipo>(USER_KEYE)
                                            if (equipoName != null) {
                                                Log.d("gruporecibimiento", "$equipoName")
                                                //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
                                            }

                                            val intent =
                                                Intent(this@EquipoMuro, MapaView::class.java)
                                            intent.putExtra(USER_KEYG, groupName)
                                            intent.putExtra(USER_KEYE, equipoName)
                                            intent.putExtra(USER_KEYEquipoMuro, ubicacion)
                                            startActivity(intent)


                                        }
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

    @Parcelize
    class MuroEquipo(val id: String, val tipoId: String, val tipo: String, val fromId: String, val toId: String, val timestamp: Long):
        Parcelable {
        constructor():this("","","","","",-1)
    }

}


class PublicacionIteamEquipo(val publicacion: MuroPublicacion.PublicacionClassMuro, val usuarioToId: String): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId


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



        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroTexto_texto).text=publicacion.text

    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_texto
    }


}


class ImagenIteamEquipo(val imagen: MuroImagen.ImagenClassMuro, val usuarioToId: String): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId


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



        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroImagen_texto).text=imagen.text

        val uri = imagen.imagenUrl
        val targetImageView = viewHolder.itemView.findViewById<ImageView>(R.id.imageView_LayoutMuroImagen_imagen)
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_imagen
    }


}


class ArchivoIteamEquipo(val archivo: MuroArchivo.ArchivoClassMuro, val usuarioToId: String): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId


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



        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroArchivo_texto).text=archivo.text
        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroArchivo_Archivo).text=archivo.nombreArchivo



    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_archivo
    }


}


class UbicacionIteamEquipo(val ubicacion: MuroUbicacion.UbicacionClassMuro, val usuarioToId: String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int){

        val fromId = FirebaseAuth.getInstance().uid
        val toId = usuarioToId


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


        viewHolder.itemView.findViewById<TextView>(R.id.textView_LayoutMuroUbicacion_texto).text=ubicacion.text




    }

    override fun getLayout(): Int{
        return R.layout.layout_muro_ubicacion
    }



}
