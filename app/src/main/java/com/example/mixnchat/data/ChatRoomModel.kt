package com.example.mixnchat.data

import com.google.firebase.Timestamp



class ChatRoomModel(
    val chatRoomId: String = "",
    val userIds: List<String> = emptyList(),
    var lastMessageTimeStamp: Timestamp ?= null,
    var lastMessageSenderId: String = "",
    var lastMessage : String = ""
) {
    constructor() : this("", emptyList(), null, "","")
}