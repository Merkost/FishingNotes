package com.joesemper.fishing.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.domain.SplashViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class SplashActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScope()
    private val viewModel: SplashViewModel by viewModel()

    private val logger: Logger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        lifecycleScope.launch {
            viewModel.subscribe().collect { state ->
                when (state) {
                    is BaseViewState.Success<*> -> onSuccess(state.data as User?)
                    is BaseViewState.Loading -> { }
                    is BaseViewState.Error -> handleError(state.error)
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
        startActivity(LoginActivity.getStartIntent(this))
        finish()
    }

    private fun startMainActivity() {
        startActivity(MainActivity.getStartIntent(this))
        finish()
    }

    private fun handleError(error: Throwable) {
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        logger.log(error.message)
    }
}