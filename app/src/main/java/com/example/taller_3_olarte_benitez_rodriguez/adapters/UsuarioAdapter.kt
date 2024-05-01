package com.example.taller_3_olarte_benitez_rodriguez.adapters

import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.taller_3_olarte_benitez_rodriguez.R

class UsuarioAdapter(context: Context?, c: Cursor?, flags: Int) : CursorAdapter(context, c, flags) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return LayoutInflater.from(context)
            .inflate(R.layout.layout_usuarios, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        val imgUsario = view?.findViewById<ImageView>(R.id.imagenUsuarioLista)
        val nomUsuario = view?.findViewById<TextView>(R.id.nombreUsuarioLista)

        nomUsuario?.text = cursor?.getString(1)
        val uriStr = cursor?.getString(2)
        val uri = uriStr?.let { android.net.Uri.parse(it) }
        imgUsario?.setImageURI(uri)
    }
}