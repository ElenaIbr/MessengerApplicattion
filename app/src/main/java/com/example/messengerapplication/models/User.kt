package com.example.messengerapplication.models

data class User(
    val id: String = "",
    var username: String = "",
    var bio: String = "",
    var fullname: String = "",
    var state: String = "В сети",
    var phone: String = "",
    var photoUrl: String = "")