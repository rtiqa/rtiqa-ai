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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun dismissError() {
        _errorMessage.value = null
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
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = aiRepository.askAiTutor(prompt, isArabic)
                
                if (result.error != null) {
                    _errorMessage.value = if (isArabic) "فشل الاتصال السحابي. تم تفعيل المحرك المحلي." else "Cloud connection failed. Local engine activated."
                }
                
                val finalReply = if (result.isOfflineFallback) {
                    result.text + if (isArabic) "\n\n(تم الرد عبر المحرك المحلي ⚡)" else "\n\n(Replied via Local Engine ⚡)"
                } else {
                    result.text
                }
                
                val aiMsg = ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    sender = ChatMessage.Sender.AI,
                    text = finalReply
                )
                _messages.value = _messages.value + aiMsg
            } catch (e: Exception) {
                _errorMessage.value = if (isArabic) "عذراً، حدث خطأ غير متوقع." else "Sorry, an unexpected error occurred."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
