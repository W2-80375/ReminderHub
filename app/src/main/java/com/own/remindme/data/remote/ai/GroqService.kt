package com.own.remindme.data.remote.ai

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.HttpException
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
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
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
        
        REPEAT TYPE GUIDANCE:
        When asking for repeatType, provide simple examples to the user in the language they are using:
        - If English: "How often should this repeat? (e.g., Every day, Weekly, Monthly)"
        - If Hindi: "यह कब कब दोहराना चाहिए? (जैसे कि: हर दिन, हर हफ्ते, या हर महीने)"
        
        Mapping for repeatType:
        - DAILY: "Every day", "Daily", "Har din", "Rozana"
        - ALTERNATE: "Every other day", "Ek din chodkar"
        - WEEKLY: "Every week", "Weekly", "Har hafte", "Saptahik"
        - MONTHLY: "Every month", "Monthly", "Har mahine"
        - YEARLY: "Every year", "Yearly", "Har saal"
        
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
        
        BILINGUAL SUPPORT (CRITICAL):
        - You MUST respond ONLY in the language the user is using.
        - DO NOT provide translations or bilingual responses.
        - If user speaks English, respond 100% in English.
        - If user speaks Hindi, respond 100% in Hindi (Devanagari).
        - NEVER mix both languages in a single response unless it is natural Hinglish.
        - Do not provide both "English: ..." and "Hindi: ..." in your output. Pick ONE.
        - Do not explicitly mention that you are switching languages; just speak naturally.
        
        Example:
        User: "Hello"
        Response: "Hi! What reminder should I set for you?" (English only)
        
        User: "नमस्ते"
        Response: "नमस्ते! मैं आपके लिए क्या रिमाइंडर सेट करूँ?" (Hindi only)
        
        Keep your verbal responses very short and friendly.
    """.trimIndent()

    private val messages = mutableListOf<GroqMessage>().apply {
        add(GroqMessage(role = "system", content = systemInstruction))
    }

    suspend fun processInput(input: String): String {
        Log.d("GroqService", "processInput() called with input: '$input'")
        
        if (apiKey.isBlank() || apiKey == "null") {
            Log.e("GroqService", "API Key is missing or invalid")
            return "Error: Groq API key is missing. Please add GROQ_API_KEY to local.properties."
        }
        
        // Provide the current date and time context to the AI
        val now = java.util.Date()
        val dateStr = java.text.SimpleDateFormat("EEEE, yyyy-MM-dd", java.util.Locale.getDefault()).format(now)
        val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(now)
        val contextInfo = "Today is $dateStr, and the current time is $timeStr. Use this for relative dates and to help clarify ambiguous times."
        
        if (messages.none { it.role == "system" && it.content.contains("Today is") }) {
            messages.add(GroqMessage(role = "system", content = contextInfo))
        }

        val userMsg = GroqMessage(role = "user", content = input)
        messages.add(userMsg)
        
        var lastException: Exception? = null
        val maxRetries = 3
        
        for (attempt in 1..maxRetries) {
            try {
                Log.d("GroqService", "Attempt $attempt: Sending request to Groq API...")
                val request = GroqRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = messages
                )
                
                val response = api.getChatCompletion("Bearer $apiKey", request)
                Log.d("GroqService", "Response received from Groq API")
                
                val responseText = response.choices.firstOrNull()?.message?.content ?: "Sorry, I couldn't process that."
                messages.add(GroqMessage(role = "assistant", content = responseText))
                
                Log.d("GroqService", "AI Response content: $responseText")
                return responseText
                
            } catch (e: HttpException) {
                Log.e("GroqService", "HTTP Exception (Attempt $attempt): ${e.code()}", e)
                if (e.code() == 401 || e.code() == 429) {
                    // Don't retry auth or rate limit errors
                    return when (e.code()) {
                        401 -> "Error: Unauthorized. Please check your Groq API key."
                        429 -> "Error: AI service limit exceeds for today try tommorrow or add manually"
                        else -> "Error: Server error ${e.code()}"
                    }
                }
                lastException = e
            } catch (e: Exception) {
                Log.e("GroqService", "Exception (Attempt $attempt) in processInput", e)
                lastException = e
            }
            
            // If we're here, we failed. Wait before retrying (exponential backoff)
            if (attempt < maxRetries) {
                val delayTime = attempt * 1000L // 1s, 2s...
                Log.d("GroqService", "Retrying in ${delayTime}ms...")
                kotlinx.coroutines.delay(delayTime)
            }
        }
        
        // If we exhausted retries, remove the last user message so the history stays clean for next attempt
        if (messages.lastOrNull() == userMsg) {
            messages.removeAt(messages.size - 1)
        }
        
        return "Error: ${lastException?.message ?: "Something went wrong. Please check your connection."}"
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
