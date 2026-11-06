package com.tarkeez.app

/**
 * Editorial provenance registry. These are ORIGINAL practice questions, not
 * ETEC questions and not endorsed by any testing authority.
 *
 * A source describing the skill is NOT evidence that an authored question is
 * official or that its distractors have been independently reviewed.
 */
enum class ReviewStatus { DRAFT, NEEDS_REVIEW, VERIFIED }
data class QuestionAudit(
    val status: ReviewStatus,
    val skill: String,
    val referenceTitle: String,
    val referenceUrl: String,
    val reviewer: String = "",
    val reviewedOn: String = ""
)

object ContentQuality {
    const val ETEC = "https://etec.gov.sa/"
    const val DISCLAIMER = "أسئلة تدريبية أصلية غير رسمية؛ ليست صادرة عن هيئة تقويم التعليم والتدريب أو معتمدة منها."
    fun skillFor(category: String): String = when (category) {
        QuestionBank.QUANT -> "استدلال رياضي وحل مسائل"
        QuestionBank.VERBAL -> "استدلال وفهم لغوي"
        QuestionBank.T_MATH -> "مفاهيم الرياضيات"
        QuestionBank.T_PHYSICS -> "مفاهيم الفيزياء"
        QuestionBank.T_CHEMISTRY -> "مفاهيم الكيمياء"
        QuestionBank.T_BIOLOGY -> "مفاهيم الأحياء"
        QuestionBank.TEACHER -> "كفايات تربوية"
        QuestionBank.STEP -> "كفايات اللغة الإنجليزية"
        else -> "مهارات عامة"
    }
    fun audit(question: Question): QuestionAudit = QuestionAudit(
        status = ReviewStatus.NEEDS_REVIEW,
        skill = skillFor(question.category),
        referenceTitle = "هيئة تقويم التعليم والتدريب — بوابة الاختبارات",
        referenceUrl = ETEC
    )
    fun validate(): List<String> {
        val issues = mutableListOf<String>()
        QuestionBank.questions.forEachIndexed { index, q ->
            if (q.prompt.isBlank()) issues += "Question ${index + 1}: empty prompt"
            if (q.choices.size != 4) issues += "Question ${index + 1}: expected four choices"
            if (q.answer !in q.choices.indices) issues += "Question ${index + 1}: invalid answer index"
            if (q.choices.distinct().size != q.choices.size) issues += "Question ${index + 1}: duplicate options"
            if (q.explanation.isBlank()) issues += "Question ${index + 1}: missing explanation"
            if (q.difficulty !in 1..3) issues += "Question ${index + 1}: difficulty outside 1..3"
        }
        return issues
    }
}
