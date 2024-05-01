package com.example.taller_3_olarte_benitez_rodriguez.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller_3_olarte_benitez_rodriguez.R
import com.example.taller_3_olarte_benitez_rodriguez.databinding.ActivityMapLocateBinding
import com.example.taller_3_olarte_benitez_rodriguez.databinding.FragmentInteresBinding
import com.example.taller_3_olarte_benitez_rodriguez.model.Ubicaciones
import com.google.gson.Gson
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.nio.charset.Charset

class MapLocateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapLocateBinding
    private lateinit var puntosDeInteres: List<GeoPoint>
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var osmMap: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapLocateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        osmMap = binding.osmMap
        osmMap.setMultiTouchControls(true)
        Configuration.getInstance().userAgentValue = "com.example.taller_3_olarte_benitez_rodriguez"

        iniciarOverlayUbicacion()
    }

    override fun onResume() {
        super.onResume()
        osmMap.onResume()
        val mapController: IMapController = osmMap.controller
        mapController.setZoom(18.0)
        mapController.setCenter(puntosDeInteres[0])
    }

    override fun onPause() {
        super.onPause()
        osmMap.onPause()
    }
    private fun iniciarOverlayUbicacion() {
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
            mLocationOverlay.runOnFirstFix {
                val location = mLocationOverlay.myLocation
                location?.let {
                    val myLocation = GeoPoint(it)
                    agregarMarcadorUbicacionActual(myLocation)
                }
            }
        }
    }

    private fun agregarMarcadorUbicacionActual(ubicacion: GeoPoint) {
        val marker = Marker(osmMap)
        osmMap.overlays.add(marker)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}