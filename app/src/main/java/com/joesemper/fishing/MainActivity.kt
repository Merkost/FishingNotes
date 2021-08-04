package com.joesemper.fishing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.joesemper.fishing.databinding.ActivityMainBinding
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.viewmodels.MainViewModel
import com.joesemper.fishing.viewmodels.viewstates.MainViewState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class MainActivity : AppCompatActivity(), AndroidScopeComponent, NavigationHolder {

    override val scope : Scope by activityScope()
    private val viewModel: MainViewModel by viewModel()

    private val logger: Logger by inject()

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override fun closeNav() {
        binding.bottomNav.visibility = View.GONE
    }

    override fun showNav() {
        binding.bottomNav.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNav()
        subscribeOnViewModel()
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    is MainViewState.Success -> { onSuccess() }
                    is MainViewState.Error -> { onError(viewState.error) }
                    MainViewState.Loading -> { }
                }
            }
        }
    }

    private fun onSuccess() {

    }

    private fun onError(error: Throwable) {
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        logger.log(error.message)
    }

    private fun initBottomNav() {
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment? ?: return
        val navController = host.navController

        bottomNav.setupWithNavController(navController)
    }

}
