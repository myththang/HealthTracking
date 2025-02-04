package com.fpt.edu.healthtracking.adapters.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fpt.edu.healthtracking.databinding.ItemOnboardingPageBinding
import com.fpt.edu.healthtracking.data.model.OnboardingPage

class OnboardingAdapter : ListAdapter<OnboardingPage, OnboardingAdapter.OnboardingViewHolder>(
    OnboardingDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OnboardingViewHolder(private val binding: ItemOnboardingPageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(page: OnboardingPage) {
            binding.imageOnboarding.setImageResource(page.imageResId)
            binding.textTitle.text = page.title
            binding.textDescription.text = page.description
        }
    }

    class OnboardingDiffCallback : DiffUtil.ItemCallback<OnboardingPage>() {
        override fun areItemsTheSame(oldItem: OnboardingPage, newItem: OnboardingPage): Boolean {
            return oldItem.imageResId == newItem.imageResId
        }

        override fun areContentsTheSame(oldItem: OnboardingPage, newItem: OnboardingPage): Boolean {
            return oldItem == newItem
        }
    }
}