package com.joesemper.fishing.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.databinding.ActivityLoginBinding
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.domain.LoginViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class LoginActivity : AppCompatActivity(), AndroidScopeComponent {

    private lateinit var vb: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override val scope: Scope by activityScope()
    private val logger: Logger by inject()
    private val viewModel: LoginViewModel by viewModel()

    private val registeredActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }

    companion object {
        fun getStartIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vb.root)

        lifecycleScope.launch {
            viewModel.subscribe().collect { state ->
                when (state) {
                    is BaseViewState.Success<*> -> onSuccess(state.data as User?)
                    is BaseViewState.Loading -> onLoading()
                    is BaseViewState.Error -> handleError(state.error)
                }
            }
        }

        auth = FirebaseAuth.getInstance()

        vb.googleSignInButton.setOnClickListener {
            startGoogleLogin()
            onLoading()
        }
        vb.guestSignInButton.setOnClickListener {
            startGuestLogin()
            onLoading()
        }

    }

    private fun onLoading() {
        setViews(true)
    }

    private fun setViews(isLoading: Boolean) {
        if (isLoading) {
            vb.progressBar.visibility = View.VISIBLE
            vb.warning.visibility = View.GONE
        } else {
            vb.progressBar.visibility = View.INVISIBLE
        }
        vb.googleSignInButton.isClickable = !isLoading
        vb.guestSignInButton.isClickable = !isLoading
    }

    private fun onSuccess(user: User?) {
        setViews(false)
        if (user != null) {
            startMainActivity()
        }
    }

    private fun startMainActivity() {
        startActivity(MainActivity.getStartIntent(this))
        finish()
    }

    private fun handleError(error: Throwable) {
        setViews(false)
        vb.warning.visibility = View.VISIBLE
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        vb.warning.setOnClickListener {
            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        }
        logger.log(error.message)
    }

    private fun startGoogleLogin() {
        // Configure GOOGLE sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent: Intent = googleSignInClient.signInIntent
        registeredActivity.launch(signInIntent)
    }

    private fun onActivityResult(result: ActivityResult) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val exception = task.exception
        if (task.isSuccessful) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                handleError(exception as Throwable)
            }
        } else {
            handleError(exception as Throwable)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                } else {
                    // If sign in fails, display a message to the user.
                    handleError(task.exception as Throwable)
                }
            }
    }

    private fun startGuestLogin() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                } else {
                    // If sign in fails, display a message to the user.
                    handleError(task.exception as Throwable)
                }
            }
    }


}