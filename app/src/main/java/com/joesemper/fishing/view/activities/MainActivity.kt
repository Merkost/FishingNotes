package com.joesemper.fishing.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.joesemper.fishing.R
import com.joesemper.fishing.model.entity.user.User
import com.joesemper.fishing.view.fragments.MapFragment
import com.joesemper.fishing.view.fragments.dialog.LogoutListener
import com.joesemper.fishing.view.fragments.dialog.UserBottomSheetDialogFragment
import com.joesemper.fishing.viewmodel.main.MainViewModel
import com.joesemper.fishing.viewmodel.main.MainViewState
import com.joesemper.fishing.viewmodel.map.MapViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class MainActivity : AppCompatActivity(), LogoutListener {

//    private val viewModel: MainViewModel by inject()

    private val viewModelScope = getKoin().getOrCreateScope("MainScope", named<MainActivity>())
    private val viewModel: MainViewModel = viewModelScope.get()

    private var currentUser: User? = null

    companion object {
        fun getStartIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initBottomNav()

        lifecycleScope.launch {

            viewModel.subscribe().collect { viewState ->
                when (viewState) {
                    is MainViewState.Success -> { onSuccess(viewState.user) }
                }
            }
        }

    }

    private fun onSuccess(user: User?) {
        if (user != null) {
            currentUser = user
        } else {
            startSplashActivity()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_logout -> {
                startBottomSheetDialogFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onLogout() {
        viewModel.logOut()
    }

    private fun initBottomNav() {
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment? ?: return
        val navController = host.navController

        bottomNav.setupWithNavController(navController)
    }

    private fun startBottomSheetDialogFragment(): Boolean {
        if (currentUser != null) {
            val dialog = UserBottomSheetDialogFragment.newInstance(currentUser!!)
            dialog.show(supportFragmentManager, "TAG")
        }
        return true
    }

    private fun startSplashActivity() {
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unsubscribe()
        viewModelScope.close()
    }

}
