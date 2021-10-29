package com.joesemper.fishing.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.LoginViewModel
import com.joesemper.fishing.model.entity.common.User
import com.joesemper.fishing.utils.Logger
import com.joesemper.fishing.domain.SplashViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.ui.theme.Typography
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashActivity : AppCompatActivity() {

    private val logger: Logger by inject()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val registeredActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            onActivityResult(result)
        }



    @ExperimentalPermissionsApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigation()
        }

        auth = FirebaseAuth.getInstance()
    }


    @InternalCoroutinesApi
    @ExperimentalMaterialApi
    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @ExperimentalPermissionsApi
    @Composable
    fun Navigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "splash_screen"
        ) {

            composable("splash_screen") {
                SplashScreen(navController = navController)
            }

            composable("login_screen") {
                LoginScreen(navController = navController)
            }
           /* // Main Screen
            composable("main_screen") {
                FishingNotesApp()
            }*/
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalAnimationApi
    @Composable
    fun LoginScreen(navController: NavController) {



        var isLoading by remember { mutableStateOf(false) }

        val loginViewModel: LoginViewModel = get()
        val uiState = loginViewModel.subscribe().collectAsState()

        LaunchedEffect(loginViewModel.subscribe().collectAsState().value) {
            when (uiState.value) {
                is BaseViewState.Success<*> -> {
                    onSuccess(
                        (uiState.value as BaseViewState.Success<*>).data as User?,
                        navController
                    )
                    isLoading = false
                }
                is BaseViewState.Loading -> isLoading = true
                is BaseViewState.Error -> handleError((uiState.value as BaseViewState.Error).error)
            }
        }
        //var isLoading by remember { mutableStateOf(false) }

        ConstraintLayout(modifier = Modifier.fillMaxSize().background(Color.White)) {
            val (background, card, lottieSuccess, cardColumn) = createRefs()

            Surface(modifier = Modifier.fillMaxWidth().height(450.dp).constrainAs(background) {
                top.linkTo(parent.top)
            }, color = primaryFigmaColor) {}

            Card(
                modifier = Modifier.constrainAs(card) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }.fillMaxWidth().padding(30.dp).wrapContentHeight(),
                elevation = 10.dp,
                shape = RoundedCornerShape(30.dp)
            ) {

                //LottieSuccess
                AnimatedVisibility(false, modifier = Modifier.constrainAs(lottieSuccess) {
                    top.linkTo(card.top)
                    bottom.linkTo(card.bottom)
                    absoluteLeft.linkTo(card.absoluteLeft)
                    absoluteRight.linkTo(card.absoluteRight)
                }) { LottieSuccess(modifier = Modifier) }

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .constrainAs(cardColumn) {
                            top.linkTo(card.top)
                            bottom.linkTo(card.bottom)
                            absoluteLeft.linkTo(card.absoluteLeft)
                            absoluteRight.linkTo(card.absoluteRight)
                        }.fillMaxWidth().animateContentSize()
                ) {

                    //AppIcon
                    Image(
                        painterResource(R.mipmap.ic_launcher), stringResource(R.string.icon),
                        modifier = Modifier.padding(30.dp).size(140.dp)
                    )

                    //Title
                    Text(stringResource(R.string.login_title), style = Typography.h5, color = Color.DarkGray)

                    //LottieLoading
                    AnimatedVisibility(isLoading) { LottieLoading(modifier = Modifier.size(140.dp)) }
                    AnimatedVisibility(!isLoading) { Spacer(modifier = Modifier.fillMaxWidth().height(30.dp)) }

                    //Google button
                    Card(shape = RoundedCornerShape(20.dp), elevation = 10.dp, onClickLabel = stringResource(R.string.google_login),
                        onClick = { isLoading = true; startGoogleLogin() },) {
                        Row(modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Image(painterResource(R.drawable.googleg_standard_color_18),
                                stringResource(R.string.google_login), modifier = Modifier.size(25.dp))
                            Text("Sign in with Google", style = Typography.body1)
                        }
                    }

                    //Space
                    Spacer(modifier = Modifier.fillMaxWidth().height(30.dp))
                }


            }
        }
    }

    @Composable
    private fun LottieSuccess(modifier: Modifier = Modifier) {
        val coroutineScope = rememberCoroutineScope()
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.another_marker))
        val lottieAnimatable = rememberLottieAnimatable()
        var minMaxFrame by remember {
            mutableStateOf(LottieClipSpec.Frame(0, 30))
        }

        minMaxFrame = LottieClipSpec.Frame(30, 82).also { Log.d("MAP", "MoveFinish") }
        LaunchedEffect(Unit) {
            lottieAnimatable.animate(
                composition,
                iteration = 1,
                continueFromPreviousAnimate = true,
                clipSpec = minMaxFrame,
            )
        }


        LottieAnimation(
            modifier = modifier.size(128.dp),
            composition = composition,
            progress = lottieAnimatable.progress
        )
    }

    @Composable
    private fun LottieLoading(modifier: Modifier = Modifier) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.walking_fish))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
        LottieAnimation(
            composition,
            progress,
            modifier = modifier
        )
    }


    @Composable
    fun SplashScreen(navController: NavController) {
        val viewModel: SplashViewModel by viewModel()
        val userState = viewModel.subscribe().collectAsState()

        LaunchedEffect(viewModel.subscribe().collectAsState().value) {
            when (userState.value) {
                is BaseViewState.Success<*> -> {
                    if ((userState.value as BaseViewState.Success<*>).data as User? == null) {
                        navController.navigate("login_screen")
                    } else startMainActivity()
                }
                is BaseViewState.Loading -> {
                }
                is BaseViewState.Error -> {
                } //showErrorSnackbar
            }
        }

        val scale = remember {
            Animatable(0f)
        }

        // AnimationEffect
        LaunchedEffect(key1 = true) {
            scale.animateTo(
                targetValue = 0.2f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = {
                        OvershootInterpolator(4f).getInterpolation(it)
                    })
            )
            delay(1000)
            //navController.navigate("main_screen")
        }

        // Image
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_fishing),
                contentDescription = "Logo",
                modifier = Modifier.scale(scale.value)
            )
        }
    }

    /*private fun onSuccess(user: User?, navController: NavController) {
        if (user != null) {
            navController.navigate("main_screen")
        } else {
            navController.navigate("login_screen")
        }
    }*/

    private fun startGoogleLogin() {

        // Configure GOOGLE sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent: Intent = googleSignInClient.signInIntent
        registeredActivity.launch(signInIntent)
    }

    private fun onActivityResult(result: ActivityResult) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val exception = task.exception
        if (task.isSuccessful) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                handleError(exception as Throwable)
            }
        } else {
            handleError(exception as Throwable)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                } else {
                    // If sign in fails, display a message to the user.
                    handleError(task.exception as Throwable)
                }
            }
    }

    private fun handleError(error: Throwable) {
        /*setViews(false)
        vb.warning.visibility = View.VISIBLE
        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
        vb.warning.setOnClickListener {
            Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
        }*/
        logger.log(error.message)
    }

    private fun onSuccess(user: User?, navController: NavController) {
        if (user != null) {
            //vb.progressAnimationView.playAnimation()
            //Timer().schedule(2250) {
                startMainActivity()
            //}
        } else if (navController.currentDestination?.route != "login_screen")  navController.navigate("login_screen")
    }

    private fun startMainActivity() {
        startActivity(MainActivity.getStartIntent(this))
        finish()
    }
}