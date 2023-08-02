package com.example.mixnchat.data

data class Users(
    val username : String ?= null,
    val biography : String ?= null,
    val profileURL : String ?= null,
    val userUid : String ?= null
) {
}