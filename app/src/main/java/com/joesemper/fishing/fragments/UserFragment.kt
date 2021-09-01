package com.joesemper.fishing.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.LoginActivity
import com.joesemper.fishing.R
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.viewmodels.UserViewModel
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
        Scaffold(
            topBar = { AppBar() },
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    UserInfo()
                    UserButtons()

                }
            })
    }

    @ExperimentalMaterialApi
    @Composable
    fun UserButtons() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 80.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            ColumnButton(painterResource(R.drawable.ic_friends), getString(R.string.friends)) {
                notReadyYetToast()
            }
            Spacer(modifier = Modifier.size(15.dp))
            ColumnButton(painterResource(R.drawable.ic_edit), getString(R.string.edit_profile)) {
                notReadyYetToast()
            }
            Spacer(modifier = Modifier.size(15.dp))
            ColumnButton(painterResource(R.drawable.ic_settings), getString(R.string.settings)) {
                notReadyYetToast()
            }
            Spacer(modifier = Modifier.size(30.dp))
            OutlinedButton(onClick = {
                lifecycleScope.launch {
                    viewModel.logoutCurrentUser().collect { isLogout ->
                        if (isLogout) startLoginActivity()
                    }
                }
            }) {
                Text(getString(R.string.logout))
            }
            Spacer(modifier = Modifier.size(30.dp))
        }
    }

    @Composable
    fun ColumnButton(image: Painter, name: String, click: () -> Unit) {
        OutlinedButton(
            onClick = { click },
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


    @SuppressLint("CoroutineCreationDuringComposition")
    @ExperimentalCoilApi
    @Composable
    fun UserInfo() {
        //var user: User? = null
        val user by viewModel.getCurrentUser().collectAsState(User())
        if (user == null) {
            startLoginActivity()
        }

//        LaunchedEffect(viewModel.getCurrentUser()) {
//
//        }

        // Create a CoroutineScope that follows this composable's lifecycle
        val composableScope = rememberCoroutineScope()

//        composableScope.launch {
//            viewModel.getCurrentUser().collect {
//                user = it
//            }
//        }

//        lifecycleScope.launch {
//
//        }
//        val user = remember {
//            viewModel.getCurrentUser()
//        }
//        val currentUser by user.collect {
//
//        }
//        lifecycleScope.launchWhenStarted {
//            viewModel.getCurrentUser().collect { user ->
//                LocalContext.current.run {
//                    UserInfo(user!!.userPic)
//                }

//Might Help - https://stackoverflow.com/questions/67380598/compose-use-flowt-collectasstate-render-listt-lazycolumn-progressive
        //LaunchedEffect(coroutineContext })

//        val lifecycleOwner = LocalLifecycleOwner.current
//        val currentUserInner = remember(currentUserFlow, lifecycleOwner) {
//            currentUserFlow.flowOn(lifecycleScope.coroutineContext)
//        }
//
//        val currentUser by currentUserInner.collectAsState()
        Card(
            elevation = 10.dp,
            modifier = Modifier
                .padding(top = 50.dp).size(230.dp),
            shape = RoundedCornerShape(25.dp),
            backgroundColor = MaterialTheme.colors.surface
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.size(30.dp))
//            lifecycleScope.launchWhenStarted {
//                viewModel.getCurrentUserComposable().collect { user ->
                UserImage(user) /*?: UserImage(painterResource(R.drawable.ic_fisher))*/
                UserName(user)
            }
        }
    }

    @Composable
    private fun UserName(user: User?) {
        Text(
            text = when (user == null) {
                true -> stringResource(R.string.anonymous)
                false -> user.userName
            }, style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(vertical = 15.dp, horizontal = 20.dp),
            textAlign = TextAlign.Center
        )
    }

    @ExperimentalCoilApi
    @Composable
    fun UserImage(user: User?) {
        Image(
            painter = when (user == null) {
                true -> painterResource(R.drawable.ic_fisher)
                false -> if (user.userPic.isNullOrEmpty() or user.isAnonymous) painterResource(R.drawable.ic_fisher)
                else rememberImagePainter(
                    data = user.userPic,
                    builder = {
                        transformations(CircleCropTransformation())
                    })
            },
            contentDescription = stringResource(R.string.fisher),
            modifier = Modifier
                .size(120.dp)
        )
    }

    @Composable
    fun AppBar() {
        TopAppBar(
            title = {
                Text(text = "User")
            },
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
            Toast.LENGTH_LONG
        ).show()
    }

    private fun startLoginActivity() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }
}