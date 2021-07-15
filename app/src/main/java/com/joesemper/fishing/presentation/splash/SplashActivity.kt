package com.joesemper.fishing.presentation.splash

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.joesemper.fishing.model.common.User
import com.joesemper.fishing.presentation.MainActivity
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.utils.getLoginActivityIntent
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.core.scope.Scope

class SplashActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope : Scope by activityScope()
    private val viewModel: SplashViewModel by viewModel()

    private val logger: Logger by inject()

    private val registeredActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        lifecycleScope.launch {
            viewModel.subscribe().collect { state->
                when (state) {
                    is SplashViewState.Success -> onSuccess(state.user)
                    is SplashViewState.Loading -> {}
                    is SplashViewState.Error -> handleError(state.error)
                }
            }
        }
    }

    private fun onSuccess(user: User?) {
        if (user != null) {
            startMainActivity()
        } else {
            startLoginActivity()
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
        logger.log(error.message)
    }
}