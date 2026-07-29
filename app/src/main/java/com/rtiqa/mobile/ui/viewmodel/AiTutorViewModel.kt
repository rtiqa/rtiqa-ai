package com.rtiqa.mobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rtiqa.mobile.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val sender: Sender,
    val text: String,
    val timestamp: String = "الآن"
) {
    enum class Sender { USER, AI }
}

class AiTutorViewModel(
    private val aiRepository: AiRepository = AiRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "1",
                sender = ChatMessage.Sender.AI,
                text = "مرحباً بك في المعلم الذكي لمنصة رتقاء! 👋\n\nأنا هنا لمساعدتك في تبسيط مفاهيم الذكاء الاصطناعي، والفيزياء، والرياضيات، وهندسة البرمجيات، وإعداد خطط الدراسة، والإجابة عن تساؤلاتك أوفلاين أو أونلاين. كيف يمكنني مساعدتك في رحلتك التعليمية اليوم؟"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun sendMessage(prompt: String = _inputText.value, isArabic: Boolean = true) {
        if (prompt.isBlank()) return

        val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            sender = ChatMessage.Sender.USER,
            text = prompt
        )
        _messages.value = _messages.value + userMsg
        _inputText.value = ""
        _isLoading.value = true

        viewModelScope.launch {
            val reply = aiRepository.askAiTutor(prompt, isArabic)
            val aiMsg = ChatMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                sender = ChatMessage.Sender.AI,
                text = reply
            )
            _messages.value = _messages.value + aiMsg
            _isLoading.value = false
        }
    }
}
