package com.own.remindme.data.remote.ai

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton
import com.own.remindme.BuildConfig

@Singleton
class GroqService @Inject constructor() {

    val apiKey = BuildConfig.GROQ_API_KEY

    private val api: GroqApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GroqApi::class.java)
    }

    private val systemInstruction = """
        You are an expert Reminder Assistant for the "ReminderHub" app. 
        Your goal is to help users create reminders through natural conversation.
        
        When a user speaks to you:
        1. Identify if they want to create a reminder.
        2. Extract the following fields:
           - title (String): The specific task or item (e.g., "Take Paracetamol", "Service the car").
           - description (String): Additional details.
           - date (Format: YYYY-MM-DD): The specific date.
           - time (Format: HH:mm): The specific time.
           - repeatType (DAILY, ALTERNATE, WEEKLY, TWO_WEEKS, THREE_WEEKS, MONTHLY, THREE_MONTHS, SIX_MONTHS, YEARLY): How often it repeats.
           - category (MEDICINE, VEHICLE, BILL, DOCUMENT, HEALTH, BIRTHDAY, SHOPPING, CUSTOM): The broad category the reminder belongs to.
             If the user's specific category is not in this list, set the category to "CUSTOM" and include their specific category name at the beginning of the description.
        
        Important distinction: 
        If a user says "Vehicle" or "Medicine", they are likely specifying the CATEGORY. Do not mistake a category name for the TITLE. 
        If the title is missing but the category is provided, politely ask "What should I name this [Category] reminder?".
        
        If any CRITICAL field (title, date, time, repeatType) is missing, ask the user for it politely and briefly.
        repeatType is MANDATORY. 
        
        Time Ambiguity:
        If the user provides a time without specifying AM or PM (e.g., "3 o'clock", "at 5"), you MUST ask if they mean AM or PM before proceeding, unless it is clearly implied by context (like "breakfast at 8" or "dinner at 7").
        
        If you have enough information to create the reminder, respond with a JSON block at the end of your message in this format:
        {
          "status": "READY",
          "data": {
            "title": "...",
            "date": "...",
            "time": "...",
            "repeatType": "...",
            "category": "...",
            "description": "..."
          }
        }
        
        Wait until you have the TITLE before setting the status to READY.
        
        If details are missing, respond with:
        {
          "status": "INCOMPLETE",
          "message": "Your polite question to the user"
        }
        
        Keep your verbal responses very short and friendly.
    """.trimIndent()

    private val messages = mutableListOf<GroqMessage>().apply {
        add(GroqMessage(role = "system", content = systemInstruction))
    }

    suspend fun processInput(input: String): String {
        Log.d("GroqService", "processInput() called with input: '$input'")
        
        // Provide the current date and time context to the AI
        val now = java.util.Date()
        val dateStr = java.text.SimpleDateFormat("EEEE, yyyy-MM-dd", java.util.Locale.getDefault()).format(now)
        val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(now)
        val contextInfo = "Today is $dateStr, and the current time is $timeStr. Use this for relative dates and to help clarify ambiguous times."
        
        return try {
            if (messages.none { it.role == "system" && it.content.contains("Today is") }) {
                messages.add(GroqMessage(role = "system", content = contextInfo))
            }

            val userMsg = GroqMessage(role = "user", content = input)
            messages.add(userMsg)
            Log.d("GroqService", "Current conversation history size: ${messages.size}")
            
            val request = GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = messages
            )
            Log.d("GroqService", "Sending request to Groq API...")
            val response = api.getChatCompletion("Bearer $apiKey", request)
            Log.d("GroqService", "Response received from Groq API")
            
            val responseText = response.choices.firstOrNull()?.message?.content ?: "Sorry, I couldn't process that."
            
            messages.add(GroqMessage(role = "assistant", content = responseText))

            Log.d("GroqService", "AI Response content: $responseText")
            responseText
        } catch (e: Exception) {
            Log.e("GroqService", "Exception in processInput", e)
            "Error: ${e.message}"
        }
    }

    fun clearChat() {
        Log.d("GroqService", "clearChat() called. Resetting messages list.")
        messages.clear()
        messages.add(GroqMessage(role = "system", content = systemInstruction))
    }
}

interface GroqApi {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqRequest
    ): GroqResponse
}

data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1024
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val id: String,
    val choices: List<GroqChoice>
)

data class GroqChoice(
    val message: GroqMessage,
    val finish_reason: String
)
