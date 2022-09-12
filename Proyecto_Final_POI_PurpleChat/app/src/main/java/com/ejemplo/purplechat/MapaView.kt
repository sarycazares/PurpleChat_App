package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import com.ejemplo.purplechat.ChatMuro.Companion.USER_KEYChatMuro
import com.ejemplo.purplechat.EquipoMuro.Companion.USER_KEYEquipoMuro
import com.ejemplo.purplechat.GrupoMuro.Companion.USER_KEYGrupoMuro


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso


class MapaView : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa_view)

        val chatName = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        if (chatName != null) {
            Log.d("gruporecibimiento","$chatName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val groupName = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val equipoName = intent.getParcelableExtra<Equipo>(NuevoMensajeEquipos.USER_KEYE)
        if (equipoName != null) {
            Log.d("gruporecibimiento","$equipoName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }


        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_Map_FlechaRegresar)
        val botonrecargar=findViewById<ImageButton>(R.id.imageButton_Map_NuevoMensaje)

        botonrecargar.setOnClickListener{
            val uidUsuarioLog = FirebaseAuth.getInstance().uid
            val referenceRecompensa = FirebaseDatabase.getInstance().getReference("/recompensas/$uidUsuarioLog").push()
            val crearrecompensa = Registrarse.Recompensa(
                referenceRecompensa.key!!,
                uidUsuarioLog.toString(),
                "Con que eres un curioso",
                System.currentTimeMillis() / 1000
            )
            referenceRecompensa.setValue(crearrecompensa)
            Toast.makeText( baseContext,"Logro Desbloqueado: ¡Oh no! No puedes encriptar :c", Toast.LENGTH_SHORT).show()

        }

        imageButtonFlechaRegresar.setOnClickListener{

            if (chatName != null) {
                val intent = Intent(this, ChatMuro::class.java)
                intent.putExtra(NuevoMensaje.USER_KEY, chatName)
                startActivity(intent)
                finish()
            }

            if (groupName != null) {
                val intent = Intent(this, GrupoMuro::class.java)
                intent.putExtra(NuevoMensajeGrupos.USER_KEYG, groupName)
                startActivity(intent)
                finish()
            }

            if(groupName != null && equipoName != null){
                val intent = Intent(this,EquipoMuro::class.java)
                intent.putExtra(NuevoMensajeGrupos.USER_KEYG, groupName)
                intent.putExtra(NuevoMensajeEquipos.USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }


        botonrecargar.setOnClickListener{
            val uidUsuarioLog = FirebaseAuth.getInstance().uid
            val referenceRecompensa = FirebaseDatabase.getInstance().getReference("/recompensas/$uidUsuarioLog").push()
            val crearrecompensa = Registrarse.Recompensa(
                referenceRecompensa.key!!,
                uidUsuarioLog.toString(),
                "Con que eres un curioso",
                System.currentTimeMillis() / 1000
            )
            referenceRecompensa.setValue(crearrecompensa)
            Toast.makeText( baseContext,"Logro Desbloqueado: ¡Oh no! No puedes encriptar :c", Toast.LENGTH_SHORT).show()

        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val chatName = intent.getParcelableExtra<Registrarse.User>(NuevoMensaje.USER_KEY)
        if (chatName != null) {
            val ubicacionActual = intent.getParcelableExtra<MuroUbicacion.UbicacionClassMuro>(USER_KEYChatMuro)
            val fromId = ubicacionActual!!.fromId

            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {

                        val sydney = LatLng(ubicacionActual.latitud, ubicacionActual.longitud)
                        val userNombre = user.username
                        mMap.addMarker(MarkerOptions().position(sydney).title(userNombre))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,18f),4000,null)
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }

        val groupName = intent.getParcelableExtra<Group>(NuevoMensajeGrupos.USER_KEYG)
        if (groupName != null) {
            val ubicacionActual = intent.getParcelableExtra<MuroUbicacion.UbicacionClassMuro>(USER_KEYGrupoMuro)
            val fromId = ubicacionActual!!.fromId

            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {

                        val sydney = LatLng(ubicacionActual.latitud, ubicacionActual.longitud)
                        val userNombre = user.username
                        mMap.addMarker(MarkerOptions().position(sydney).title(userNombre))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,18f),4000,null)
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }

        val equipoName = intent.getParcelableExtra<Equipo>(NuevoMensajeEquipos.USER_KEYE)
        if (groupName != null && equipoName != null) {
            val ubicacionActual = intent.getParcelableExtra<MuroUbicacion.UbicacionClassMuro>(USER_KEYEquipoMuro)
            val fromId = ubicacionActual!!.fromId

            val ref = FirebaseDatabase.getInstance().getReference("/users/$fromId")

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {

                    val user = p0.getValue(Registrarse.User::class.java)

                    if (user != null) {

                        val sydney = LatLng(ubicacionActual.latitud, ubicacionActual.longitud)
                        val userNombre = user.username
                        mMap.addMarker(MarkerOptions().position(sydney).title(userNombre))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,18f),4000,null)
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

                    }

                }

                override fun onCancelled(p0: DatabaseError) {}
            })
        }




    }
}