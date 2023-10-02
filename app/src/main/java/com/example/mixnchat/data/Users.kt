package com.example.mixnchat.data

data class Users(
    val username : String ?= null,
    val biography : String ?= null,
    val profileUrl : String ?= null,
    val userUid : String ?= null,
    val token : String ?= null
) {
}
