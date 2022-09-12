package com.ejemplo.purplechat

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*


class Registrarse : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    var imagenCreada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)


       auth = Firebase.auth

        val botonCancelar=findViewById<Button>(R.id.btn_registrarme_cancelar)
        val botonRegistrarse=findViewById<Button>(R.id.btn_registrarse_registrarme)
        val textviewYaCuenta=findViewById<TextView>(R.id.textView_Reg_YaCuenta)

        val editText_Usuario= findViewById<EditText>(R.id.editText_Reg_Usuario)
        val editText_CorreoElectronico = findViewById<EditText>(R.id.editText_Reg_CorreoElectronico)
        val editText_Contraseña= findViewById<EditText>(R.id.editText_Reg_Contraseña)

        val boton_SeleccionFoto= findViewById<Button>(R.id.button_Reg_SelectorPhoto)

        botonCancelar.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        botonRegistrarse.setOnClickListener {
            //val message = "Usuario: ${usuario},Email: ${correo}, Contraseña: ${contraseña}"
            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            val usuario = editText_Usuario.text.toString()
            val email = editText_CorreoElectronico.text.toString()
            val password = editText_Contraseña.text.toString()

            if (email.isEmpty() || password.isEmpty() || usuario.isEmpty() || imagenCreada == false) {

                Toast.makeText(this, "Favor de llenar los campos necesarios", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if(password.length < 6) {
                Toast.makeText(this, "Favor de crear una contraseña mayor a 6 digitos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)

                        subirImagenStorage()




                        val intent = Intent(this,IniciarSesion::class.java)
                        Toast.makeText(this, "Usuario creado con éxito ¡Yei!", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }


        }

        textviewYaCuenta.setOnClickListener{
            val intent = Intent(this,IniciarSesion::class.java)
            startActivity(intent)
            finish()
        }

        boton_SeleccionFoto.setOnClickListener{

            Log.d("Registrarse", " Selecciono boton de foto")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)

        }

    }


    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val boton_SeleccionPhoto = findViewById<Button>(R.id.button_Reg_SelectorPhoto)
        val circle_View = findViewById<CircleImageView>(R.id.circle_Reg_PhotoView)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null)
        {
            Log.d("Registrarse","Se selecciono foto")
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circle_View.setImageBitmap(bitmap)
            boton_SeleccionPhoto.alpha = 0f
            imagenCreada = true

            //val bitmapDrawable = BitmapDrawable(bitmap)
            //boton_SeleccionPhoto.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun updateUI(user: FirebaseUser?) {

    }

    private fun subirImagenStorage(){

        if(selectedPhotoUri == null){
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!).addOnSuccessListener{
            Log.d("Registrarse","Successfully uploaded image:${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener{
                Log.d("Registrarse","File Location: $it")
                guardadoUsuariosDataBase(it.toString())
            }
        }

    }



    private fun guardadoUsuariosDataBase(profileImageUrl: String){

        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val editText_Usuario= findViewById<EditText>(R.id.editText_Reg_Usuario)
        val estadoConexion = "Desconectado"
        val user = User(uid, editText_Usuario.text.toString(),estadoConexion,profileImageUrl)

        val referenceRecompensa = FirebaseDatabase.getInstance().getReference("/recompensas/$uid").push()
        val crearrecompensa = Recompensa(referenceRecompensa.key!!,uid,"Logro: Me registre",System.currentTimeMillis() / 1000)
        referenceRecompensa.setValue(crearrecompensa)
        Toast.makeText( baseContext,"Logro Desbloqueado: Me registre", Toast.LENGTH_SHORT).show()

        Log.d("Registrarse","Si estoy en la base de datos")

        ref.setValue(user)


    }


    @Parcelize
    class User(val uid: String, val username: String, val estadoConexion:String, val profileImageUrl: String): Parcelable {
        constructor(): this("","","","")
    }

    @Parcelize
    class Recompensa(val id: String, val userId: String, val recompensa:String, val timestamp: Long): Parcelable {
        constructor(): this("","","",-1)
    }
}