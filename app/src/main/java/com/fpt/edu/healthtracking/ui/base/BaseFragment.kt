package com.fpt.edu.healthtracking.ui.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.fpt.edu.healthtracking.StartActivity
import com.fpt.edu.healthtracking.api.AuthApi
import com.fpt.edu.healthtracking.api.RemoteDataSource
import com.fpt.edu.healthtracking.data.TokenRequest
import com.fpt.edu.healthtracking.data.UserPreferences
import com.fpt.edu.healthtracking.data.repository.AuthRepository
import com.fpt.edu.healthtracking.data.repository.BaseRepository
import com.fpt.edu.healthtracking.ui.home.HomePageActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class BaseFragment<VM: ViewModel, B: ViewBinding, R: BaseRepository> : Fragment() {


    protected lateinit var  userPreferences: UserPreferences
    protected lateinit var binding: B
    lateinit var viewModel: VM
    protected lateinit var remoteDataSource: RemoteDataSource
    protected lateinit var authApi: AuthApi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userPreferences = UserPreferences(requireContext())
        authApi = RemoteDataSource().buildApi(AuthApi::class.java)
        remoteDataSource = RemoteDataSource(userPreferences, authApi)
        binding = getFragmentBinding(inflater,container)
        val factory = ViewModelFactory(getFragmentRepository())
        viewModel = ViewModelProvider(this, factory).get(getViewModel())
        return binding.root
    }

    abstract fun getViewModel() : Class<VM>

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : B

    abstract fun getFragmentRepository() : R

    protected fun handleAuthenticationError() {
        lifecycleScope.launch {
            userPreferences.clearAuth()
            startActivity(Intent(requireActivity(), StartActivity::class.java))
            requireActivity().finish()
        }
    }
}