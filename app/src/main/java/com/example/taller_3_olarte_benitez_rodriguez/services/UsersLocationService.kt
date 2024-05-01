package com.example.taller_3_olarte_benitez_rodriguez.services

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.taller_3_olarte_benitez_rodriguez.activities.MainActivity
import com.example.taller_3_olarte_benitez_rodriguez.activities.MenuActivity
import com.example.taller_3_olarte_benitez_rodriguez.fragments.ListaUsuariosFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UsersLocationService : Service() {
    private lateinit var databaseReferenceLat: DatabaseReference
    private lateinit var databaseReferenceLon: DatabaseReference

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Get string uid from intent
        val uid = intent?.getStringExtra("uid")
        databaseReferenceLat = FirebaseDatabase.getInstance().getReference("users/$uid/latitud")
        databaseReferenceLon = FirebaseDatabase.getInstance().getReference("users/$uid/longitud")


        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                handleUserLocationChange(snapshot, userId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                handleUserLocationChange(snapshot, userId)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error al obtener datos de la base de datos", Toast.LENGTH_SHORT).show()
            }
        }

        databaseReferenceLat.addChildEventListener(childEventListener)
        databaseReferenceLon.addChildEventListener(childEventListener)

        return START_STICKY
    }

    private fun handleUserLocationChange(snapshot: DataSnapshot, userId: String?) {
        if (snapshot.key != userId) {
            val mainHandler = Handler(Looper.getMainLooper())

            mainHandler.post {
                //
            }
        }
    }
}
