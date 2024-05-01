package com.example.taller_3_olarte_benitez_rodriguez.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.MatrixCursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
    private var isAdded = false

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
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(userAvailabilityReceiver,
            IntentFilter("com.example.taller_3_olarte_benitez_rodriguez.USER_AVAILABILITY_CHANGED"))
        isAdded = true
    }

    private fun loadContacts(onDataLoaded: (Cursor) -> Unit){
        val cursor = MatrixCursor(arrayOf("_id", "name", "uid"))
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        usersRef.get().addOnSuccessListener {
            val children = it.children
            children.forEach { child ->
                val user = child.getValue(User::class.java)
                if (user != null && user.uid != currentUserId && user.estado == "Disponible") {
                    cursor.addRow(arrayOf(user.id, user.name, user.uid))
                }
            }
            onDataLoaded(cursor)
        }
    }

    private fun setUpListView() {
        loadContacts { cursor ->
            val adapter = UsuarioAdapter(requireContext(), cursor, 0)
            binding.listViewUsuarios.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    private val userAvailabilityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Update the fragment
            setUpListView()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(userAvailabilityReceiver,
                IntentFilter("com.example.taller_3_olarte_benitez_rodriguez.USER_AVAILABILITY_CHANGED"))
        }
    }

    override fun onPause() {
        super.onPause()
        if (isAdded) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(userAvailabilityReceiver)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}