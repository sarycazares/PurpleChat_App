package com.ejemplo.purplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val botonRegistrarse=findViewById<Button>(R.id.btn_bienvenida_Registrarme)
        val botonIniciarSesion=findViewById<Button>(R.id.btn_bienvenida_IniciarSesion)



        botonRegistrarse.setOnClickListener{
            val intent = Intent(this,Registrarse::class.java)
            startActivity(intent)
        }

        botonIniciarSesion.setOnClickListener{
            val intent = Intent(this,IniciarSesion::class.java)
            startActivity(intent)
        }

    }

}