package com.ejemplo.purplechat

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class IniciarSesion : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar_sesion)



        auth = Firebase.auth

        val botonCancelar=findViewById<Button>(R.id.btn_IniSes_Cancelar)
        val botonIniciarSesion=findViewById<Button>(R.id.btn_IniSes_IniciarSesion)
        val textviewYaCuenta=findViewById<TextView>(R.id.textView_IniSes_YaCuenta)

        val editText_Correo= findViewById<EditText>(R.id.editText_IniSes_CorreoElectronico)
        val correo = editText_Correo.text

        val editText_Contraseña= findViewById<EditText>(R.id.editText_IniSes_Contraseña)
        val contraseña = editText_Contraseña.text



        botonCancelar.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        botonIniciarSesion.setOnClickListener{

            val email = editText_Correo.text.toString()
            val password = editText_Contraseña.text.toString()

            if (email.isEmpty() || password.isEmpty() ) {

                Toast.makeText(this, "Favor de llenar los campos necesarios", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        val intent = Intent(this,PaginaPrincipal::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }


        }

        textviewYaCuenta.setOnClickListener{
            val intent = Intent(this,Registrarse::class.java)
            startActivity(intent)
            finish()
        }
    }
}