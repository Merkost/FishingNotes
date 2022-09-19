package com.mobileprism.fishing.model.datasource.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
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
import org.koin.androidx.compose.get

const val CATCHES_COLLECTION = "catches"

fun Context.getGoogleLoginAuth(): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .requestProfile()
        .build()
    return GoogleSignIn.getClient(this, gso)
}

@Composable
fun createLauncherActivityForGoogleAuth(
    context: Context,
    onComplete: () -> Unit,
    onError: (Exception) -> Unit,
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    val auth: FirebaseAuth = get()

    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            result.data?.let { intent ->
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)

                startFirebaseLogin(
                    context = context,
                    task = task,
                    firebaseAuth = auth,
                    onComplete = onComplete,
                    onError = {
                        onError(
                            it ?: Exception(context.getString(R.string.error_occured))
                        )
                    }
                )

            } ?: onError(Exception(context.getString(R.string.error_occured)))
        } else {
            onError(Exception(context.getString(R.string.cancelled_by_user)))
        }
    }
}

private fun startFirebaseLogin(
    context: Context,
    task: Task<GoogleSignInAccount>,
    firebaseAuth: FirebaseAuth,
    onComplete: () -> Unit,
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
                                onComplete()
                            }
                            else -> {
                                onError(task.exception)
                            }
                        }
                    }

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                onError(e)
            }
        }
        else -> {
            onError(Exception(context.getString(R.string.error_occured)))
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