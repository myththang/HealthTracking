package com.fpt.edu.healthtracking.ui.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.adapters.onboarding.OnboardingAdapter
import com.fpt.edu.healthtracking.data.repository.OnboardingRepository
import com.fpt.edu.healthtracking.databinding.FragmentOnboardBinding
import com.fpt.edu.healthtracking.ui.base.BaseFragment


class OnboardingFragment : BaseFragment<OnboardingViewModel, FragmentOnboardBinding, OnboardingRepository>() {

    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun getViewModel() = OnboardingViewModel::class.java

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentOnboardBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() =
        OnboardingRepository()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(onBoardingIsFinished()){
            findNavController().navigate(R.id.action_onboardFragment_to_welcomeFragment)
        }

        setupOnboardingPager()
        setupButtons()
        observeViewModel()
    }

    private fun setupOnboardingPager() {
        onboardingAdapter = OnboardingAdapter()
        binding.viewPager.adapter = onboardingAdapter
        binding.dotsIndicator.setViewPager2(binding.viewPager)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.setCurrentPage(position)
            }
        })
    }

    private fun setupButtons() {
        binding.buttonNext.setOnClickListener {
            if (binding.viewPager.currentItem < onboardingAdapter.itemCount - 1) {
                binding.viewPager.currentItem++
            } else {
                viewModel.completeOnboarding()
            }
        }

        binding.buttonBack.setOnClickListener {
            viewModel.completeOnboarding()
        }
    }

    private fun observeViewModel() {
        viewModel.currentPage.observe(viewLifecycleOwner) { page ->
            updateButtons(page)
        }
        viewModel.onboardingCompleted.observe(viewLifecycleOwner) { completed ->
            if (completed) {
                navigateToWelcome()
            }
        }

        viewModel.onboardingPages.observe(viewLifecycleOwner) { pages ->
            onboardingAdapter.submitList(pages)
        }
    }
    private fun updateButtons(position: Int) {
        if (position == 0) {
            binding.buttonBack.text = "Bỏ qua"
            binding.buttonBack.setOnClickListener { navigateToWelcome() }
        } else {
            binding.buttonBack.text = "Quay lại"
            binding.buttonBack.setOnClickListener { binding.viewPager.currentItem-- }
        }
    }

    private fun navigateToWelcome() {
        val sharedPreferences = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("finish",true)
        editor.apply()
        findNavController().navigate(R.id.action_onboardFragment_to_welcomeFragment)
    }

    private fun onBoardingIsFinished(): Boolean {
        val sharedPreferences = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("finish",false)
    }

}
