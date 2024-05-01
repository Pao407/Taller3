package com.example.taller_3_olarte_benitez_rodriguez.services

import android.app.Service
import android.content.Intent
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

class UserAvailabilityService : Service() {
    private lateinit var databaseReference: DatabaseReference

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                handleUserAvailabilityChange(snapshot, userId)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                handleUserAvailabilityChange(snapshot, userId)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error al obtener datos de la base de datos", Toast.LENGTH_SHORT).show()
            }
        }

        databaseReference.addChildEventListener(childEventListener)

        return START_STICKY
    }

    private fun handleUserAvailabilityChange(snapshot: DataSnapshot, userId: String?) {
        if (snapshot.key != userId) {
            val availability = snapshot.child("estado").getValue(String::class.java)
            val mainHandler = Handler(Looper.getMainLooper())

            if (availability == "Disponible") {
                val userName = snapshot.child("name").getValue(String::class.java) ?: "Desconocido"
                mainHandler.post {
                    Toast.makeText(applicationContext, "$userName está disponible", Toast.LENGTH_SHORT).show()

                    val intent = Intent("com.example.taller_3_olarte_benitez_rodriguez.USER_AVAILABILITY_CHANGED")
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            } else {
                mainHandler.post {
                    val intent = Intent("com.example.taller_3_olarte_benitez_rodriguez.USER_AVAILABILITY_CHANGED")
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                }
            }

        }
    }
}
