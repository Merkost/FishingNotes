package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.joesemper.fishing.R
import com.joesemper.fishing.domain.UserPlaceViewModel
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.ui.theme.primaryFigmaBackgroundTint
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.primaryFigmaLightColor
import com.joesemper.fishing.utils.NavigationHolder
import com.joesemper.fishing.utils.showToast
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserPlaceFragment : Fragment(), AndroidScopeComponent {

    private val args: UserPlaceFragmentArgs by navArgs()

    override val scope: Scope by fragmentScope()
    private val viewModel: UserPlaceViewModel by viewModel()

    private val catches: List<UserCatch> =
        listOf(
            UserCatch(),
            UserCatch(),
            UserCatch(),
            UserCatch(),
            UserCatch(),
            UserCatch(),
            UserCatch()
        ) // для теста

    private lateinit var place: UserMapMarker

    companion object {
        private const val TAG = "PLACE"
    }

    fun newInstance(place: UserMapMarker): Fragment {
        val args = bundleOf(TAG to place)
        val fragment = UserPlaceFragment()
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        place = args.userMapMarker
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    UserPlaceScreen()
                }
            }
        }
    }

    @Composable
    fun UserPlaceScreen() {
        Scaffold(
            topBar = { AppBar() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp).background(primaryFigmaBackgroundTint)
            ) {
//                Card(elevation = 4.dp) {
//                    place.description?.let { it1 ->
//                        Title(place.title,
//                            it1, place.userId)
//                    }
//                }
                //Title(place.title, place.description!!, place.userId)
                Column {
                    PlaceInfo()
                    Buttons()
                }
                Catches(catches)
            }

        }
    }

    @Composable
    fun Buttons() {
        Row(
            modifier = Modifier
                .fillMaxWidth().height(60.dp).background(primaryFigmaColor),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MyButton(painterResource(R.drawable.ic_baseline_navigation_24),
                stringResource(R.string.navigate)) {
                showToast(requireContext(), "New Catch. Not Yet Implemented")
            }
            MyButton(painterResource(R.drawable.ic_baseline_share_24),
                stringResource(R.string.share)) {
                showToast(requireContext(), "New Catch. Not Yet Implemented")
            }
            MyButton(painterResource(R.drawable.ic_fish),
                stringResource(R.string.new_catch)) {
                showToast(requireContext(), "New Catch. Not Yet Implemented")
            }




            /*Button(modifier = Modifier
                .fillMaxWidth(1.0F)
                .height(55.dp),
                onClick = {
                    Toast.makeText(
                        requireContext(),
                        "New Catch. Not Yet Implemented",
                        Toast.LENGTH_LONG
                    ).show()
                }) {
                Column() {
                    Image(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        painter = painterResource(R.drawable.ic_fish),
                        contentDescription = stringResource(R.string.new_catch)
                    )
                    Text(fontSize = 8.sp, text = stringResource(R.string.new_catch))
                }
            }*/

        }
    }
    @Composable
    fun MyButton(painter: Painter, text: String, onClick: () -> Unit) {

        Button(modifier = Modifier.fillMaxHeight(),
            onClick = onClick, border = BorderStroke(0.dp, color = Color.Transparent),
            elevation = ButtonDefaults.elevation(0.dp)) {
            Column() {
                Image(
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(30.dp),
                    painter = painter,
                    contentDescription = text
                )
                Text(fontSize = 10.sp, text = text)
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

    /*@Composable
    private fun Title(title: String, description: String, userId: String) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            )
            {
                Image(
                    painter = painterResource(R.drawable.ic_fish_on_map_red),
                    contentDescription = stringResource(R.string.marker_icon),
                    Modifier
                        .size(50.dp)
                        .padding(start = 15.dp)
                )
                //UserProfile(userId)
            }
            Text(
                title,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 5.dp)
            )
            Text(
                description,
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth()
            )
        }
    }*/

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

/*  @Composable
  private fun UserProfile(userId: String) {
      // TODO получить имя и фото по userId
      Row {
          Image(
              painter = painterResource(R.drawable.ic_fisher),
              contentDescription = stringResource(R.string.fisher),
              Modifier.size(50.dp)
          )
          Spacer(Modifier.size(2.dp))
          Column(verticalArrangement = Arrangement.SpaceBetween) {
              Text(
                  stringResource(R.string.fisher),
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
              )
              Text("@" + stringResource(R.string.fisher), fontSize = 10.sp)
          }
      }
  }*/

    @ExperimentalCoilApi
    @Composable
    fun Catches(
        catches: List<UserCatch>,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)
/*                .padding(
                    start = 5.dp,
                    top = 200.dp,
                    end = 5.dp
                )*/
        ) {
            items(items = catches) {
                ItemCatch(
                    catch = it
                )
            }
        }
    }

    @ExperimentalCoilApi
    @Composable
    fun ItemCatch(
        catch: UserCatch,
        // TODO получить описание, вес и фото UserCatch
    ) {
        Card(elevation = 0.dp) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Text(
                    text = "Судак",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                    ) {
                        Image(painter = rememberImagePainter(R.drawable.ulov),
                            contentDescription = stringResource(R.string.catch_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(5.dp))
                                .clickable { /*clickedPhoto(photo)*/ })
                    }
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        Text(
                            text = "4.650 кг",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(start = 5.dp)
                        )
                        Text(
                            text = "22.08.2021",
                            fontSize = 10.sp,
                            modifier = Modifier
                                .padding(start = 5.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun AppBar() {
        TopAppBar(
            title = { Text(text = stringResource(R.string.place)) },
            navigationIcon = {
                IconButton(onClick = { findNavController().popBackStack() }, content = {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = getString(R.string.back)
                    )
                })
            },
            actions = {
                Row(
                    modifier = Modifier
                        .padding(end = 3.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Edit, stringResource(R.string.edit),
                        modifier = Modifier
                            .clickable {
                                showToast(
                                    requireContext(),
                                    "Not Yet Implemented"
                                )
                            }
                            .size(25.dp))
                    Spacer(modifier = Modifier.size(15.dp))
                    Icon(Icons.Default.Delete, stringResource(R.string.edit),
                        modifier = Modifier
                            .clickable {
                                showToast(
                                    requireContext(),
                                    "Not Yet Implemented"
                                )
                            }
                            .size(25.dp))
                }
            })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as NavigationHolder).closeNav()
        //setInitialData()
    }

    override fun onDetach() {
        super.onDetach()
        (requireActivity() as NavigationHolder).showNav()
    }
}


