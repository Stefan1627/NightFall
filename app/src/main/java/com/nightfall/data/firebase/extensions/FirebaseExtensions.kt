package com.nightfall.data.firebase.extensions

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun DatabaseReference.setValueSuspend(value: Any?) {
    suspendCancellableCoroutine<Unit> { continuation ->
        setValue(value)
            .addOnSuccessListener { continuation.resume(Unit) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}

suspend fun DatabaseReference.removeValueSuspend() {
    suspendCancellableCoroutine<Unit> { continuation ->
        removeValue()
            .addOnSuccessListener { continuation.resume(Unit) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}

suspend fun DatabaseReference.updateChildrenSuspend(values: Map<String, Any?>) {
    suspendCancellableCoroutine<Unit> { continuation ->
        updateChildren(values)
            .addOnSuccessListener { continuation.resume(Unit) }
            .addOnFailureListener { continuation.resumeWithException(it) }
    }
}