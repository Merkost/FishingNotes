package com.joesemper.fishing.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.joesemper.fishing.compose.ui.FishingNotesApp
import com.joesemper.fishing.domain.MainViewModel
import com.joesemper.fishing.utils.Logger
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

//    override val scope: Scope by activityScope()
    private val viewModel: MainViewModel by viewModel()

    private val logger: Logger by inject()

    //private var _binding: ActivityMainBinding? = null
    //private val binding get() = _binding!!

    companion object {
        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

//    override fun hideNav() {
//        binding.bottomNav.visibility = View.GONE
//    }
//
//    override fun showNav() {
//        binding.bottomNav.visibility = View.VISIBLE
//    }

    @ExperimentalPermissionsApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This app draws behind the system bars, so we want to handle fitting system windows
        // WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            FishingNotesApp()
        }

        //light тема
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

//        _binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

//        initBottomNav()
//        subscribeOnViewModel()
    }

//    private fun subscribeOnViewModel() {
//        lifecycleScope.launchWhenStarted {
//            viewModel.subscribe().collect { viewState ->
//                when (viewState) {
//                    is BaseViewState.Success<*> -> {
//                        onSuccess()
//                    }
//                    is BaseViewState.Error -> {
//                        onError(viewState.error)
//                    }
//                    is BaseViewState.Loading -> {
//                    }
//                }
//            }
//        }
//    }
//
//    private fun onSuccess() {
//
//    }
//
//    private fun onError(error: Throwable) {
//        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
//        logger.log(error.message)
//    }
//
//    private fun initBottomNav() {
//        val host: NavHostFragment = supportFragmentManager
//            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment? ?: return
//        val navController = host.navController
//
//        binding.bottomNav.setupWithNavController(navController)
//    }

    fun notReadyYetToast() {
        Toast.makeText(
            this,
            "This feature is still in development. Please, try it later",
            Toast.LENGTH_SHORT
        ).show()
    }

}
