package com.example.data.remote

import com.example.util.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withTimeout

object MeenaAiRepository {

    private val dangerKeywords = listOf(
        "help me", "emergency", "following me", "stalking", "chasing", "attacking",
        "hurt me", "assault", "unsafe right now", "call police", "sos", "trapped",
        "kidnap", "threat", "bleeding", "someone is outside", "danger", "ஆபத்து", "உதவி"
    )

    fun detectImmediateDangerIntent(message: String): Boolean {
        val lower = message.lowercase()
        return dangerKeywords.any { lower.contains(it) }
    }

    private fun buildSystemPrompt(language: AppLanguage, cityContext: String, riskContext: String): String {
        val langRule = if (language == AppLanguage.TA) {
            "Respond primarily in clear, compassionate Tamil (with English legal terms/numbers preserved clearly)."
        } else {
            "Respond in clear, calm, supportive English."
        }
        return """
            You are Meena, the compassionate safety, legal-rights, and crisis-guidance companion inside the CYBER WOMEN app.
            $langRule
            Context: Active monitoring city is $cityContext. Current Shield Protocol advisory: $riskContext.
            Strict safety rules:
            1. You MUST NEVER claim to dispatch police, call emergency services automatically, or guarantee live safety.
            2. If the user expresses immediate danger, instruct them clearly to tap Call 112 or activate the Shield SOS button immediately, and keep instructions short and actionable.
            3. Only quote verified Indian/Puducherry/Tamil Nadu helplines from the app catalog:
               - 112 (National Emergency ERSS), 181 (Women Helpline), 1091 (Singapen Special Force / Women Police), 14490 (NCW), 1930 (Cyber Crime), 1098 (Childline), 1094 (Missing Women/Children TN), 15100 (NALSA Free Legal Aid), 14416 (Tele-MANAS Mental Health), 0413-2336178 (All Women PS Puducherry), 155260 (Puducherry Cyber Cell).
            4. Frame Shield Protocol risk insights strictly as historical aggregate pattern advisories, never individual crime predictions.
            5. Keep answers concise, structured with bullet points when listing steps, and supportive.
        """.trimIndent()
    }

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun streamReply(
        historyPairs: List<Pair<String, String>>,
        userMessage: String,
        language: AppLanguage,
        cityContext: String,
        riskContext: String
    ): Flow<String> {
        val systemInstruction = buildSystemPrompt(language, cityContext, riskContext)
        val promptContent = content {
            text(systemInstruction)
            historyPairs.takeLast(6).forEach { (role, msg) ->
                text("$role: $msg")
            }
            text("User: $userMessage\nMeena:")
        }
        return withTimeout(60_000L) {
            model.generateContentStream(promptContent).mapNotNull { chunk -> chunk.text }
        }
    }
}
