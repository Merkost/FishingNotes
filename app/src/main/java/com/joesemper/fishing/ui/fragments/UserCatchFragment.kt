package com.joesemper.fishing.ui.fragments

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.UserCatchViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.ui.theme.primaryFigmaColor
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

    //while debugging
    val photos = listOf(
        R.drawable.ic_fish.toDrawable(),
        R.drawable.ic_fishing.toDrawable(),
        R.drawable.ic_fisher.toDrawable(),
        R.drawable.ic_fisher.toDrawable(),
        R.drawable.ic_fisher.toDrawable(),
        R.drawable.ic_fisher.toDrawable()
    )

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
                    NewCatchScreen()
                }
            }
        }
    }

    @Composable
    fun NewCatchScreen() {
        Scaffold(
            topBar = { AppBar() }
        ) {
            val scrollState = rememberScrollState()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth().padding(top = 4.dp)
                    .padding(15.dp)
                    .verticalScroll(state = scrollState, enabled = true),
            ) {
                Card(elevation = 4.dp) { Title(catch.title, catch.description, catch.userId) }
                Photos(photos)
                Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) { PlaceInfo() }
                if (!catch.fishWeight.isNaN()) {
                    MyTextField(
                        stringResource(R.string.weight) + stringResource(R.string.kg),
                        catch.fishWeight.toString()
                    )
                }
                if (catch.date.isNotBlank()) {
                    MyTextField(stringResource(R.string.date), catch.date)
                }
                if (catch.time.isNotBlank()) {
                    MyTextField(stringResource(R.string.time), catch.time)
                }
                if (catch.fishingRodType.isNotBlank()) {
                    MyTextField(stringResource(R.string.fish_rod), catch.fishingRodType)
                }
                if (catch.fishingBait.isNotBlank()) {
                    MyTextField(stringResource(R.string.bait), catch.fishingBait)
                }
                if (catch.fishingLure.isNotBlank()) {
                    MyTextField(stringResource(R.string.lure), catch.fishingLure)
                }
                Spacer(Modifier.size(5.dp))
            }
        }
    }

    @Composable
    private fun PlaceInfo() {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(4.dp).fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.Center) {
                Image(
                    painterResource(R.drawable.ic_baseline_map_24),
                    contentDescription = stringResource(R.string.map)
                )
                Text("Точка 2", fontWeight = FontWeight.Bold)
                Text("Описание точки")
            }
            Icon(Icons.Default.CheckCircle, contentDescription = "Check")
        }
    }

    @Composable
    private fun Title(title: String, description: String, userId: String) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth().height(25.dp)) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 5.dp)
                )
                UserProfile()
            }
            Text(description, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.height(30.dp).fillMaxWidth()) {
                Text("08.09.2021", modifier = Modifier.padding(end = 5.dp))
            }
        }
    }

    @Composable
    private fun UserProfile() {
        Row {
            Image(
                painter = painterResource(R.drawable.ic_fisher),
                contentDescription = stringResource(R.string.fisher),
                Modifier.size(50.dp)
            )
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.fisher),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text("@" + stringResource(R.string.fisher), fontSize =10.sp)
            }
        }
    }

    @Composable
    fun Photos(
        photos: List<Drawable>
        //clickedPhoto: SnapshotStateList<Painter>
    ) {

        Column {
            Row(
                modifier = Modifier.align(Alignment.Start),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.size(5.dp))
                Icon(
                    painterResource(R.drawable.ic_baseline_image_24),
                    stringResource(R.string.photos),
                    modifier = Modifier.size(30.dp),
                    tint = primaryFigmaColor
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.photos))
            }
            LazyRow(modifier = Modifier.fillMaxSize()) {
                items(items = photos) {
                    ItemPhoto(
                        photo = it,
                        //clickedPhoto = clickedPhoto
                    )
                }
            }
        }
    }

    @Composable
    fun ItemPhoto(
        photo: Drawable,
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
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(5.dp))
                    .clickable { /*clickedPhoto(photo)*/ })
        }
    }

    @Composable
    private fun MyTextField(text: String, info: String) {
        Card(elevation = 4.dp) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).height(40.dp)
            ) {
                Text(text, modifier = Modifier.padding(start = 5.dp).align(Alignment.CenterVertically))
                Text(info, modifier = Modifier.padding(end = 5.dp).align(Alignment.CenterVertically))
            }
        }
    }

    @Composable
    fun AppBar() {
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
                    modifier = Modifier.fillMaxWidth().padding(end = 3.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, stringResource(R.string.edit),
                        modifier = Modifier.clickable {
                            showToast(
                                requireContext(),
                                "Not Yet Implemented"
                            )
                        }.size(25.dp))
                    Spacer(modifier = Modifier.size(15.dp))
                    Icon(Icons.Default.Delete, stringResource(R.string.edit),
                        modifier = Modifier.clickable {
                            showToast(
                                requireContext(),
                                "Not Yet Implemented"
                            )
                        }.size(25.dp))
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as NavigationHolder).closeNav()
        //setInitialData()
    }

//    private fun setInitialData() {
//        lifecycleScope.launchWhenStarted {
//            viewModel.getMapMarker(catch.userMarkerId).collect { marker ->
//                if (marker != null) {
//                    binding.tvPlaceTitle.text = marker.title
//                    binding.tvPlaceDescription.text = marker.description
//                }
//            }
//        }
//
//    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}


