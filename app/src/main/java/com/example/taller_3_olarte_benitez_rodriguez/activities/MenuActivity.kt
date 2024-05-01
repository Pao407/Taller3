package com.example.taller_3_olarte_benitez_rodriguez.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.taller_3_olarte_benitez_rodriguez.R
import com.example.taller_3_olarte_benitez_rodriguez.databinding.ActivityMenuBinding
import com.example.taller_3_olarte_benitez_rodriguez.fragments.InteresFragment
import com.example.taller_3_olarte_benitez_rodriguez.fragments.ListaUsuariosFragment
import com.example.taller_3_olarte_benitez_rodriguez.services.UserAvailabilityService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var fragmentManager: FragmentManager

    companion object {
        var menuActivity: MenuActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        menuActivity = this

        setUpFragment()
        openFragment(InteresFragment())
        // Iniciar el servicio
        val serviceIntent = Intent(this, UserAvailabilityService::class.java)
        startService(serviceIntent)
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
                val usuariosFragment = ListaUsuariosFragment()
                openFragment(usuariosFragment, "ListaUsuariosFragment")
                return true
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }

    private fun openFragment(fragment: Fragment, tag: String = "") {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainerView, fragment, tag)
        fragmentTransaction.commit()
    }
}