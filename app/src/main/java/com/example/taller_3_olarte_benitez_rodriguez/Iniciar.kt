package com.example.taller_3_olarte_benitez_rodriguez
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException
import java.nio.charset.Charset

class Iniciar : AppCompatActivity() {

    private lateinit var puntosDeInteres: List<GeoPoint>
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var osmMap: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciar)
        osmMap = findViewById(R.id.osmMap)
        Configuration.getInstance().userAgentValue = "com.example.taller_3_olarte_benitez_rodriguez"

        cargarPuntosDeInteresDesdeJSON()
        mostrarPuntosDeInteresEnMapa()
        iniciarOverlayUbicacion()
    }

    private fun cargarPuntosDeInteresDesdeJSON() {
        val json: String = try {
            // Lee el archivo JSON desde la carpeta "assets"
            val inputStream = assets.open("locations.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        val gson = Gson()
        val data = gson.fromJson(json, Ubicaciones::class.java)
        puntosDeInteres = data.locationsArray.map { GeoPoint(it.latitude, it.longitude) }.toList()
    }

    private fun mostrarPuntosDeInteresEnMapa() {
        puntosDeInteres.forEach { punto ->
            val marker = Marker(osmMap)
            marker.position = punto
            marker.title = "Punto de interés"
            osmMap.overlays.add(marker)
        }
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

data class Ubicacion(
    val latitude: Double,
    val longitude: Double,
    val name: String
)

data class Ubicaciones(
    val locations: Map<String, Ubicacion>,
    val locationsArray: List<Ubicacion>
)