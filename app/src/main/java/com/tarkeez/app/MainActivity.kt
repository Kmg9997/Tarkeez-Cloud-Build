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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.time.LocalDate
import java.time.DayOfWeek
import kotlin.random.Random

private val ink = Color(0xFF15253D)
private val accent = Color(0xFF166B79)
private val paper = Color(0xFFF4F7FA)
private data class Puzzle(val title: String, val question: String, val choices: List<String>, val correct: Int, val explanation: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = getSharedPreferences("tarkeez_progress", MODE_PRIVATE)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                var page by remember { mutableStateOf("home") }
                var points by remember { mutableIntStateOf(store.getInt("points", 0)) }
                var completed by remember { mutableIntStateOf(store.getInt("completed", 0)) }
                var level by remember { mutableIntStateOf(store.getInt("level", 1)) }
                var correctCount by remember { mutableIntStateOf(0) }
                var current by remember { mutableIntStateOf(0) }
                var answered by remember { mutableStateOf<Int?>(null) }
                var puzzles by remember { mutableStateOf(listOf<Puzzle>()) }
                var mode by remember { mutableStateOf("كاملة") }
                val today = LocalDate.now()
                val topics = mapOf(DayOfWeek.SATURDAY to "الذاكرة البصرية", DayOfWeek.SUNDAY to "الانتباه", DayOfWeek.MONDAY to "الذاكرة العاملة", DayOfWeek.TUESDAY to "المنطق", DayOfWeek.WEDNESDAY to "سرعة المعالجة", DayOfWeek.THURSDAY to "التحدي الشامل", DayOfWeek.FRIDAY to "راحة اختيارية")
                fun start(short: Boolean) {
                    mode = if (short) "مصغرة" else "كاملة"
                    puzzles = makePuzzles(level, if (short) 3 else 8)
                    current = 0; correctCount = 0; answered = null; page = "play"
                }
                MaterialTheme(colorScheme = lightColorScheme(primary = accent, background = paper, surface = Color.White)) {
                    Surface(modifier = Modifier.fillMaxSize(), color = paper) {
                        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("تركيز", color = ink, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                            Text("دقائق يومية لعقل أكثر حضوراً", color = accent, fontSize = 14.sp)
                            when (page) {
                                "home" -> {
                                    InfoCard("تحدي اليوم", topics[today.dayOfWeek] ?: "التدريب اليومي", "المستوى $level  •  مجموع النقاط $points")
                                    Text("ابدأ بما يناسب وقتك. الجمعة راحة ولا تُحسب انقطاعاً.", color = ink)
                                    Button(onClick = { start(false) }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("ابدأ جلسة اليوم — 8 ألغاز") }
                                    OutlinedButton(onClick = { start(true) }, modifier = Modifier.fillMaxWidth()) { Text("جلسة إنقاذ — 3 ألغاز") }
                                    OutlinedButton(onClick = { page = "progress" }, modifier = Modifier.fillMaxWidth()) { Text("تقدمي ونتائجي") }
                                    Text("خطة الأسبوع", fontWeight = FontWeight.Bold, color = ink, fontSize = 20.sp)
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                        items(listOf("السبت — الذاكرة البصرية", "الأحد — الانتباه", "الاثنين — الذاكرة العاملة", "الثلاثاء — المنطق", "الأربعاء — سرعة المعالجة", "الخميس — التحدي الشامل", "الجمعة — راحة")) { Text(it, modifier = Modifier.fillMaxWidth(), color = ink) }
                                    }
                                }
                                "play" -> {
                                    val p = puzzles[current]
                                    LinearProgressIndicator(progress = { (current + 1).toFloat() / puzzles.size }, modifier = Modifier.fillMaxWidth())
                                    Text("لغز ${current + 1} من ${puzzles.size} • $mode", color = accent)
                                    InfoCard(p.title, p.question, "اختر الإجابة الصحيحة")
                                    p.choices.forEachIndexed { index, choice ->
                                        OutlinedButton(onClick = {
                                            if (answered == null) { answered = index; if (index == p.correct) correctCount++ }
                                        }, enabled = answered == null, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(choice, textAlign = TextAlign.Center) }
                                    }
                                    if (answered != null) {
                                        Text(if (answered == p.correct) "إجابة صحيحة!" else "الإجابة الصحيحة: ${p.choices[p.correct]}", fontWeight = FontWeight.Bold, color = accent)
                                        Text(p.explanation, color = ink)
                                        Button(onClick = {
                                            if (current < puzzles.lastIndex) { current++; answered = null }
                                            else {
                                                val gained = correctCount * 10 + 5
                                                points += gained; completed++
                                                if (correctCount >= (puzzles.size * 0.8)) level = (level + 1).coerceAtMost(20)
                                                store.edit().putInt("points", points).putInt("completed", completed).putInt("level", level).apply()
                                                page = "result"
                                            }
                                        }, modifier = Modifier.fillMaxWidth()) { Text(if (current == puzzles.lastIndex) "عرض النتيجة" else "اللغز التالي") }
                                    }
                                    TextButton(onClick = { page = "home" }) { Text("إنهاء الجلسة دون حفظ") }
                                }
                                "result" -> {
                                    InfoCard("أحسنت على إكمال الجلسة", "$correctCount من ${puzzles.size} إجابات صحيحة", "الدقة ${(100 * correctCount / puzzles.size)}%")
                                    Text("كل عودة للتدريب خطوة مهمة، حتى لو كانت النتيجة أقل من المتوقع.", color = ink)
                                    Button(onClick = { page = "home" }, modifier = Modifier.fillMaxWidth()) { Text("العودة للرئيسية") }
                                }
                                "progress" -> {
                                    InfoCard("تقدمك", "$completed جلسة مكتملة", "$points نقطة • المستوى $level")
                                    Text("النتائج تقيس أداءك داخل الألغاز فقط، وليست تقييماً طبياً للذاكرة أو الذكاء.", color = ink)
                                    Button(onClick = { page = "home" }) { Text("العودة") }
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
private fun InfoCard(title: String, main: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = accent)
            Text(main, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = ink)
            Text(detail, fontSize = 14.sp, color = ink)
        }
    }
}

private fun makePuzzles(level: Int, count: Int): List<Puzzle> = List(count) { index ->
    val n = level.coerceIn(1, 20) + index
    when (index % 4) {
        0 -> {
            val a = Random.nextInt(2, 12); val step = Random.nextInt(2, 5)
            val answer = a + step * 4
            val opts = listOf(answer, answer + step, answer - step, answer + step * 2).shuffled()
            Puzzle("اكتشف النمط", "$a ، ${a+step} ، ${a+step*2} ، ${a+step*3} ، ؟", opts.map(Int::toString), opts.indexOf(answer), "كل رقم يزيد بمقدار $step")
        }
        1 -> {
            val a = Random.nextInt(3, 12 + n); val b = Random.nextInt(2, 9)
            val answer = a + b
            val opts = listOf(answer, answer + 1, answer - 1, answer + 3).shuffled()
            Puzzle("تركيز وحساب", "ما ناتج $a + $b ؟", opts.map(Int::toString), opts.indexOf(answer), "اجمع العددين بهدوء قبل الاختيار")
        }
        2 -> {
            val words = listOf("قلم", "كتاب", "شجرة", "نافذة", "بحر", "قمر")
            val chosen = words.shuffled().take(3)
            val target = chosen[1]
            val opts = listOf(target, "سيارة", "مفتاح", "مطر").shuffled()
            Puzzle("انتباه للكلمات", "اقرأ: ${chosen.joinToString(" - ")}\nما الكلمة الثانية؟", opts, opts.indexOf(target), "الكلمة الثانية هي $target")
        }
        else -> {
            val a = Random.nextInt(2, 8 + n); val answer = a * 2
            val opts = listOf(answer, answer + 2, answer - 2, answer + 4).shuffled()
            Puzzle("تفكير منطقي", "إذا كان كل صندوق يحتوي على كرتين، فكم كرة في $a صناديق؟", opts.map(Int::toString), opts.indexOf(answer), "نضرب عدد الصناديق في 2")
        }
    }
}
