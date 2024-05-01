package com.example.taller_3_olarte_benitez_rodriguez.adapters

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.taller_3_olarte_benitez_rodriguez.R
import com.example.taller_3_olarte_benitez_rodriguez.activities.MapLocateActivity
import com.example.taller_3_olarte_benitez_rodriguez.activities.MenuActivity
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import java.io.File
import java.util.UUID

class UsuarioAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {
    companion object {
        private var restart = true;
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_usuarios, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val uid = cursor?.getString(2) ?: ""
        val name = cursor?.getString(1) ?: ""

        loadImage(uid) { file ->
            val imgUsario = view?.findViewById<ImageView>(R.id.imagenUsuarioLista)
            val nomUsuario = view?.findViewById<TextView>(R.id.nombreUsuarioLista)
            val button = view?.findViewById<Button>(R.id.botonLocalizar)

            nomUsuario?.text = name
            Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
            val uri = Uri.fromFile(file)
            imgUsario?.setImageURI(uri)

            button?.setOnClickListener {
                // Change screen
                val intent = Intent(context, MapLocateActivity::class.java)
                intent.putExtra("uid", uid)
                if (context != null) {
                    startActivity(context, intent, null)
                }
            }
        }
    }

    private fun loadImage(uid: String, onDataLoaded: (File) -> Unit) {
        val localFile = File.createTempFile("img${UUID.randomUUID()}", "jpg")
        val storageRef = Firebase.storage.reference.child("images/${uid}")

        storageRef.getFile(localFile).addOnSuccessListener {
            Log.d("ListaUsuariosFragment", "Imagen descargada")
            onDataLoaded(localFile)
        }
    }
}