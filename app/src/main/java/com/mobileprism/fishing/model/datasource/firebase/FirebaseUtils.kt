package com.mobileprism.fishing.model.datasource.firebase

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

const val CATCHES_COLLECTION = "catches"

fun Context.getGoogleLoginAuth(): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .requestProfile()
        .build()
    return GoogleSignIn.getClient(this, gso)
}

fun startFirebaseLogin(
    context: Context,
    task: Task<GoogleSignInAccount>,
    firebaseAuth: FirebaseAuth,
    onError: (Exception?) -> Unit,
) {
    when {
        task.isSuccessful -> {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(context as Activity) { task ->
                        when {
                            task.isSuccessful -> {
                                // Sign in success, update UI with the signed-in user's information
                            }
                            else -> { onError(task.exception) }
                        }
                    }

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                onError(e)
            }
        }
        else -> {
            onError(Exception("Task Unsuccessful"))
        }
    }
}

@ExperimentalCoroutinesApi
fun getCatchesFromDoc(docs: List<DocumentSnapshot>) = callbackFlow {
    docs.forEach { doc ->
        doc.reference.collection(CATCHES_COLLECTION)
            .addSnapshotListener { snapshots, _ ->
                if (snapshots != null) {
                    val catches = snapshots.toObjects(UserCatch::class.java)
                    trySend(catches)
                } else {
                    trySend(listOf<UserCatch>())
                }

            }
    }
    awaitClose { }
}