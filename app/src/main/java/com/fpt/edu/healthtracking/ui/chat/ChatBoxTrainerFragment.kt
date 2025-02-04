    package com.fpt.edu.healthtracking.ui.chat

    import android.app.AlertDialog
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.RatingBar
    import android.widget.Toast
    import androidx.core.view.isVisible
    import androidx.lifecycle.lifecycleScope
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.fpt.edu.healthtracking.R
    import com.fpt.edu.healthtracking.adapters.TrainerChatAdapter
    import com.fpt.edu.healthtracking.ui.base.BaseFragment
    import com.fpt.edu.healthtracking.databinding.FragmentChatBoxTrainerBinding
    import com.fpt.edu.healthtracking.api.ChatApi
    import com.fpt.edu.healthtracking.data.repository.ChatRepository
    import kotlinx.coroutines.launch

    class ChatBoxTrainerFragment :
        BaseFragment<ChatTrainerViewModel, FragmentChatBoxTrainerBinding, ChatRepository>() {

        private lateinit var adapter: TrainerChatAdapter
        private var selectedConsultationType: ConsultationType? = null

        enum class ConsultationType(val title: String) {
            NUTRITION("Tư vấn dinh dưỡng"),
            EXERCISE("Tư vấn luyện tập"),
            GENERAL("Tư vấn tổng quát")
        }

        override fun getViewModel() = ChatTrainerViewModel::class.java

        override fun getFragmentBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
        ) = FragmentChatBoxTrainerBinding.inflate(inflater, container, false)

        override fun getFragmentRepository(): ChatRepository {
            val api = remoteDataSource.buildApi(ChatApi::class.java)
            val chatHub = ChatHubClient("https://healthtrack-hydtdue4ede8b5fp.southeastasia-01.azurewebsites.net/")
            return ChatRepository(api, userPreferences,chatHub = chatHub)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            setupUI()
            setupCategorySelection()
            setupObservers()
            viewModel.checkExistingChat()
        }

        private fun setupCategorySelection() {
            binding.apply {
                categoryContainer.isVisible = false
                chatContainer.isVisible = false
                waitingContainer.isVisible = false

                btnNutrition.setOnClickListener {
                    handleCategorySelection(ConsultationType.NUTRITION)
                }

                btnExercise.setOnClickListener {
                    handleCategorySelection(ConsultationType.EXERCISE)
                }

                btnGeneral.setOnClickListener {
                    handleCategorySelection(ConsultationType.GENERAL)
                }
            }
        }

        private fun handleCategorySelection(type: ConsultationType) {
            selectedConsultationType = type

            binding.tvSelectedCategory.text = "Loại tư vấn: ${type.title}"

            binding.categoryContainer.isVisible = false

            viewModel.createNewChat(type.title)

            binding.etMessage.hint = "Nhập câu hỏi về ${type.title.lowercase()}..."
        }

        private fun setupUI() {
            adapter = TrainerChatAdapter()
            binding.apply {
                rvMessages.adapter = adapter
                rvMessages.layoutManager = LinearLayoutManager(context)

                btnBack.setOnClickListener {
                    findNavController().navigateUp()
                }

                btnSend.setOnClickListener {
                    val message = etMessage.text.toString().trim()
                    if (message.isNotEmpty()) {
                        viewModel.sendMessage(message)
                        etMessage.setText("")
                    }
                }

                btnEndChat.setOnClickListener {
                    viewModel.endChat()
                }
            }
        }

        private fun setupObservers() {
            viewModel.messages.observe(viewLifecycleOwner) { messages ->
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.size - 1)
                }
            }

            viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.isVisible = isLoading
            }

            viewModel.chatState.observe(viewLifecycleOwner) { state ->
                binding.apply {
                    when (state) {
                        ChatTrainerViewModel.ChatState.NO_CHAT -> {
                            categoryContainer.isVisible = true
                            waitingContainer.isVisible = false
                            chatContainer.isVisible = false
                        }
                        ChatTrainerViewModel.ChatState.WAITING_FOR_TRAINER -> {
                            categoryContainer.isVisible = false
                            waitingContainer.isVisible = true
                            chatContainer.isVisible = false
                            tvWaitingMessage.text = "Xin cảm ơn bạn đã đăng ký tư vấn " +
                                    "${selectedConsultationType?.title?.lowercase() ?: ""}. " +
                                    "Hiện tại, chúng tôi đang trong quá trình tìm kiếm trainer phù hợp " +
                                    "và sẽ thông báo cho bạn sớm nhất có thể."
                        }
                        is ChatTrainerViewModel.ChatState.ACTIVE_CHAT -> {
                            categoryContainer.isVisible = false
                            waitingContainer.isVisible = false
                            chatContainer.isVisible = true
                            tvSelectedCategory.isVisible = true
                            tvSelectedCategory.isVisible = true
                        }
                    }
                }
            }
            viewModel.error.observe(viewLifecycleOwner) { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            }

            viewModel.showRating.observe(viewLifecycleOwner) { shouldShow ->
                if (shouldShow) {
                    showRatingDialog()
                }
            }
        }

        private fun showRatingDialog() {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Đánh giá trainer")
                .setView(R.layout.dialog_rating)
                .setCancelable(false)
                .create()

            dialog.show()

            val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBar)
            val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)

            btnSubmit?.setOnClickListener {
                val rating = ratingBar?.rating?.toInt() ?: 0
                viewModel.rateChat(rating)
                dialog.dismiss()
            }
        }
    }