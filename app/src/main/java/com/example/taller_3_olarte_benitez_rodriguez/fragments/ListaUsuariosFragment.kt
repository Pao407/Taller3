package com.example.taller_3_olarte_benitez_rodriguez.fragments

import android.database.Cursor
import android.database.MatrixCursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.taller_3_olarte_benitez_rodriguez.R
import com.example.taller_3_olarte_benitez_rodriguez.adapters.UsuarioAdapter
import com.example.taller_3_olarte_benitez_rodriguez.databinding.FragmentListaUsuariosBinding
import com.example.taller_3_olarte_benitez_rodriguez.model.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import java.io.File
import java.util.UUID
import kotlin.math.log


class ListaUsuariosFragment : Fragment() {
    private var _binding: FragmentListaUsuariosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentListaUsuariosBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpListView()
    }

    private fun loadContacts(onDataLoaded: (Cursor) -> Unit){
        val usuarios = mutableListOf<User>()
        val cursor = MatrixCursor(arrayOf("_id", "name", "uri", "uid"))
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.get().addOnSuccessListener {
            val children = it.children
            children.forEach { child ->
                val user = child.getValue(User::class.java)
                if (user != null && user.uid != currentUserId) {
                    val localFile = File.createTempFile("img${UUID.randomUUID()}", "jpg")
                    val storageRef = Firebase.storage.reference.child("images/${user.uid}")

                    storageRef.getFile(localFile).addOnSuccessListener {
                        Log.d("ListaUsuariosFragment", "Imagen descargada")
                        cursor.addRow(arrayOf(1, user.name, localFile.absolutePath.toString(), user.uid))
                    }.addOnFailureListener {
                        Log.e("ListaUsuariosFragment", "Error al descargar imagen de ${user.name}", it)
                    }
                }
            }
            onDataLoaded(cursor)
        }
    }

    private fun setUpListView() {
        loadContacts { cursor ->
            val adapter = UsuarioAdapter(requireContext(), cursor, 0)
            binding.listViewUsuarios.adapter = adapter

            binding.listViewUsuarios.setOnItemClickListener { _, _, position, _ ->
                cursor.moveToPosition(position)
                Toast.makeText(requireContext(), "Este es un contacto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}