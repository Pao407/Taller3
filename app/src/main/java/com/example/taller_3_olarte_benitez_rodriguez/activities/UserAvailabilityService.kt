package com.example.taller_3_olarte_benitez_rodriguez.activities

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
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
            if (availability == "Disponible") {
                val userName = snapshot.child("name").getValue(String::class.java) ?: "Desconocido"
                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.post {
                    Toast.makeText(applicationContext, "$userName está disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
