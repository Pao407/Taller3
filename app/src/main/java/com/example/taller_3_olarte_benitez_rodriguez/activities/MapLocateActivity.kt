package com.example.taller_3_olarte_benitez_rodriguez.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.taller_3_olarte_benitez_rodriguez.databinding.ActivityMapLocateBinding
import com.example.taller_3_olarte_benitez_rodriguez.services.UserAvailabilityService
import com.google.firebase.database.FirebaseDatabase
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapLocateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapLocateBinding
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var osmMap: MapView
    private var destino: Marker? = null
    private var inicial: Marker? = null
    private var isAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapLocateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        osmMap = binding.osmMap
        osmMap.setMultiTouchControls(true)
        Configuration.getInstance().userAgentValue = "com.example.taller_3_olarte_benitez_rodriguez"

        iniciarOverlayUbicacion(intent.getStringExtra("uid").toString())
        drawLine()
        // Iniciar el
    }

    private fun drawLine() {
        val polyline = org.osmdroid.views.overlay.Polyline()
        val puntos = ArrayList<GeoPoint>()
        puntos.add(destino!!.position)
        puntos.add(inicial!!.position)
        polyline.setPoints(puntos)
        osmMap.overlays.add(polyline)
    }

    override fun onResume() {
        super.onResume()
        osmMap.onResume()
        val mapController: IMapController = osmMap.controller
        mapController.setZoom(18.0)
    }

    override fun onPause() {
        super.onPause()
        osmMap.onPause()
    }
    private fun iniciarOverlayUbicacion(uid: String) {
        mLocationOverlay = MyLocationNewOverlay(osmMap)
        osmMap.overlays.add(mLocationOverlay)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            mLocationOverlay.enableMyLocation()
            mLocationOverlay.enableFollowLocation()

            obtenerUbicacion(uid) { ubicacion ->
                marcadorInicial(mLocationOverlay.myLocation)
                marcadorFinal(ubicacion)
            }
        }
    }

    private fun obtenerUbicacion(uid: String, onDataLoaded: (GeoPoint) -> Unit) {
        // Obtain from firebase database
        val databaseReference = FirebaseDatabase.getInstance().getReference("users/$uid/")

        databaseReference.get().addOnSuccessListener {
            val latitud = it.child("latitud").getValue(String::class.java)
            val longitud = it.child("longitud").getValue(String::class.java)
            val ubicacion = GeoPoint(latitud!!.toDouble(), longitud!!.toDouble())

            onDataLoaded(ubicacion)
        }
    }

    private fun marcadorInicial(ubicacion: GeoPoint) {
        inicial?.let { osmMap.overlays.remove(it) }
        inicial = Marker(osmMap)
        inicial!!.position = ubicacion
        osmMap.overlays.add(inicial)
    }

    private fun marcadorFinal(ubicacion: GeoPoint) {
        destino?.let { osmMap.overlays.remove(it) }
        destino = Marker(osmMap)
        destino!!.position = ubicacion
        osmMap.overlays.add(destino)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}