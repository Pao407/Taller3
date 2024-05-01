package com.example.taller_3_olarte_benitez_rodriguez.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller_3_olarte_benitez_rodriguez.R
import com.example.taller_3_olarte_benitez_rodriguez.databinding.FragmentInteresBinding
import com.example.taller_3_olarte_benitez_rodriguez.model.Ubicaciones
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
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
    private var _binding: FragmentInteresBinding? = null
    private val binding get() = _binding!!
    private lateinit var puntosDeInteres: List<GeoPoint>
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var osmMap: MapView
    private var marker: Marker? = null

    // Variables para Google Play Services
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment with binding
        _binding = FragmentInteresBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        osmMap = binding.osmMap
        osmMap.setMultiTouchControls(true)
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

            val mlocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val mlocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val myLocation = GeoPoint(location)
                    Toast.makeText(requireContext(), "Location changed: $myLocation", Toast.LENGTH_SHORT).show()
                    actualizarUbicacionEnBaseDeDatos(myLocation) // Actualiza la ubicación en la base de datos
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                    // Implement your code here if needed
                }

                override fun onProviderEnabled(provider: String) {
                    // Implement your code here if needed
                }

                override fun onProviderDisabled(provider: String) {
                    // Log or show a message when the provider is disabled
                    Log.d("LocationListener", "Provider disabled: $provider")
                }
            }

            mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, mlocationListener)

            mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, mlocationListener)
        }
    }

    private fun agregarMarcadorUbicacionActual(ubicacion: GeoPoint) {
        Log.i("map", "Se actuaizo")
    }

    private fun actualizarUbicacionEnBaseDeDatos(ubicacion: GeoPoint) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid // Obtiene el ID del usuario que inició sesión

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users/$userId/latitud")
            // Set latitud y longitud
            myRef.setValue(ubicacion.latitude)
            val myRef2 = database.getReference("users/$userId/longitud")
            myRef2.setValue(ubicacion.longitude)

            Toast.makeText(requireContext(), "Ubicación actualizada", Toast.LENGTH_SHORT).show()
        } else {
            Log.w("Firebase", "No hay un usuario autenticado.")
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}