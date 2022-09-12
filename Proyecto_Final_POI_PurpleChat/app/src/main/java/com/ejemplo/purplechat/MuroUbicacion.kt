package com.ejemplo.purplechat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ejemplo.purplechat.NuevoMensaje.Companion.USER_KEY
import com.ejemplo.purplechat.NuevoMensajeEquipos.Companion.USER_KEYE
import com.ejemplo.purplechat.NuevoMensajeGrupos.Companion.USER_KEYG
import com.ejemplo.purplechat.databinding.ActivityMapaViewBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.LocationManager
import android.os.Parcelable
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.parcel.Parcelize


class MuroUbicacion : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap
    var longitude : Double = 0.0
    var latitude : Double = 0.0

    var ubicacionTomada = false

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muro_ubicacion)

        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
        if (chatName != null) {
            Log.d("gruporecibimiento","$chatName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento","$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        if (equipoName != null) {
            Log.d("gruporecibimiento","$equipoName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val imageButtonFlechaRegresar=findViewById<ImageButton>(R.id.imageButton_MuroUbicacion_FlechaRegresar)
        val botonCrearUbicacion = findViewById<Button>(R.id.button_MuroUbicacion_CrearPublicacion)

        imageButtonFlechaRegresar.setOnClickListener{

            if (chatName != null) {
                val intent = Intent(this, ChatMuro::class.java)
                intent.putExtra(USER_KEY, chatName)
                startActivity(intent)
                finish()
            }

            if (groupName != null) {
                val intent = Intent(this, GrupoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                startActivity(intent)
                finish()
            }

            if(groupName != null && equipoName != null){
                val intent = Intent(this,EquipoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                intent.putExtra(USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }

        botonCrearUbicacion.setOnClickListener {

            if (chatName != null) {


                if ( ubicacionTomada == false) {

                    Toast.makeText(
                        this,
                        "Favor de seleccionar tu ubicación",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                guardadoUbicacionChatDataBase()

                val intent = Intent(this, ChatMuro::class.java)
                intent.putExtra(USER_KEY, chatName)
                startActivity(intent)
                finish()
            }

            if (groupName != null) {


                if ( ubicacionTomada == false) {

                    Toast.makeText(
                        this,
                        "Favor de seleccionar tu ubicación",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                guardadoUbicacionGrupoDataBase()

                val intent = Intent(this, GrupoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                startActivity(intent)
                finish()
            }

            if (groupName != null && equipoName != null) {


                if ( ubicacionTomada == false) {

                    Toast.makeText(
                        this,
                        "Favor de seleccionar tu ubicación",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                guardadoUbicacionEquipoDataBase()

                val intent = Intent(this,EquipoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                intent.putExtra(USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }


        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView_MuroUbicacion_Mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val lugar = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(lugar).title("Tu ubicación"))
       // mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lugar,18f),4000,null)
        mMap.setOnMyLocationClickListener(this)
        enableMyLocation()
    }

    private fun isPermissionsGranted() = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::mMap.isInitialized) return
        if (isPermissionsGranted()) {
            mMap.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    private var locationManager : LocationManager? = null

    @SuppressLint("MissingPermission", "MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                mMap.isMyLocationEnabled = true

                val locationManager: LocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers: List<String> = locationManager.getProviders(true)
                var location: Location? = null
                for (i in providers.size - 1 downTo 0) {
                    location= locationManager.getLastKnownLocation(providers[i])
                    if (location != null)
                        break
                }

            }else{
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onMyLocationClick(p0: Location) {
        longitude = p0.longitude
        latitude = p0.latitude
        val lugar = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(lugar).title("Tu ubicación"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lugar,18f),4000,null)
        Toast.makeText(this, "Ubicacion Tomada", Toast.LENGTH_SHORT).show()
        ubicacionTomada = true
    }


    private fun guardadoUbicacionChatDataBase() {

        val editText_Texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroUbicacion_Publicacion).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
        val toId = chatName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroubicacion/chat/$refUsuarioLoggeado/$toId").push()
        val crearubicacion = UbicacionClassMuro(reference.key!!,editText_Texto.toString(), latitude, longitude,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(crearubicacion)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroubicacion/chat/$toId/$refUsuarioLoggeado").push()
        val crearubicacionToId = UbicacionClassMuro(reference.key!!,editText_Texto.toString(), latitude, longitude,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(crearubicacionToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroubicacion = ChatMuro.MuroChat(ref.key!!,reference.key!!,"ubicacion", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        ref.setValue(crearMuroubicacion)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$toId/$refUsuarioLoggeado")
                .push()
        val crearMuroubicacionToId = ChatMuro.MuroChat(ref.key!!,reference.key!!,"ubicacion", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        referencia.setValue(crearMuroubicacionToId)

    }

    private fun guardadoUbicacionGrupoDataBase() {

        val editText_Texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroUbicacion_Publicacion).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        val toId = groupName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroubicacion/grupo/$refUsuarioLoggeado/$toId").push()
        val crearubicacion = UbicacionClassMuro(reference.key!!,editText_Texto.toString(), latitude, longitude,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(crearubicacion)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroubicacion/grupo/$toId").push()
        val crearubicacionToId = UbicacionClassMuro(reference.key!!,editText_Texto.toString(), latitude, longitude,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(crearubicacionToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroubicacion = GrupoMuro.MuroGrupo(ref.key!!,reference.key!!,"ubicacion", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        ref.setValue(crearMuroubicacion)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$toId")
                .push()
        val crearMuroubicacionToId = GrupoMuro.MuroGrupo(ref.key!!,reference.key!!,"ubicacion", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        referencia.setValue(crearMuroubicacionToId)

    }

    private fun guardadoUbicacionEquipoDataBase() {

        val editText_Texto = findViewById<EditText>(R.id.editTextTextMultiLine_MuroUbicacion_Publicacion).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        val toId = equipoName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muroubicacion/equipo/$refUsuarioLoggeado/$toId").push()
        val crearubicacion = UbicacionClassMuro(reference.key!!,editText_Texto.toString(), latitude, longitude,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        reference.setValue(crearubicacion)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muroubicacion/equipo/$toId").push()
        val crearubicacionToId = UbicacionClassMuro(reference.key!!,editText_Texto.toString(), latitude, longitude,refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        refererence.setValue(crearubicacionToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroubicacion = EquipoMuro.MuroEquipo(ref.key!!,reference.key!!,"ubicacion", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        ref.setValue(crearMuroubicacion)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$toId")
                .push()
        val crearMuroubicacionToId = EquipoMuro.MuroEquipo(ref.key!!,reference.key!!,"ubicacion", refUsuarioLoggeado.toString(),toId,System.currentTimeMillis() / 1000)
        referencia.setValue(crearMuroubicacionToId)

    }

    @Parcelize
    class UbicacionClassMuro(val id: String,val text: String,val latitud: Double,val longitud: Double,val fromId: String,val toId: String,val timestamp: Long) :
        Parcelable {
        constructor() : this("","", 0.0,0.0, "", "", -1)
    }


}