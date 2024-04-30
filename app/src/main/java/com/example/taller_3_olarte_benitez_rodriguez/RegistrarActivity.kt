package com.example.taller_3_olarte_benitez_rodriguez

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

class RegistrarActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var ivImage: ImageView
    private lateinit var preferences: SharedPreferences
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        ivImage.setImageURI(uri)
    }
    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val seleccionarImagenButton = findViewById<Button>(R.id.buttonImgenPerfil)
        seleccionarImagenButton.setOnClickListener {
            showMediaOptions()
        }

        val registrarButton = findViewById<Button>(R.id.buttonRegistrarse)
        registrarButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextTextEmailAddress2).text.toString()
            val password = findViewById<EditText>(R.id.editTextTextPassword2).text.toString()
            val nombre = findViewById<EditText>(R.id.editTextNombre).text.toString()
            val apellido = findViewById<EditText>(R.id.editTextApellido).text.toString()
            val identificacion = findViewById<EditText>(R.id.editTextNumberIdentificacion).text.toString()
            val latitud = findViewById<EditText>(R.id.editTextNumberDecimalLatitud).text.toString()
            val longitud = findViewById<EditText>(R.id.editTextNumberDecimalLongitud).text.toString()

            if (isEmailValid(email) && isPasswordValid(password) && isNameValid(nombre) && isLastNameValid(apellido) && isIdValid(identificacion) && isLatitudeValid(latitud) && isLongitudeValid(longitud)) {
                registrar(nombre,apellido,email,password,identificacion,latitud,longitud)
            } else {
                Toast.makeText(this, "Por favor, verifica los datos ingresados", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun registrar(nombre: String, apellido: String,email: String,password: String, identificacion: String, latitud: String, longitud: String) {
        // Registrar el usuario con Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    uploadImageAndSaveUser(user?.uid, nombre, apellido, identificacion, latitud, longitud)
                } else {
                    Toast.makeText(baseContext, "Authenticacion fallo.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageAndSaveUser(uid: String?, nombre: String, apellido: String, identificacion: String, latitud: String, longitud: String) {

        val user=User()
        user.uid=uid.toString()
        user.name=nombre
        user.apellido=apellido
        user.id=identificacion
        user.latitud=latitud
        user.longitud=longitud

        val key = myRef.push().key
        myRef = database.getReference(PATH_USERS + key)
        myRef.setValue(user)

    }


    //funcion para seleccionar imagen de la galeria o tomar una foto
    private fun showMediaOptions() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una opción")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if (checkCameraPermission()) {
                        openCamera()
                    } else {
                        requestCameraPermission()
                    }
                }
                1 -> pickMedia.launch("image/*")
            }
        }
        builder.show()
    }
    //funcion para verificar si la aplicacion tiene permisos para acceder a la camara
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    //funcion para solicitar permisos para acceder a la camara
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE)
    }
//funcion para abrir la camara
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }
//funcion para solicitar permisos para acceder a la camara
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Log.i("aris", "Permiso de cámara denegado.")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            saveImageToGallery(imageBitmap)
            ivImage.setImageBitmap(imageBitmap)
        }
    }
//funcion para guardar la imagen en la galeria
    private fun saveImageToGallery(bitmap: Bitmap) {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val imageFile = File(imagesDir, "image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        MediaScannerConnection.scanFile(this, arrayOf(imageFile.absolutePath), null, null)

        // Guardar la ruta de la imagen en SharedPreferences
        val editor = preferences.edit()
        editor.putString("imagePath", imageFile.absolutePath)
        editor.apply()
    }


    //validar que el email tenga un formato correcto
    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }
    //validar que la contraseña tenga al menos 6 caracteres sino muestre un mensaje de error
    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    //validar que el nombre no este vacio sino muestre un mensaje de error
    private fun isNameValid(name: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacio", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    //validar que el apellido no este vacio sino muestre un mensaje de error
    private fun isLastNameValid(lastName: String): Boolean {
        if (lastName.isEmpty()) {
            Toast.makeText(this, "El apellido no puede estar vacio", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    //validar numero de identificacion no este vacio sino muestre un mensaje de error
    private fun isIdValid(id: String): Boolean {
        if (id.isEmpty()) {
            Toast.makeText(this, "El numero de identificacion no puede estar vacio", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    //validar latitud no este vacio sino muestre un mensaje de error
    private fun isLatitudeValid(latitude: String): Boolean {
        if (latitude.isEmpty()) {
            Toast.makeText(this, "La latitud no puede estar vacia", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    //validar longitud no este vacio sino muestre un mensaje de error
    private fun isLongitudeValid(longitude: String): Boolean {
        if (longitude.isEmpty()) {
            Toast.makeText(this, "La longitud no puede estar vacia", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        const val PATH_USERS="users/"
    }


}