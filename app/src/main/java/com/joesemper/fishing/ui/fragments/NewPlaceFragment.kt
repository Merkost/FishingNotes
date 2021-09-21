package com.joesemper.fishing.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.joesemper.fishing.databinding.FragmentNewPlaceBinding
import com.joesemper.fishing.domain.NewPlaceViewModel
import com.joesemper.fishing.domain.viewstates.BaseViewState
import com.joesemper.fishing.model.entity.raw.RawMapMarker
import com.joesemper.fishing.utils.NavigationHolder
import kotlinx.coroutines.flow.collect
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

class NewPlaceFragment : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScope()
    private val viewModel: NewPlaceViewModel by viewModel()

    private lateinit var binding: FragmentNewPlaceBinding

    private val args: NewPlaceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPlaceBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        hideNavigation()
        initTitle()
        initLoadingLayout()
        setOnFabClickListener()
    }


    private fun hideNavigation() {
        val navigationHolder = activity as NavigationHolder
        navigationHolder.hideNav()
    }

    private fun showNavigation() {
        val navigationHolder = activity as NavigationHolder
        navigationHolder.showNav()
    }

    private fun initTitle() {
        val title = args.title
        binding.etTitle.setText(title)
    }

    private fun initLoadingLayout() {
        binding.loadingLayout.setOnClickListener { }
    }

    private fun setOnFabClickListener() {
        binding.floatingActionButton.setOnClickListener {
            binding.floatingActionButton.shrink()
            subscribeOnViewModel()
            saveNewPlace()
        }
    }

    private fun subscribeOnViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.subscribe().collect { state ->
                when (state) {
                    is BaseViewState.Loading -> onLoading()
                    is BaseViewState.Success<*> -> onSuccess()
                    is BaseViewState.Error -> onError()
                }

            }
        }
    }

    private fun onLoading() {
        binding.loadingLayout.visibility = View.VISIBLE
    }

    private fun onSuccess() {
        showNavigation()
        findNavController().popBackStack()
    }

    private fun onError() {

    }

    private fun saveNewPlace() {
        viewModel.addNewMarker(
            RawMapMarker(
                title = binding.etTitle.text.toString(),
                description = binding.etDescription.text.toString(),
                latitude = args.coordinats.latitude,
                longitude = args.coordinats.longitude
            )
        )
    }


}