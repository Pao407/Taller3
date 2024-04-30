package com.example.taller_3_olarte_benitez_rodriguez.model

data class Ubicaciones(
    val locations: Map<String, Ubicacion>,
    val locationsArray: List<Ubicacion>
)