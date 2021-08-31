package com.joesemper.fishing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.joesemper.fishing.R
import com.joesemper.fishing.data.entity.common.User
import com.joesemper.fishing.viewmodels.UserViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
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
                UserScreen()
            }
        }
    }

    @ExperimentalMaterialApi
    @InternalCoroutinesApi
    @ExperimentalCoilApi
    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        UserScreen()
    }

    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalCoilApi
    @Composable
    fun UserScreen() {
        MaterialTheme {
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
    }

    @ExperimentalMaterialApi
    @Composable
    fun UserButtons() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp, bottom = 10.dp)
                .padding(horizontal = 80.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            ColumnButton(painterResource(R.drawable.ic_friends), getString(R.string.friends))
            ColumnButton(painterResource(R.drawable.ic_edit), getString(R.string.edit_profile))
            ColumnButton(painterResource(R.drawable.ic_settings), getString(R.string.settings))
            Spacer(modifier = Modifier.size(15.dp))
            OutlinedButton(onClick = { /*TODO*/ }) {
                Text(getString(R.string.logout))
            }
        }
    }

    @Composable
    fun ColumnButton(image: Painter, name: String) {
        OutlinedButton(
            onClick = { /*TODO*/ },
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
        val user: User = User("0", null, true, null)
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
//            lifecycleScope.launchWhenStarted {
//                viewModel.getCurrentUserComposable().collect { user ->
            Image(
                painter = rememberImagePainter(

                    data = when (user.isAnonymous) {
                        true -> R.drawable.ic_fisher
                        false -> user.userPic
                    },
                    builder = {
                        transformations(CircleCropTransformation())
                    }),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .padding(top = 30.dp)
            )
        }

        Text(
            "User name",
            modifier = Modifier.padding(10.dp)
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