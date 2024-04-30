package com.example.taller_3_olarte_benitez_rodriguez

class User {
    var email: String = ""
    var password: String = ""
    var name: String = ""
    var apellido: String = ""
    var latitud: String = ""
    var longitud: String = ""
    var uid: String = ""
    var id: String = ""

    constructor(email: String, password: String, name: String, latitud: String, longitud: String, uid: String, id: String, apellido: String) {
        this.email = email
        this.password = password
        this.name = name
        this.latitud = latitud
        this.longitud = longitud
        this.uid = uid
        this.id = id
        this.apellido = apellido
    }

    constructor() {}

}