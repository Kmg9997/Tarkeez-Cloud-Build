package com.tarkeez.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DiagnosticResult(val scores: Map<String, Int>, val total: Int) {
    val weakest: String get() = scores.minByOrNull { it.value }?.key ?: QuestionBank.QUANT
    val strongest: String get() = scores.maxByOrNull { it.value }?.key ?: QuestionBank.QUANT
    fun plan(): List<String> = listOf(
        "اليوم 1–2: تدرب على ${weakest}، ثم راجع شرح الإجابات الخاطئة.",
        "اليوم 3–4: أكمل تدريباً في ${QuestionBank.VERBAL} وتدريباً في ${QuestionBank.QUANT}.",
        "اليوم 5: حل جلسة قصيرة في ${strongest} لتثبيت نقاط القوة.",
        "اليوم 6: أعد اختبار تحديد المستوى وقارن النتائج."
    )
}

@Composable
fun DiagnosticScreen(onDone: (DiagnosticResult) -> Unit, onBack: () -> Unit) {
    val categories = QuestionBank.categories
    val questions = remember { categories.flatMap { c -> QuestionBank.questions.filter { it.category == c }.shuffled().take(2) } }
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    val scores = remember { mutableStateMapOf<String, Int>() }
    val q = questions[index]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("اختبار تحديد المستوى", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF172C43))
        Text("اختبار مبدئي من 20 سؤالاً يغطي الأقسام العشرة. نتيجته إرشادية وليست درجة اختبار رسمية.", fontSize = 14.sp)
        LinearProgressIndicator(progress = { (index + 1f) / questions.size }, modifier = Modifier.fillMaxWidth())
        Text("السؤال ${index + 1} من ${questions.size} • ${q.category}", color = Color(0xFF176B77))
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Text(q.prompt, modifier = Modifier.padding(18.dp), fontSize = 19.sp, lineHeight = 28.sp)
        }
        q.choices.forEachIndexed { i, value ->
            OutlinedButton(onClick = { selected = i }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(if (selected == i) "● $value" else value)
            }
        }
        Button(onClick = {
            if (selected == q.answer) scores[q.category] = (scores[q.category] ?: 0) + 1
            if (index == questions.lastIndex) {
                onDone(DiagnosticResult(categories.associateWith { scores[it] ?: 0 }, questions.size))
            } else {
                index++
                selected = -1
            }
        }, enabled = selected >= 0, modifier = Modifier.fillMaxWidth()) {
            Text(if (index == questions.lastIndex) "عرض التقرير والخطة" else "السؤال التالي")
        }
        TextButton(onClick = onBack) { Text("العودة") }
    }
}

@Composable
fun DiagnosticReport(result: DiagnosticResult, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("تقرير مستواك الشخصي", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF172C43))
        val totalCorrect = result.scores.values.sum()
        Text("الإجابات الصحيحة: $totalCorrect من ${result.total}", fontSize = 20.sp, color = Color(0xFF176B77))
        Text("تقييم مبدئي: سؤالان فقط لكل مجال؛ لا تعتمد عليه وحده لتحديد مستوى دقيق.", fontSize = 13.sp)
        result.scores.forEach { (category, score) ->
            Text("$category: $score / 2", fontSize = 15.sp)
            LinearProgressIndicator(progress = { score / 2f }, modifier = Modifier.fillMaxWidth())
        }
        HorizontalDivider()
        Text("خطة تدريب شخصية مقترحة — 6 أيام", fontSize = 19.sp, fontWeight = FontWeight.Bold)
        result.plan().forEach { Text("• $it", fontSize = 15.sp, lineHeight = 23.sp) }
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("إعادة التقييم") }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("العودة للرئيسية") }
    }
}
