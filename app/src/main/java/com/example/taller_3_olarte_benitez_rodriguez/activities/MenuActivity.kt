package com.example.taller_3_olarte_benitez_rodriguez.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.taller_3_olarte_benitez_rodriguez.R
import com.example.taller_3_olarte_benitez_rodriguez.databinding.ActivityMenuBinding
import com.example.taller_3_olarte_benitez_rodriguez.fragments.InteresFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var fragmentManager: FragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpFragment()
        openFragment(InteresFragment())
    }

    private fun setUpFragment() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.setOnItemSelectedListener {item ->
            onNavigationItemSelected(item)
        }
        fragmentManager = supportFragmentManager
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val interesFragment = InteresFragment()
                openFragment(interesFragment)
                return true
            }
            R.id.nav_disponibilidad -> {
                // Obtener valor de estado del usuario actual
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("users/$userId/estado")

                myRef.get().addOnSuccessListener {
                    val value = it.value
                    val nuevoValor = if (value == "Disponible") "Desconectado" else "Disponible"

                    // Cambiar texto del item de menu
                    item.title = nuevoValor

                    // Actualizar valor de estado del usuario actual
                    myRef.setValue(nuevoValor).addOnSuccessListener {
                        Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar el estado del usuario", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Error al obtener el estado del usuario", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.nav_usuarios -> {
                //val usuariosFragment = NotificationsFragment()
                //fragmentManager.beginTransaction().replace(R.id.fragmentContainerView, notificationsFragment).commit()
                return true
            }
        }
        return false
    }

    private fun openFragment(fragment: Fragment) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
        fragmentTransaction.commit()
    }
}