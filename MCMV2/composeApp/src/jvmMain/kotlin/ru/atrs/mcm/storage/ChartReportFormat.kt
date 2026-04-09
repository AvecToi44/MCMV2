package ru.atrs.mcm.storage

data class ChartReportStep(
    val durationMs: Int,
    val comment: String
)

private const val STEPS_PREFIX = "#steps#"
private const val STANDARD_PREFIX = "#standard#"
private const val VISIBILITY_PREFIX = "#visibility#"

fun buildStandardHeaderLine(standardName: String?): String =
    "$STANDARD_PREFIX${standardName ?: ""}"

fun buildVisibilityHeaderLine(flags: List<Boolean>): String {
    val encoded = flags.joinToString("#") { if (it) "1" else "0" }
    return "$VISIBILITY_PREFIX$encoded"
}

fun sanitizeStepComment(raw: String): String {
    val sanitized = raw
        .replace('#', ' ')
        .replace("\r", " ")
        .replace("\n", " ")
        .trim()
    return if (sanitized.isBlank()) "no comment" else sanitized
}

fun buildStepsHeaderLine(steps: List<ChartReportStep>): String {
    if (steps.isEmpty()) return STEPS_PREFIX

    val payload = steps.joinToString("#") { step ->
        val duration = step.durationMs.coerceAtLeast(0)
        val comment = sanitizeStepComment(step.comment)
        "$duration;$comment"
    }
    return "$STEPS_PREFIX$payload"
}

fun parseStepsHeaderLine(line: String): List<ChartReportStep> {
    val trimmed = line.trim()
    if (!trimmed.startsWith(STEPS_PREFIX)) return emptyList()

    val payload = trimmed.removePrefix(STEPS_PREFIX)
    if (payload.isBlank()) return emptyList()

    return payload
        .split('#')
        .mapNotNull { token ->
            val part = token.trim()
            if (part.isBlank()) return@mapNotNull null

            val delimiterIdx = part.indexOf(';')
            if (delimiterIdx <= 0) return@mapNotNull null

            val timePart = part.substring(0, delimiterIdx).trim()
            val commentPart = part.substring(delimiterIdx + 1)
            val durationMs = timePart.toIntOrNull() ?: return@mapNotNull null

            ChartReportStep(
                durationMs = durationMs.coerceAtLeast(0),
                comment = sanitizeStepComment(commentPart)
            )
        }
}
