package com.jennyai.app

import java.util.Locale

/** Offline Persian voice-command router for Jenny's avatar. */
data class JennyAvatarCommand(val animationName: String, val confirmation: String)

object JennyCommandInterpreter {
    fun normalize(input: String): String = input
        .lowercase(Locale("fa", "IR"))
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace('ۀ', 'ه')
        .replace(Regex("[،,.!?؟؛:]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    fun parse(input: String): JennyAvatarCommand? {
        var phrase = normalize(input)
        if (phrase.isBlank()) return null
        if (listOf("نمیخوام", "نمی‌خوام", "نکن", "نرو", "ننشین").any { phrase.contains(it) }) return null
        val addressedToJenny = phrase.startsWith("جنی ")
        if (addressedToJenny) phrase = phrase.removePrefix("جنی ").trim()
        phrase = phrase.removePrefix("لطفا ").removePrefix("لطفاً ").removePrefix("خواهشا ").trim()
        val commands = linkedMapOf(
            "راه برو" to JennyAvatarCommand("walk", "باشه، شروع می‌کنم به راه رفتن."),
            "راه رفتن" to JennyAvatarCommand("walk", "باشه، شروع می‌کنم به راه رفتن."),
            "قدم بزن" to JennyAvatarCommand("walk", "باشه، قدم می‌زنم."),
            "برو" to JennyAvatarCommand("walk", "باشه، حرکت می‌کنم."),
            "بدو" to JennyAvatarCommand("run", "باشه، شروع می‌کنم به دویدن."),
            "بایست" to JennyAvatarCommand("idle", "ایستادم."),
            "وایسا" to JennyAvatarCommand("idle", "ایستادم."),
            "توقف کن" to JennyAvatarCommand("idle", "حرکتم متوقف شد."),
            "متوقف شو" to JennyAvatarCommand("idle", "حرکتم متوقف شد."),
            "بشین" to JennyAvatarCommand("sit", "باشه، می‌شینم."),
            "بنشین" to JennyAvatarCommand("sit", "باشه، می‌شینم."),
            "بلند شو" to JennyAvatarCommand("stand", "بلند شدم."),
            "دست تکون بده" to JennyAvatarCommand("wave", "سلام!"),
            "دستت رو تکون بده" to JennyAvatarCommand("wave", "سلام!"),
            "برگرد" to JennyAvatarCommand("turn", "دارم برمی‌گردم.")
        )
        commands[phrase]?.let { return it }
        if (!addressedToJenny) return null
        return commands.entries.firstOrNull { phrase.contains(it.key) }?.value
    }
}
