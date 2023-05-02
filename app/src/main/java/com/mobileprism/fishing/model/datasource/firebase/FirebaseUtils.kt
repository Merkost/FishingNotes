package com.mobileprism.fishing.model.datasource.firebase

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInCredential
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.mobileprism.fishing.R
import com.mobileprism.fishing.domain.entity.content.UserCatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.koin.androidx.compose.get

const val CATCHES_COLLECTION = "catches"

fun Context.getGoogleLoginAuth(): Task<BeginSignInResult> {
    val otclient = Identity.getSignInClient(this);
    val signInRequest =

        BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build();

    return otclient.beginSignIn(signInRequest)
//    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestIdToken(getString(R.string.default_web_client_id))
//        .requestEmail()
//        .requestProfile()
//        .build()
//    return GoogleSignIn.getClient(this, gso)
}

@Composable
fun createLauncherActivityForGoogleAuth(
    context: Context,
    onComplete: (GoogleSignInAccount, FirebaseUser?) -> Unit,
    onError: (Exception) -> Unit,
): ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult> {
    val auth: FirebaseAuth = get()

    return rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                val otclient = Identity.getSignInClient(context)
                val credential = otclient.getSignInCredentialFromIntent(intent)
//                val task: Task<GoogleSignInAccount> =
//                    GoogleSignIn.getSignedInAccountFromIntent(intent)

                Log.e("credential", credential.password.toString())
//                credential.
//                startFirebaseLogin(
//                    context = context,
//                    signInCredential = credential,
//                    firebaseAuth = auth,
//                    onComplete = onComplete,
//                    onError = {
//                        onError(it ?: Exception(context.getString(R.string.error_occured)))
//                    }
//                )

            } ?: onError(Exception(context.getString(R.string.error_occured)))
        } else {
            onError(Exception(context.getString(R.string.cancelled_by_user)))
        }
    }
}

private fun startFirebaseLogin(
    context: Context,
    signInCredential: SignInCredential,
    firebaseAuth: FirebaseAuth,
    onComplete: (GoogleSignInAccount, FirebaseUser?) -> Unit,
    onError: (Exception?) -> Unit,
) {

    try {
        // Google Sign In was successful, authenticate with Firebase
        val credential = GoogleAuthProvider.getCredential(signInCredential.googleIdToken, null)
        val account = GoogleSignInAccount.createDefault()

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(context as Activity) { completedTask ->
                when {
                    completedTask.isSuccessful -> {
                        // Sign in success, update UI with the signed-in user's information
                        onComplete(account, completedTask.result.user)
                    }

                    else -> {
                        onError(completedTask.exception)
                    }
                }
            }.addOnFailureListener(context as Activity) { completedTask ->
                onError(completedTask.cause as Exception?)
            }

    } catch (e: ApiException) {
        // Google Sign In failed, update UI appropriately
        onError(e)
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