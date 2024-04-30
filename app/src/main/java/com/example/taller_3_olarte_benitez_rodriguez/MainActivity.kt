package com.example.taller_3_olarte_benitez_rodriguez

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Firebase Auth
        auth = Firebase.auth

        iniciar()

        val buttonRegistrarse = findViewById<Button>(R.id.buttonRegistrarse)
        buttonRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistrarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }

    fun iniciar(){
        val emailField = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordField = findViewById<EditText>(R.id.editTextTextPassword)
        val butonIniciar = findViewById<Button>(R.id.buttonIniciar)

        butonIniciar.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                if (isEmailValid(email)) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // si la autenticacion es exitosa, pasar a la siguiente actividad
                                Log.d(TAG, "signInWithEmail:success")
                                val user = auth.currentUser
                                val intent = Intent(this, Iniciar::class.java)
                                startActivity(intent)
                            } else {
                                // si falla la autenticacion, mostrar un mensaje al usuario
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext, "Autenticacion fallida",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        baseContext, "Por favor ingrese un correo válido.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    baseContext, "Por favor ingrese un correo y una contraseña.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}