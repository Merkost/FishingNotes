package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.joesemper.fishing.R
import com.joesemper.fishing.databinding.FragmentCatchesBinding
import com.joesemper.fishing.domain.UserCatchesViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.content.UserCatch
import com.joesemper.fishing.model.entity.content.UserMapMarker
import com.joesemper.fishing.ui.adapters.UserCatchesRVAdapter
import com.joesemper.fishing.ui.theme.FigmaTheme
import com.joesemper.fishing.ui.theme.primaryFigmaColor
import com.joesemper.fishing.ui.theme.secondaryFigmaColor
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class UserCatchesFragment : Fragment(), AndroidScopeComponent {

    companion object {
        private const val TAG = "CATCHES"

        fun newInstance(): Fragment {
            return UserCatchesFragment()
        }
    }

    override val scope: Scope by fragmentScope()
    private val viewModel: UserCatchesViewModel by viewModel()

    private lateinit var binding: FragmentCatchesBinding

    private lateinit var adapter: UserCatchesRVAdapter

    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FigmaTheme {
                    UserCatchesScreen()
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun UserCatchesScreen() {
        Scaffold() {
            val uiState = viewModel.viewStateFlow.collectAsState()
            when (uiState.value) {
                is BaseViewState.Loading ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                is BaseViewState.Success<*> -> UserCatches(
                    (uiState.value as BaseViewState.Success<*>).data as List<UserCatch>)
                is BaseViewState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "An error ocurred fetching the catches.")
                    }
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    fun UserCatches(
        catches: List<UserCatch>
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item { ItemAddCatch {  onAddNewCatchClick() } }
            items(items = catches) {
                ItemCatch (
                    catch = it
                )
            }
        }
    }

    @Composable
    fun MyCard(content: @Composable () -> Unit) {
        Card(elevation = 8.dp, modifier = Modifier.fillMaxWidth().padding(4.dp), content = content)
    }

    @Composable
    fun ItemAddCatch(addCatch: () -> Unit) {
        MyCard {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(110.dp).fillMaxWidth().clickable { addCatch() }
                    .padding(5.dp)
            ) {
                Column(verticalArrangement = Arrangement.Center) {
                    Icon(
                        painterResource(R.drawable.ic_add_catch),
                        stringResource(R.string.new_catch),
                        modifier = Modifier.weight(2f).align(Alignment.CenterHorizontally)
                            .size(50.dp),
                        tint = primaryFigmaColor
                    )
                    Text(stringResource(R.string.new_catch), modifier = Modifier.weight(1f))
                }
            }
        }

    }

    @ExperimentalAnimationApi
    @Composable
    fun ItemCatch(catch: UserCatch) {
        MyCard {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(75.dp).fillMaxWidth().clickable { onCatchItemClick(catch) }
                    .padding(5.dp)
            ) {
                Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(75.dp).padding(5.dp)) {
                        Icon(
                            painterResource(R.drawable.ic_no_photo_vector),
                            stringResource(R.string.place),
                            modifier = Modifier.padding(5.dp).fillMaxSize(),
                            tint = secondaryFigmaColor
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Column {
                            Text(catch.title, fontWeight = Bold)
                            Text(stringResource(R.string.amount) + ": " + catch.fishAmount)
                        }
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.ic_baseline_location_on_24),
                                stringResource(R.string.place),
                                modifier = Modifier.size(20.dp),
                                tint = secondaryFigmaColor
                            )
                            Text("Place", color = primaryFigmaColor, fontSize = 12.sp)
                        }

                    }
                }
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(text = "0.0 KG", fontWeight = Bold)
                    Text("14:06", color = primaryFigmaColor, fontSize = 12.sp)
                }
            }
        }
    }

    private fun onLoading() {

    }

    private fun onSuccess(catches: List<UserCatch>) {

    }

    private fun onError(error: Throwable) {

    }

    private fun onAddNewCatchClick() {
        val action =
            NotesFragmentDirections.actionNotesFragmentToNewCatchDialogFragment(
                UserMapMarker()
            )
        findNavController().navigate(action)
    }

    private fun onCatchItemClick(catch: UserCatch) {
        val action =
            NotesFragmentDirections.actionNotesFragmentToUserCatchFragment(catch)
        findNavController().navigate(action)
    }


}