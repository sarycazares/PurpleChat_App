package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.ejemplo.purplechat.NuevoMensaje.Companion.USER_KEY
import com.ejemplo.purplechat.NuevoMensajeEquipos.Companion.USER_KEYE
import com.ejemplo.purplechat.NuevoMensajeGrupos.Companion.USER_KEYG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.parcel.Parcelize

class MuroPublicacion : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muro_publicacion)


        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
        if (chatName != null) {
            Log.d("gruporecibimiento", "$chatName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        if (groupName != null) {
            Log.d("gruporecibimiento", "$groupName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        if (equipoName != null) {
            Log.d("gruporecibimiento", "$equipoName")
            //Toast.makeText( baseContext,"me recibo $groupName", Toast.LENGTH_SHORT).show()
        }

        val imageButtonFlechaRegresar =
            findViewById<ImageButton>(R.id.imageButton_MuroPublicacion_FlechaRegresar)
        val editText_texto =
            findViewById<EditText>(R.id.editTextTextMultiLine_MuroPublicacion_Publicacion)
        val botonCrearPublicacion =
            findViewById<Button>(R.id.button_MuroPublicacion_CrearPublicacion)

        imageButtonFlechaRegresar.setOnClickListener {

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

            if (groupName != null && equipoName != null) {
                val intent = Intent(this, EquipoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                intent.putExtra(USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }

        botonCrearPublicacion.setOnClickListener {

            if (chatName != null) {

                val texto = editText_texto.text.toString()

                if (texto.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Favor de llenar los campos necesarios",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                guardadoPublicacionChatDataBase()

                val intent = Intent(this, ChatMuro::class.java)
                intent.putExtra(USER_KEY, chatName)
                startActivity(intent)
                finish()
            }

            if (groupName != null) {

                val texto = editText_texto.text.toString()

                if (texto.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Favor de llenar los campos necesarios",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                guardadoPublicacionGrupoDataBase()

                val intent = Intent(this, GrupoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                startActivity(intent)
                finish()
            }

            if (groupName != null && equipoName != null) {

                val texto = editText_texto.text.toString()

                if (texto.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Favor de llenar los campos necesarios",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }

                guardadoPublicacionEquipoDataBase()

                val intent = Intent(this, EquipoMuro::class.java)
                intent.putExtra(USER_KEYG, groupName)
                intent.putExtra(USER_KEYE, equipoName)
                startActivity(intent)
                finish()
            }

        }

    }

    private fun guardadoPublicacionChatDataBase() {

        val editText_Texto =
            findViewById<EditText>(R.id.editTextTextMultiLine_MuroPublicacion_Publicacion).text
        val refUsuarioLoggeado = FirebaseAuth.getInstance().uid

        val chatName = intent.getParcelableExtra<Registrarse.User>(USER_KEY)
        val toId = chatName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muropublicacion/chat/$refUsuarioLoggeado/$toId").push()
        val crearPublicacion = PublicacionClassMuro(
            reference.key!!,
            editText_Texto.toString(),
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        reference.setValue(crearPublicacion)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muropublicacion/chat/$toId/$refUsuarioLoggeado").push()
        val crearPublicacionToId = PublicacionClassMuro(
            reference.key!!,
            editText_Texto.toString(),
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        refererence.setValue(crearPublicacionToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$refUsuarioLoggeado/$toId")
                .push()
        val crearMuroPublicacion = ChatMuro.MuroChat(
            ref.key!!,
            reference.key!!,
            "publicacion",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        ref.setValue(crearMuroPublicacion)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/chat/$toId/$refUsuarioLoggeado")
                .push()
        val crearMuroPublicacionToId = ChatMuro.MuroChat(
            ref.key!!,
            reference.key!!,
            "publicacion",
            refUsuarioLoggeado.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        referencia.setValue(crearMuroPublicacionToId)

    }

    private fun guardadoPublicacionGrupoDataBase() {

        val editText_Texto =
            findViewById<EditText>(R.id.editTextTextMultiLine_MuroPublicacion_Publicacion).text
        val fromId = FirebaseAuth.getInstance().uid

        val groupName = intent.getParcelableExtra<Group>(USER_KEYG)
        val toId = groupName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muropublicacion/grupo/$fromId/$toId").push()
        val crearPublicacion = PublicacionClassMuro(
            reference.key!!,
            editText_Texto.toString(),
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        reference.setValue(crearPublicacion)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muropublicacion/grupo/$toId").push()
        val crearPublicacionToId = PublicacionClassMuro(
            reference.key!!,
            editText_Texto.toString(),
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        refererence.setValue(crearPublicacionToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$fromId/$toId")
                .push()
        val crearMuroPublicacion = GrupoMuro.MuroGrupo(
            ref.key!!,
            reference.key!!,
            "publicacion",
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        ref.setValue(crearMuroPublicacion)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/grupo/$toId")
                .push()
        val crearMuroPublicacionToId = GrupoMuro.MuroGrupo(
            ref.key!!,
            reference.key!!,
            "publicacion",
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        referencia.setValue(crearMuroPublicacionToId)

    }

    private fun guardadoPublicacionEquipoDataBase() {

        val editText_Texto =
            findViewById<EditText>(R.id.editTextTextMultiLine_MuroPublicacion_Publicacion).text
        val fromId = FirebaseAuth.getInstance().uid

        val equipoName = intent.getParcelableExtra<Equipo>(USER_KEYE)
        val toId = equipoName!!.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/muropublicacion/equipo/$fromId/$toId").push()
        val crearPublicacion = PublicacionClassMuro(
            reference.key!!,
            editText_Texto.toString(),
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        reference.setValue(crearPublicacion)

        val refererence = FirebaseDatabase.getInstance()
            .getReference("/muropublicacion/equipo/$toId").push()
        val crearPublicacionToId = PublicacionClassMuro(
            reference.key!!,
            editText_Texto.toString(),
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        refererence.setValue(crearPublicacionToId)

        val ref =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$fromId/$toId")
                .push()
        val crearMuroPublicacion = EquipoMuro.MuroEquipo(
            ref.key!!,
            reference.key!!,
            "publicacion",
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        ref.setValue(crearMuroPublicacion)

        val referencia =
            FirebaseDatabase.getInstance().getReference("/muro/equipo/$toId")
                .push()
        val crearMuroPublicacionToId = EquipoMuro.MuroEquipo(
            ref.key!!,
            reference.key!!,
            "publicacion",
            fromId.toString(),
            toId,
            System.currentTimeMillis() / 1000
        )
        referencia.setValue(crearMuroPublicacionToId)

    }


    @Parcelize
    class PublicacionClassMuro(val id: String, val text: String, val fromId: String, val toId: String, val timestamp: Long) : Parcelable {
        constructor() : this("", "", "", "", -1)
    }


}



