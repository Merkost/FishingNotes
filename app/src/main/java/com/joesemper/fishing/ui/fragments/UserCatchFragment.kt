package com.joesemper.fishing.ui.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
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
                    .fillMaxWidth().background(primaryFigmaBackgroundTint)
                    .verticalScroll(state = scrollState, enabled = true),
            ) {
                Title(catch.title, catch.description, catch.userId, catch.date)
                Photos(photos)
                PlaceInfo()
                MyTextField(
                    stringResource(R.string.weight),
                    catch.fishWeight.toString() + " " + stringResource(R.string.kg)
                )
                MyTextField(stringResource(R.string.date), catch.date)
                MyTextField(stringResource(R.string.time), catch.time)
                MyTextField(stringResource(R.string.fish_rod), catch.fishingRodType)
                MyTextField(stringResource(R.string.bait), catch.fishingBait)
                MyTextField(stringResource(R.string.lure), catch.fishingLure)
                Spacer(Modifier.size(5.dp))
            }

        }
    }

    @Composable
    fun MyCard(content: @Composable () -> Unit) {
        Card(
            elevation = 4.dp, shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(), content = content
        )
    }

    @Composable
    private fun PlaceInfo() {
        MyCard {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp).height(50.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.Place, stringResource(R.string.place))
                    UserProfile()
                    //Icon(Icons.Default.Check, stringResource(R.string.place))
                }
                Text("Точка 2", fontWeight = FontWeight.Bold)
                Text("Описание точки")
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }

    @Composable
    private fun Title(title: String, description: String, userId: String, date: String) {
        MyCard {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().size(50.dp)
                ) {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    UserProfile()
                    Spacer(modifier = Modifier.size(5.dp))
                }
                Text(
                    description, modifier = Modifier.fillMaxWidth(),
                    fontSize = MaterialTheme.typography.button.fontSize
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(date, fontSize = MaterialTheme.typography.caption.fontSize)
                }
            }
        }
    }

    @Composable
    private fun UserProfile() {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_fisher),
                contentDescription = stringResource(R.string.fisher),
                Modifier.fillMaxHeight().padding(10.dp)
            )
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    stringResource(R.string.fisher),
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.button.fontSize
                )
                Text(
                    "@" + stringResource(R.string.fisher),
                    fontSize = MaterialTheme.typography.caption.fontSize
                )
            }
        }
    }

    @Composable
    fun Photos(
        photos: List<Drawable>
        //clickedPhoto: SnapshotStateList<Painter>
    ) {
        LazyRow(modifier = Modifier.fillMaxSize()) {
            item { Spacer(modifier = Modifier.size(4.dp)) }
            items(items = photos) {
                ItemPhoto(
                    photo = it,
                    //clickedPhoto = clickedPhoto
                )
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
        MyCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)
                    .height(40.dp)
            ) {
                Text(
                    text,
                    modifier = Modifier.padding(start = 5.dp).align(Alignment.CenterVertically)
                )
                Text(
                    info,
                    modifier = Modifier.padding(end = 5.dp).align(Alignment.CenterVertically)
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
                    modifier = Modifier.fillMaxWidth().padding(end = 3.dp),
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
            title = { Text("Удаление улова") },
            text = { Text("Вы уверены, что хотите удалить данный улов?") },
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
        (requireActivity() as NavigationHolder).closeNav()
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}


