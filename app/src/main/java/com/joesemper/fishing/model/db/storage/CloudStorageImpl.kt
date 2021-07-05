package com.joesemper.fishing.model.db.storage

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CloudStorageImpl: Storage {

    private val storage = Firebase.storage

    private var storageRef = storage.reference
}