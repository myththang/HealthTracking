package com.fpt.edu.healthtracking.ui.chat


import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpt.edu.healthtracking.databinding.FragmentChatBoxBinding
import kotlinx.coroutines.launch


class ChatBoxFragment : Fragment() {
    private var _binding: FragmentChatBoxBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBoxBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ChatMessageAdapter()
        setupUI()
        setupObservers()
        viewModel.sendWelcomeMessage()
    }


    private fun setupUI() {


        binding.rvMessages.apply {
            adapter = this@ChatBoxFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }


        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.etMessage.setText("")
            }
        }


        binding.etMessage.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                binding.btnSend.performClick()
                true
            } else {
                false
            }
        }

    }


    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messages.collect { messages ->
                adapter.setMessages(messages)
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.btnSend.isEnabled = !isLoading
                if (isLoading) {
                    adapter.addMessage(ChatMessage("Đang nhập...", false))
                } else {
                    adapter.removeLastMessage()
                }
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
            }
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
