package com.joesemper.fishing.view.activities

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.joesemper.fishing.utils.getLoginActivityIntent
import com.joesemper.fishing.viewmodel.splash.SplashViewModel
import com.joesemper.fishing.viewmodel.splash.SplashViewState
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
                is SplashViewState.Authorised -> startMainActivity()
                is SplashViewState.NotAuthorised -> startLoginActivity()
                is SplashViewState.Error -> handleError(state.error)
            }
        }
    }

    private fun startLoginActivity() {
        registeredActivity.launch(getLoginActivityIntent())
    }

    private fun startMainActivity() {
        startActivity(MainActivity.getStartIntent(this))
        finish()
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