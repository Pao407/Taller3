package com.example.taller_3_olarte_benitez_rodriguez.model

data class User (
    var email: String = "",
    var password: String = "",
    var name: String = "",
    var apellido: String = "",
    var latitud: String = "",
    var longitud: String = "",
    var uid: String = "",
    var id: String = "",
    var image: String = "",
    var estado: String = "Disponible"
);