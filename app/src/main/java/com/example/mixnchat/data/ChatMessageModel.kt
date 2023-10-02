package com.example.mixnchat.data

import com.google.firebase.Timestamp

class ChatMessageModel(
    var message: String? = null,
    var senderId: String? = null,
    var timestamp: Timestamp? = null,
    var seen : Boolean ?= false,
    var receiverId : String ?= null

    ) {
    constructor() : this("", "", null,false, "")
}