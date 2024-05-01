package com.example.taller_3_olarte_benitez_rodriguez.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller_3_olarte_benitez_rodriguez.R
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

class InteresFragment : Fragment() {
    private lateinit var _binding: FragmentInteresBinding
    private lateinit var puntosDeInteres: List<GeoPoint>
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var osmMap: MapView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment with binding
        _binding = FragmentInteresBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        osmMap = _binding.osmMap
        Configuration.getInstance().userAgentValue = "com.example.taller_3_olarte_benitez_rodriguez"

        cargarPuntosDeInteresDesdeJSON()
        mostrarPuntosDeInteresEnMapa()
        iniciarOverlayUbicacion()
    }

    private fun cargarPuntosDeInteresDesdeJSON() {
        val json: String = try {
            // Lee el archivo JSON desde la carpeta "assets"
            val inputStream = requireActivity().assets.open("locations.json")
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

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
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