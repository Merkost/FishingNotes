package com.joesemper.fishing.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.UserViewModel
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.ui.LoginActivity
import com.joesemper.fishing.ui.theme.FigmaTheme
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val viewModel: UserViewModel by viewModel()

    @InternalCoroutinesApi
    @ExperimentalCoilApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    UserScreen()
                }
            }
        }
    }

    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    @Composable
    fun UserScreen() {
        val uiState = viewModel.uiState
        Scaffold(
            topBar = { AppBar() },
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Card(
                        elevation = 10.dp,
                        modifier = Modifier.padding(50.dp).fillMaxWidth().height(220.dp),
                        shape = RoundedCornerShape(25.dp),
                        backgroundColor = MaterialTheme.colors.surface
                    ) {
                        Column() {
                            UserInfo()
                            UserStats()
                        }

                    }
                    UserButtons()
                }
            })
    }

    @Composable
    fun UserStats() {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(5.dp).fillMaxWidth().height(50.dp)
        ) {
            PlacesNumber()
            CatchesNumber()
        }
    }

    @Composable
    fun PlacesNumber() {
        val userPlacesNum by viewModel.getUserPlaces().collectAsState(listOf())
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = Modifier.size(50.dp)
        ) {
            Icon(
                Icons.Default.Place, stringResource(R.string.place),
                modifier = Modifier.size(25.dp)
            )
            Text(userPlacesNum.size.toString())
        }
    }

    @Composable
    fun CatchesNumber() {
        val userCatchesNum by viewModel.userCatches
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center, modifier = Modifier.size(50.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_fishing), stringResource(R.string.place),
                modifier = Modifier.size(25.dp)
            )
            Text(userCatchesNum.toString())
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun UserButtons() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(horizontal = 80.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.Bottom)
        ) {
            ColumnButton(painterResource(R.drawable.ic_friends), getString(R.string.friends)) {
                notReadyYetToast()
            }

            ColumnButton(painterResource(R.drawable.ic_edit), getString(R.string.edit_profile)) {
                notReadyYetToast()
            }

            ColumnButton(painterResource(R.drawable.ic_settings), getString(R.string.settings)) {
                val action =
                    UserFragmentDirections.actionUserFragmentToSettingsFragment()
                findNavController().navigate(action)
            }
            Spacer(modifier = Modifier.size(15.dp))
            OutlinedButton(onClick = {
                lifecycleScope.launch {
                    viewModel.logoutCurrentUser().collect { isLogout ->
                        if (isLogout) startLoginActivity()
                    }
                }
            }) { Text(getString(R.string.logout)) }
            Spacer(modifier = Modifier.size(30.dp))
        }
    }

    @Composable
    fun ColumnButton(image: Painter, name: String, click: () -> Unit) {
        OutlinedButton(
            onClick = click,
            modifier = Modifier.fillMaxWidth(),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(image, name, modifier = Modifier.size(25.dp))
                    Text(name, modifier = Modifier.padding(start = 10.dp))
                }
            })
    }

    @ExperimentalCoilApi
    @Composable
    fun UserInfo() {
        val user by viewModel.getCurrentUser().collectAsState(null)
        //TODO: add number of places, catches and more
        user?.let { nutNullUser ->
            Crossfade(nutNullUser, animationSpec = tween(500)) { animatedUser ->
                UserNameAndImage(animatedUser)
            }
        } ?: Row(
            modifier = Modifier.fillMaxWidth().height(150.dp).padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()
        }
    }


    @ExperimentalCoilApi
    @Composable
    fun UserNameAndImage(user: User) {
        Row(
            modifier = Modifier.fillMaxWidth().height(150.dp).padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = if (user.userPic.isNullOrEmpty() or user.isAnonymous)
                    painterResource(R.drawable.ic_fisher)
                else rememberImagePainter(
                    data = user.userPic,
                    builder = {
                        transformations(CircleCropTransformation())
                        //crossfade(500)
                    }
                ),
                contentDescription = stringResource(R.string.fisher),
                modifier = Modifier.size(150.dp),
            )
            Text(
                text = when (user.isAnonymous) {
                    true -> stringResource(R.string.anonymous)
                    false -> user.userName
                }, style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    fun AppBar() {
        TopAppBar(
            title = { Text(text = "User") },
            navigationIcon = {
                IconButton(onClick = { findNavController().popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = getString(R.string.back)
                    )
                }
            },
            elevation = 2.dp
        )
    }

    private fun notReadyYetToast() {
        Toast.makeText(
            context,
            "This feature is still in development. Please, try it later",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun startLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }

    private fun temp() {
        TODO("deal with collecting flow")
        // Create a CoroutineScope that follows this composable's lifecycle
        // val composableScope = rememberCoroutineScope()

        //Might Help - https://stackoverflow.com/questions/67380598/compose-use-flowt-collectasstate-render-listt-lazycolumn-progressive
        //LaunchedEffect(coroutineContext })
    }
}