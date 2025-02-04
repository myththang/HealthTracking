package com.fpt.edu.healthtracking.ui.menu

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fpt.edu.healthtracking.R
import com.fpt.edu.healthtracking.StartActivity
import com.fpt.edu.healthtracking.data.UserPreferences
import kotlinx.coroutines.launch

class MoreMenuFragment : Fragment() {

    private lateinit var btnGraph: Button
    private lateinit var btnExercisePlan: Button
    private lateinit var btnGoal: Button
    private lateinit var btnMealPlan: Button
    private lateinit var btnChangePassword: Button
    private lateinit var tvFullName: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnChat: Button
    private lateinit var btnChatBot: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_more_menu, container, false)

        // Initialize the buttons and TextView
        btnGraph = view.findViewById(R.id.btnGraph)
        btnExercisePlan = view.findViewById(R.id.btnExercisePlan)
        btnGoal = view.findViewById(R.id.btnGoal)
        btnMealPlan = view.findViewById(R.id.btnMealPlan)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        tvFullName = view.findViewById(R.id.usernameText)
        btnLogout= view.findViewById(R.id.btnLogout)
        btnChat= view.findViewById(R.id.btnChat)
        btnChatBot= view.findViewById(R.id.btnChatBot)
        // Retrieve the full name from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", MODE_PRIVATE)
        tvFullName.text = sharedPreferences.getString("fullname", "") ?: "No Name"

        // Set up click listeners for buttons
        setupButtonListeners()

        return view
    }

    private fun setupButtonListeners() {
        btnGraph.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_navigation_graphFragment)
        }

        btnExercisePlan.setOnClickListener {
          findNavController().navigate(R.id.action_navigation_menumoremenu_to_exercisePlanListFragment)
        }

        btnGoal.setOnClickListener {
            findNavController().navigate(R.id.action_menu_to_navigation_goalFragment)
        }

        btnMealPlan.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_menumoremenu_to_menuListFragment)
        }

        btnChangePassword.setOnClickListener {
        findNavController().navigate(R.id.action_menu_to_navigation_forgetPassword)

        }

        btnChat.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_menumoremenu_to_chatBoxTrainerFragment)
        }

        btnChatBot.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_menumoremenu_to_chatBoxFragment)
        }

        btnLogout.setOnClickListener {
            val userPreferences = UserPreferences(requireContext())

            lifecycleScope.launch {
                userPreferences.clearAuth()
                startActivity(Intent(requireContext(), StartActivity::class.java))
                requireActivity().finish()
            }

        }
    }
}
