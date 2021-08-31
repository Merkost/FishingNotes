package com.joesemper.fishing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter.State.Empty.painter
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.viewmodels.UserViewModel
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val viewModel: UserViewModel by viewModel()

    @ExperimentalCoilApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                UserScreen()
            }
        }
    }

    @ExperimentalCoilApi
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        UserScreen()
    }

    @ExperimentalCoilApi
    @Composable
    fun UserScreen() {
        MaterialTheme {
            Column {
                AppBar()
                UserInfo()

                Text("Hello Jetpack Compose!")
            }
        }
    }

    @ExperimentalCoilApi
    @Composable
    fun UserInfo(/*user: User*/) {
        Column {
            Image(
                painter = rememberImagePainter(
                    data = /*when (user.isAnonymous) {
                        true -> */R.drawable.ic_fisher,/*
                        false -> "https://www.example.com/image.jpg",
                    },*/
                    builder = {
                        transformations(CircleCropTransformation())
                    }),
                contentDescription = null,
                modifier = Modifier.size(128.dp))
        }
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
        backgroundColor = Color.Gray,
        contentColor = Color.Black,
        elevation = 2.dp
    )
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)


//        subscribeOnViewModel()
//        setButtonsClickListeners()
//        setToolbarBackButton()
//
//        setOnLogoutButtonListener()
}

//    private fun setButtonsClickListeners() {
//        vb.buttonEdit.setOnClickListener { notReadyYetToast() }
//        vb.buttonFriends.setOnClickListener { notReadyYetToast() }
//        vb.buttonSettings.setOnClickListener { notReadyYetToast() }
//    }
//
//    private fun notReadyYetToast() {
//        Toast.makeText(context, "This feature is still in development. Please, try it later", Toast.LENGTH_LONG).show()
//    }
//
//    private fun setToolbarBackButton() {
//        vb.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
//    }
//
//    private fun subscribeOnViewModel() {
//        lifecycleScope.launchWhenStarted {
//            viewModel.getCurrentUser().collect { user ->
//                if (user != null) {
//                    when (user.isAnonymous) {
//                        true -> doOnAnonymousUser(user)
//                        false -> doOnSimpleUser(user)
//                    }
//                } else {
//                    startSplashActivity()
//                }
//            }
//        }
//    }
//
//    private fun setOnLogoutButtonListener() {
//        vb.buttonLogout.setOnClickListener {
//            lifecycleScope.launch{
//                viewModel.logoutCurrentUser()
//            }
//        }
//    }
//
//    private fun startSplashActivity() {
//        val intent = Intent(requireContext(), SplashActivity::class.java)
//        startActivity(intent)
//    }
//
//    private fun doOnAnonymousUser(user: User) {
//        vb.ivUserPic.load(R.drawable.ic_fisher)
//        vb.tvUsername.text = "Guest"
//        vb.buttonLogout.text = "Login"
//    }
//
//    private fun doOnSimpleUser(user: User) {
//        vb.ivUserPic.load(user.userPic) {
//            placeholder(R.drawable.ic_fisher)
//            transformations(CircleCropTransformation())
//        }
//       vb.tvUsername.text = user.userName
//    }
}