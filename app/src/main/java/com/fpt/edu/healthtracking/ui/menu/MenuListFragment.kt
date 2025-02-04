package com.fpt.edu.healthtracking.ui.menu

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.MenuAdapter
import com.fpt.edu.healthtracking.api.MenuApi
import com.fpt.edu.healthtracking.data.repository.MenuRepository
import com.fpt.edu.healthtracking.databinding.FragmentMenuListBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MenuListFragment : BaseFragment<MenuViewModel, FragmentMenuListBinding, MenuRepository>() {
    private lateinit var menuAdapter: MenuAdapter
    private var searchJob: Job? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        viewModel.loadMenus()
    }
    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupSearch()
    }
    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        menuAdapter = MenuAdapter().apply {
            onItemClick = { menu ->
                Log.d("Navigation", "Attempting to navigate to detail with id: ${menu.mealPlanId}")
                findNavController().navigate(
                    R.id.action_menuListFragment_to_menuDetailFragment,
                    bundleOf("meal_plan_id" to menu.mealPlanId)
                )
            }
        }

        binding.menuRecyclerView.apply {
            adapter = menuAdapter
            layoutManager = LinearLayoutManager(requireContext())

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.bottom = resources.getDimensionPixelSize(R.dimen.item_spacing)
                }
            })
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)
                    s?.toString()?.let { query ->
                        if (query.isEmpty()) {
                            viewModel.loadMenus()
                        } else {
                            viewModel.searchMenus(query)
                        }
                    }
                }
            }
        })

        binding.etSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = v.text.toString()
                viewModel.searchMenus(query)
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }


    private fun setupObservers() {
        viewModel.menus.observe(viewLifecycleOwner) { menus ->
            menuAdapter.submitList(menus)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getViewModel() = MenuViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMenuListBinding.inflate(inflater, container, false)

    override fun getFragmentRepository(): MenuRepository {
        val api = remoteDataSource.buildApi(MenuApi::class.java)
        return MenuRepository(api, userPreferences)
    }
}

