package com.nightfall.core.session

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class SessionManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid
    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null
}
