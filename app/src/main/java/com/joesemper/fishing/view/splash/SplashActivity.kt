package com.joesemper.fishing.view.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.joesemper.fishing.model.splash.entity.SplashState
import com.joesemper.fishing.view.MainActivity
import com.joesemper.fishing.viewmodel.splash.SplashViewModel
import org.koin.android.scope.currentScope

class SplashActivity : AppCompatActivity() {

    private val registeredActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: SplashViewModel by currentScope.inject()

        viewModel.subscribe().observe(this) { state ->
            when (state) {
                is SplashState.Authorised -> startMainActivity()
                is SplashState.NotAuthorised -> startLoginActivity()
                is SplashState.Error -> handleError(state.error)
            }
        }
    }

    private fun startLoginActivity() {
        val intent = getLoginActivityIntent()
        launchActivityForResult(intent)
    }

    private fun startMainActivity() {
        startActivity(MainActivity.getStartIntent(this))
    }

    private fun getLoginActivityIntent(): Intent {
        val providers = getProvidersList()
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
    }

    private fun getProvidersList() = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.AnonymousBuilder().build()
    )

    private fun launchActivityForResult(intent: Intent) {
        registeredActivity.launch(intent)
    }

    private fun onActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            startMainActivity()
            finish()
        }
    }

    private fun handleError(error: Throwable) {
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
    }

}