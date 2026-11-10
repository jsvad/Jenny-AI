package com.jennyai.app

/**
 * Maps short Persian or English spoken commands to animation clips that are
 * actually present in the uploaded Jenny GLB asset.
 */
internal object JennyVoiceCommand {
    data class Action(
        val animationName: String?,
        val feedback: String
    )

    fun parse(phrase: String): Action? {
        val text = phrase.lowercase()
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace("‌", " ")
            .replace(Regex("[!،,.؟?؛:]+"), " ")
            .trim()
        if (text.isBlank()) return null

        return when {
            text == "walk" || text.contains("راه برو") || text.contains("قدم بزن") ->
                Action("walk", "باشه، جنی شروع به راه رفتن کرد.")
            text == "run" || text.contains("بدو") || text.contains("سریع بدو") ->
                Action("run", "باشه، جنی شروع به دویدن کرد.")
            text == "wave" || text.contains("دست تکان بده") ||
                text.contains("دستتو تکون بده") || text.contains("سلام کن") ->
                Action("wave", "سلام! جنی دست تکان می‌دهد.")
            text == "jump" || text.contains("بپر") || text.contains("پرش کن") ->
                Action("jump", "باشه، جنی می‌پرد.")
            text == "idle" || text == "stand" || text.contains("بایست") ||
                text.contains("بایستا") || text == "توقف" || text == "ایست" ->
                Action("idle", "جنی به حالت ایستاده برگشت.")
            text == "sit" || text.contains("بشین") || text.contains("بنشین") ->
                Action(null, "حرکت نشستن هنوز در فایل مدل وجود ندارد؛ فعلاً می‌توانم راه بروم، بدوم، بپرم یا دست تکان بدهم.")
            else -> null
        }
    }
}
