package com.joesemper.fishing.ui.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.ui.composable.CatchInfo
import com.joesemper.fishing.ui.composable.MyCardNoPadding
import com.joesemper.fishing.ui.composable.UserProfile
import com.joesemper.fishing.ui.composable.PlaceInfo
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.showToast
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserCatchFragment : Fragment(), AndroidScopeComponent {

    private val args: UserCatchFragmentArgs by navArgs()

    override val scope: Scope by fragmentScope()
    private val viewModel: UserCatchViewModel by viewModel()

    private lateinit var catch: UserCatch

    companion object {
        private const val TAG = "CATCH"
        private const val ITEM_PHOTO = "ITEM_PHOTO"
    }

    fun newInstance(catch: UserCatch): Fragment {
        val args = bundleOf(TAG to catch)
        val fragment = UserCatchFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catch = args.userCatch
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    UserCatchScreen()
                }
            }
        }
    }

    @Composable
    fun UserCatchScreen() {
        Scaffold(
            topBar = { AppBar() }
        ) {
            val scrollState = rememberScrollState()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(primaryFigmaBackgroundTint)
                    .verticalScroll(state = scrollState, enabled = true),
            ) {
                val user by viewModel.getCurrentUser().collectAsState(null)
                CatchInfo(catch, user)
                Photos()
                val mapMarker by viewModel.getMapMarker(catch.userMarkerId).collectAsState(null)
                mapMarker?.let { it1 -> PlaceInfo(user, it1) }
                MyTextField(
                    stringResource(R.string.weight),
                    catch.fishWeight.toString() + " " + stringResource(R.string.kg)
                )
                MyTextField(
                    stringResource(R.string.amount),
                    catch.fishAmount.toString() + " " + stringResource(R.string.pc)
                )
                /*MyTextField(stringResource(R.string.date), catch.date)
                MyTextField(stringResource(R.string.time), catch.time)*/
                MyTextField(stringResource(R.string.fish_rod), if (!catch.fishingRodType.isEmpty())
                    catch.fishingRodType else getString(R.string.not_specified))
                MyTextField(stringResource(R.string.bait), if (!catch.fishingBait.isEmpty())
                    catch.fishingBait else getString(R.string.not_specified))
                MyTextField(stringResource(R.string.lure), if (!catch.fishingLure.isEmpty())
                    catch.fishingLure else getString(R.string.not_specified))
                Spacer(Modifier.size(5.dp))
            }

        }
    }

    @Composable
    fun Photos(
        //clickedPhoto: SnapshotStateList<Painter>
    ) {
        LazyRow(modifier = Modifier.fillMaxSize()) {
            item { Spacer(modifier = Modifier.size(4.dp)) }
            if (catch.downloadPhotoLinks.isNullOrEmpty()) {
                item { Text("No photos here", modifier = Modifier.padding(horizontal = 11.dp)) }
            } else {
                items(items = catch.downloadPhotoLinks) {
                    ItemPhoto(
                        photo = it,
                        //clickedPhoto = clickedPhoto
                    )
                }
            }
        }
    }

    @Composable
    fun ItemNoPhoto() {



        /*Box(
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { showToast(requireContext(),
                        "На этот улов не были добавлены фото, к сожалению.") },
                elevation = 5.dp, backgroundColor = Color.LightGray
            ) {
                Icon(
                    painterResource(R.drawable.ic_no_photo_vector), //Or we can use Icons.Default.Add
                    contentDescription = "NO_PHOTOS",
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )
            }
        }*/
    }

    @Composable
    fun ItemPhoto(
        photo: String,
        //clickedPhoto: (Painter) -> Unit
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(4.dp)
        ) {
            Image(painter = rememberImagePainter(data = photo),
                contentDescription = ITEM_PHOTO,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { /*clickedPhoto(photo)*/ })
        }
    }

    @Composable
    private fun MyTextField(text: String, info: String) {
        MyCardNoPadding {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .height(40.dp)
            ) {
                Text(
                    text,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    info,
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }

    @Composable
    fun AppBar() {
        val dialogOnDelete = rememberSaveable { mutableStateOf(false) }
        TopAppBar(
            title = { Text(text = "Catch") },
            navigationIcon = {
                IconButton(onClick = { findNavController().popBackStack() }, content = {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = getString(R.string.back)
                    )
                })
            }, actions = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 3.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            showToast(
                                requireContext(),
                                "Not Yet Implemented"
                            )
                        },
                        content = { Icon(Icons.Default.Edit, stringResource(R.string.edit)) }
                    )
                    IconButton(
                        onClick = {
                            dialogOnDelete.value = true
                        },
                        content = { Icon(Icons.Default.Delete, stringResource(R.string.edit)) }
                    )
                }
            })
        if (dialogOnDelete.value) DeleteDialog(dialogOnDelete)
    }

    @Composable
    fun DeleteDialog(dialogOnDelete: MutableState<Boolean>) {
        AlertDialog(
            title = { Text(stringResource(R.string.catch_deletion)) },
            text = { Text(stringResource(R.string.catch_delete_confirmantion)) },
            onDismissRequest = { dialogOnDelete.value = false },
            confirmButton = {
                OutlinedButton(
                    onClick = { viewModel.deleteCatch(catch); findNavController().popBackStack() },
                    content = { Text(getString(R.string.Yes)) })
            }, dismissButton = {
                OutlinedButton(
                    onClick = { dialogOnDelete.value = false },
                    content = { Text(getString(R.string.No)) })
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as NavigationHolder).hideNav()
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}


