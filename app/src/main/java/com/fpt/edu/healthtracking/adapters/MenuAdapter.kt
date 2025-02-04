package com.fpt.edu.healthtracking.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.data.model.Menu
import com.fpt.edu.healthtracking.databinding.ItemMenuBinding

class MenuAdapter : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var menus: List<Menu> = emptyList()
    var onItemClick: ((Menu) -> Unit)? = null

    inner class MenuViewHolder(private val binding: ItemMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menu: Menu) {
            binding.apply {
                tvMenuTitle.text = menu.name

                tvCalories.text = "${menu.totalCalories} cal"

                tvMenuDescription.text = menu.shortDescription

                Glide.with(itemView.context)
                    .load(menu.mealPlanImage)
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.error_food)
                    .centerCrop()
                    .into(ivMenuImage)

                root.setOnClickListener {
                    onItemClick?.invoke(menu)
                }

                root.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            v.animate().scaleX(0.98f).scaleY(0.98f).setDuration(100).start()
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                        }
                    }
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(menus[position])
    }

    override fun getItemCount() = menus.size

    fun submitList(newMenus: List<Menu>) {
        val diffCallback = MenuDiffCallback(menus, newMenus)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        menus = newMenus
        diffResult.dispatchUpdatesTo(this)
    }
}
class MenuDiffCallback(
    private val oldList: List<Menu>,
    private val newList: List<Menu>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].mealPlanId == newList[newItemPosition].mealPlanId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}