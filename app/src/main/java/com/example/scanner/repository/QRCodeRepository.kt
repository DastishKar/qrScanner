package com.example.scanner.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class QRCodeRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun getQRCodeReference(): DatabaseReference {
        return database.child("qrcodes")
    }
}
