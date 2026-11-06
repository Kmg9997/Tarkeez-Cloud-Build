package com.tarkeez.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

private val navy = Color(0xFFF5F8FA)
private val teal = Color(0xFF22D4AC)
private val background = Color(0xFF0C111A)
private val panel = Color(0xFF1B2430)
private val tahsili = listOf(QuestionBank.T_MATH, QuestionBank.T_PHYSICS, QuestionBank.T_CHEMISTRY, QuestionBank.T_BIOLOGY)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("tarkeez_progress", MODE_PRIVATE)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                var screen by remember { mutableStateOf("home") }
                var diagnosticResult by remember { mutableStateOf<DiagnosticResult?>(null) }
                var selectedCategory by remember { mutableStateOf(QuestionBank.QUANT) }
                var questions by remember { mutableStateOf(emptyList<Question>()) }
                var index by remember { mutableIntStateOf(0) }
                var choice by remember { mutableIntStateOf(-1) }
                var correct by remember { mutableIntStateOf(0) }
                var points by remember { mutableIntStateOf(prefs.getInt("points", 0)) }
                var completed by remember { mutableIntStateOf(prefs.getInt("completed", 0)) }
                var lastScore by remember { mutableIntStateOf(0) }
                var lastTotal by remember { mutableIntStateOf(0) }
                val today = LocalDate.now().toString()
                var todaySessions by remember { mutableIntStateOf(prefs.getInt("day_$today", 0)) }
                var categorySessions by remember { mutableIntStateOf(0) }
                var mistakes by remember { mutableStateOf(prefs.getStringSet("mistakes", emptySet())?.toSet() ?: emptySet<String>()) }

                fun begin(category: String, short: Boolean = false) {
                    selectedCategory = category
                    val pool = QuestionBank.questions.filter { it.category == category }.shuffled()
                    questions = pool.take(if (short) 3 else 6)
                    if (questions.isNotEmpty()) {
                        index = 0
                        choice = -1
                        correct = 0
                        screen = "quiz"
                    }
                }

                MaterialTheme(colorScheme = darkColorScheme(primary = teal, background = background, surface = Color(0xFF1B2430), onSurface = navy)) {
                    Surface(Modifier.fillMaxSize(), color = background) {
                        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 14.dp)) {
                            Text("مِعيار", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = navy)
                            Text("تعلّم • تدرّب • أتقن", color = Color(0xFFB7C6D3), fontSize = 13.sp)
                            Text("القدرات  •  التحصيلي  •  الكفايات  •  STEP", fontSize = 12.sp, color = teal)
                            Spacer(Modifier.height(14.dp))
                            when (screen) {
                                "diagnostic" -> DiagnosticScreen(onDone = { result -> diagnosticResult = result; screen = "report" }, onBack = { screen = "home" })
                                "report" -> diagnosticResult?.let { result -> DiagnosticReport(result, onRetry = { screen = "diagnostic" }, onBack = { screen = "home" }) }
                                "home" -> {
                                    InfoPanel("رحلتك التعليمية", "كل مهارة لها رتبة، وكل يوم تقدم", "$completed جلسة مكتملة  •  $points نقطة")
                                    Spacer(Modifier.height(10.dp))
                                    Button(onClick = { screen = "diagnostic" }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("اختبار تحديد المستوى الشخصي") }
                                    if (diagnosticResult != null) {
                                        TextButton(onClick = { screen = "report" }) { Text("عرض خطتي الشخصية") }
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text("اختر مسارك", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = navy)
                                    Spacer(Modifier.height(8.dp))
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.weight(1f)) {
                                        items(QuestionBank.categories.filter { it !in tahsili }.take(2) + listOf("التحصيلي") + QuestionBank.categories.filter { it !in tahsili }.drop(2)) { category ->
                                            Card(
                                                onClick = { if (category == "التحصيلي") { screen = "tahsili" } else { selectedCategory = category; screen = "category" } },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(15.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2430))
                                            ) {
                                                Row(Modifier.fillMaxWidth().padding(17.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(category, color = navy, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                                    Text("←", color = teal, fontSize = 21.sp)
                                                }
                                            }
                                        }
                                        item {
                                            OutlinedButton(onClick = { screen = "progress" }, modifier = Modifier.fillMaxWidth()) { Text("نتائجي وتقدمي") }
                                            OutlinedButton(onClick = { screen = "mistakes" }, modifier = Modifier.fillMaxWidth()) { Text("دفتر أخطائي (${mistakes.size})") }
                                        }
                                    }
                                }
                                "tahsili" -> {
                                    Text("التحصيلي", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = navy)
                                    Text("اختر المادة التي تريد التدريب عليها", color = Color(0xFFB7C6D3), fontSize = 14.sp)
                                    Spacer(Modifier.height(14.dp))
                                    tahsili.forEach { subject ->
                                        Card(
                                            onClick = { selectedCategory = subject; screen = "category" },
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                            colors = CardDefaults.cardColors(containerColor = panel),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Text(subject.removePrefix("التحصيلي — "), modifier = Modifier.padding(20.dp), color = navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    TextButton(onClick = { screen = "home" }) { Text("العودة للرئيسية") }
                                }
                                "mistakes" -> {
                                    Text("دفتر أخطائي", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = navy)
                                    Text("تُحفظ الأسئلة التي أخطأت فيها وتُحذف بعد الإجابة الصحيحة عنها.", color = navy, fontSize = 14.sp)
                                    Spacer(Modifier.height(12.dp))
                                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(QuestionBank.questions.filter { it.prompt in mistakes }) { item ->
                                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2430))) {
                                                Column(Modifier.padding(14.dp)) {
                                                    Text(item.category, color = teal)
                                                    Text(item.prompt, color = navy, fontWeight = FontWeight.Bold)
                                                    Text("الإجابة: ${item.choices[item.answer]}", color = teal)
                                                    Text(item.explanation, color = navy)
                                                }
                                            }
                                        }
                                    }
                                    TextButton(onClick = { screen = "home" }) { Text("العودة") }
                                }
                                "notes" -> {
                                    InfoPanel("ملخص المذاكرة", selectedCategory, "راجع القاعدة ثم اختبر فهمك")
                                    Spacer(Modifier.height(14.dp))
                                    Card(colors = CardDefaults.cardColors(containerColor = panel), modifier = Modifier.fillMaxWidth()) {
                                        Text(QuestionBank.studyNotes[selectedCategory] ?: "سيضاف الملخص قريباً.", modifier = Modifier.padding(18.dp), color = navy, fontSize = 17.sp, lineHeight = 28.sp)
                                    }
                                    Spacer(Modifier.height(14.dp))
                                    Button(onClick = { begin(selectedCategory) }, modifier = Modifier.fillMaxWidth()) { Text("تدرب على هذا القسم") }
                                    TextButton(onClick = { screen = "category" }) { Text("العودة") }
                                }
                                "category" -> {
                                    InfoPanel(selectedCategory, "تدريبات أصلية مع شرح كل إجابة", "يعمل دون اتصال بالإنترنت")
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedButton(onClick = { screen = "notes" }, modifier = Modifier.fillMaxWidth()) { Text("ملخص المذاكرة") }
                                    Spacer(Modifier.height(20.dp))
                                    Button(onClick = { begin(selectedCategory) }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("ابدأ التدريب الكامل") }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = { begin(selectedCategory, true) }, modifier = Modifier.fillMaxWidth()) { Text("جلسة سريعة — 3 أسئلة") }
                                    Spacer(Modifier.height(8.dp))
                                    Text("بعد الإجابة سيظهر سبب صحة الاختيار. الأسئلة تدريبية وليست أسئلة رسمية من الاختبارات.", color = navy, fontSize = 14.sp)
                                    TextButton(onClick = { screen = "home" }) { Text("العودة للأقسام") }
                                }
                                "quiz" -> {
                                    val q = questions[index]
                                    Text(selectedCategory, fontWeight = FontWeight.Bold, color = teal, fontSize = 16.sp)
                                    Spacer(Modifier.height(9.dp))
                                    LinearProgressIndicator(progress = { (index + 1f) / questions.size }, modifier = Modifier.fillMaxWidth())
                                    Spacer(Modifier.height(9.dp))
                                    Text("السؤال ${index + 1} من ${questions.size}", color = navy)
                                    Spacer(Modifier.height(10.dp))
                                    Card(colors = CardDefaults.cardColors(containerColor = panel), modifier = Modifier.fillMaxWidth()) {
                                        Text(q.prompt, modifier = Modifier.padding(18.dp), color = navy, fontSize = 19.sp, lineHeight = 29.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    q.choices.forEachIndexed { i, answer ->
                                        OutlinedButton(
                                            onClick = { if (choice == -1) { choice = i; if (i == q.answer) { correct++; mistakes = mistakes - q.prompt } else { mistakes = mistakes + q.prompt }; prefs.edit().putStringSet("mistakes", mistakes).apply() } },
                                            enabled = choice == -1,
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp).heightIn(min = 48.dp)
                                        ) { Text(answer, fontSize = 16.sp, textAlign = TextAlign.Center) }
                                    }
                                    if (choice >= 0) {
                                        Text(if (choice == q.answer) "إجابة صحيحة" else "الإجابة الصحيحة: ${q.choices[q.answer]}", fontWeight = FontWeight.Bold, color = teal)
                                        Spacer(Modifier.height(5.dp))
                                        Text(q.explanation, fontSize = 14.sp, color = navy, lineHeight = 23.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Button(onClick = {
                                            if (index < questions.lastIndex) {
                                                index++
                                                choice = -1
                                            } else {
                                                lastScore = correct
                                                lastTotal = questions.size
                                                points += correct * 10 + 5
                                                completed++
                                                todaySessions++
                                                prefs.edit().putInt("points", points).putInt("completed", completed).putInt("day_$today", todaySessions).apply()
                                                categorySessions++
                                                screen = "result"
                                            }
                                        }, modifier = Modifier.fillMaxWidth()) {
                                            Text(if (index == questions.lastIndex) "عرض النتيجة" else "السؤال التالي")
                                        }
                                    }
                                    TextButton(onClick = { screen = "category" }) { Text("إنهاء التدريب") }
                                }
                                "result" -> {
                                    InfoPanel("اكتمل التدريب!", "$lastScore من $lastTotal إجابات صحيحة", "الدقة: ${if (lastTotal > 0) lastScore * 100 / lastTotal else 0}%  •  مجموع نقاطك $points")
                                    Spacer(Modifier.height(15.dp))
                                    Text("راجع دفتر أخطائك وملخص المهارة، ثم حاول مرة أخرى.", color = navy)
                                    Spacer(Modifier.height(12.dp))
                                    Button(onClick = { begin(selectedCategory) }, modifier = Modifier.fillMaxWidth()) { Text("تدريب جديد") }
                                    OutlinedButton(onClick = { screen = "mistakes" }, modifier = Modifier.fillMaxWidth()) { Text("مراجعة أخطائي") }
                                    OutlinedButton(onClick = { screen = "home" }, modifier = Modifier.fillMaxWidth()) { Text("العودة للأقسام") }
                                }
                                "progress" -> {
                                    InfoPanel("تقدمك", "$completed جلسة مكتملة", "$points نقطة مكتسبة")
                                    Spacer(Modifier.height(12.dp))
                                    Text("جلسات اليوم: $todaySessions", color = navy, fontSize = 17.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("النتائج تعكس الأداء داخل التطبيق، وليست تشخيصاً للذاكرة أو مقياساً رسمياً للقدرات.", color = navy)
                                    TextButton(onClick = { screen = "home" }) { Text("العودة") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, main: String, detail: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = panel),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = teal)
            Text(main, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = navy)
            Text(detail, fontSize = 13.sp, color = Color(0xFFB7C6D3))
        }
    }
}
