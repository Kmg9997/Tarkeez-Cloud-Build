package com.tarkeez.app

/** Pilot learning module. Editorial content is original; external reference is
 * a reading destination, NOT a claim that this question was published there.
 * No question is marked verified until an independent reviewer checks it.
 */
data class LearningLesson(
    val title: String,
    val concept: String,
    val steps: List<String>,
    val workedExample: String,
    val whyOthersWrong: String,
    val sourceName: String,
    val sourceUrl: String,
    val sourceNote: String
)

object LearningLibrary {
    private val formative = LearningLesson(
        title = "التقويم التكويني: كيف تستخدم أدلة التعلم لتحسين التدريس؟",
        concept = "التقويم التكويني ليس اختباراً نهائياً؛ هو جمع أدلة عن فهم المتعلمين أثناء التدريس، ثم تفسيرها واستخدامها لتعديل الشرح والنشاط والتغذية الراجعة. مجرد جمع الدرجات دون اتخاذ إجراء تعليمي لا يحقق غرضه.",
        steps = listOf(
            "حدد هدف تعلم واضحاً ومعيار نجاح يمكن ملاحظته.",
            "اطرح سؤالاً كاشفاً للفهم أو مهمة قصيرة يؤديها جميع الطلاب.",
            "حلل الإجابات وحدد التصورات الخاطئة والفجوات.",
            "عدل التدريس أو قدم تغذية راجعة قابلة للتنفيذ.",
            "أعد التحقق بسؤال جديد لمعرفة أثر التعديل."
        ),
        workedExample = "بعد شرح الكسور، أجاب عدد من الطلاب أن 1/8 أكبر من 1/6 لأن 8 أكبر من 6. يكشف المعلم التصور الخاطئ، ويستخدم نموذج أجزاء متساوية لتوضيح أن زيادة المقام تصغر الجزء عند ثبات البسط، ثم يطرح مسألة مشابهة للتحقق. هنا تحولت أدلة الفهم إلى قرار تدريسي.",
        whyOthersWrong = "الترتيب النهائي وتحديد الدرجة الختامية من وظائف التقويم الختامي غالباً، وليس الغرض الأساسي للتقويم التكويني. كما أن التقويم التكويني لا يلغي الاختبارات ولا يختص بتسجيل الغياب.",
        sourceName = "Education Endowment Foundation (EEF) — Assessment and Feedback",
        sourceUrl = "https://educationendowmentfoundation.org.uk/education-evidence/teaching-learning-toolkit/feedback",
        sourceNote = "مرجع مهني مساند عن التغذية الراجعة والتعلم. هذا الرابط ليس إثباتاً مباشراً لصحة السؤال ولا مرجعاً رسمياً لاختبار الرخصة المهنية؛ تحديد الفقرة المطابقة ومراجعتها مطلوب قبل اعتماد الدرس."
    )

    fun forQuestion(q: Question): LearningLesson? =
        if (q.category == QuestionBank.TEACHER && q.prompt.contains("التقويم التكويني")) formative else null
}
