package com.mashqyar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

data class Task(val id: Long, val title: String, val subject: String, val due: String, val done: Boolean = false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MashqYarApp() }
    }
}

@Composable
fun MashqYarApp() {
    val nav = rememberNavController()
    var tasks by remember { mutableStateOf(listOf(
        Task(1, "حل تمرین صفحه ۴۲", "ریاضی", "امروز"),
        Task(2, "تمرین Unit 3", "انگلیسی", "فردا"),
        Task(3, "مطالعه فصل دوم", "علوم", "پس‌فردا")
    )) }
    MaterialTheme {
        NavHost(nav, startDestination = "home") {
            composable("home") { Home(tasks, nav) }
            composable("tasks") { Tasks(tasks, nav) { id -> tasks = tasks.map { if (it.id == id) it.copy(done = !it.done) else it } } }
            composable("add") { Add(nav) { tasks = tasks + it; nav.popBackStack() } }
            composable("calendar") { SimplePage("تقویم", nav, "تکالیف را بر اساس روز مشاهده کنید.") }
            composable("stats") { Stats(tasks, nav) }
            composable("settings") { SimplePage("تنظیمات", nav, "یادآوری، ظاهر و مدیریت اطلاعات") }
        }
    }
}

@Composable
fun Shell(title: String, nav: NavHostController, body: @Composable ColumnScope.() -> Unit) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) }) },
        bottomBar = {
            NavigationBar {
                listOf("home" to "خانه", "tasks" to "تکالیف", "calendar" to "تقویم", "stats" to "آمار", "settings" to "تنظیمات").forEach { (route, label) ->
                    NavigationBarItem(
                        selected = false,
                        onClick = { nav.navigate(route) { launchSingleTop = true } },
                        icon = { Icon(when (route) { "home" -> Icons.Default.Home; "tasks" -> Icons.Default.Edit; "calendar" -> Icons.Default.DateRange; "stats" -> Icons.Default.BarChart; else -> Icons.Default.Settings }, null) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { p -> Column(Modifier.fillMaxSize().padding(p).padding(16.dp), horizontalAlignment = Alignment.End, content = body) }
}

@Composable
fun Home(tasks: List<Task>, nav: NavHostController) = Shell("مشق‌یار", nav) {
    Text("سلام 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Text("مدیریت ساده و آفلاین تکالیف شما")
    Spacer(Modifier.height(18.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(Modifier.weight(1f)) { Column(Modifier.padding(18.dp)) { Text("${tasks.count { !it.done }}", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("باقی‌مانده") } }
        Card(Modifier.weight(1f)) { Column(Modifier.padding(18.dp)) { Text("${tasks.count { it.done }}", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("انجام‌شده") } }
    }
    Spacer(Modifier.height(18.dp))
    Button({ nav.navigate("add") }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("＋ افزودن تکلیف") }
    Spacer(Modifier.height(18.dp))
    Text("تکالیف نزدیک", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    tasks.filter { !it.done }.forEach { TaskCard(it) }
}

@Composable
fun TaskCard(t: Task, onDone: (() -> Unit)? = null) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onDone != null) Checkbox(t.done, { onDone() }) else Spacer(Modifier.width(4.dp))
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) { Text(t.title, fontWeight = FontWeight.Bold); Text("${t.subject} • ${t.due}") }
        }
    }
}

@Composable
fun Tasks(tasks: List<Task>, nav: NavHostController, toggle: (Long) -> Unit) = Shell("تکالیف", nav) {
    Spacer(Modifier.height(10.dp))
    LazyColumn { items(tasks) { TaskCard(it) { toggle(it.id) } } }
}

@Composable
fun Add(nav: NavHostController, save: (Task) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("امروز") }
    Scaffold(topBar = { TopAppBar(title = { Text("افزودن تکلیف") }, navigationIcon = { IconButton({ nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.End) {
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("عنوان تکلیف") })
            OutlinedTextField(subject, { subject = it }, Modifier.fillMaxWidth(), label = { Text("درس") })
            OutlinedTextField(due, { due = it }, Modifier.fillMaxWidth(), label = { Text("موعد") })
            Button({ save(Task(System.currentTimeMillis(), title, subject.ifBlank { "سایر" }, due)) }, enabled = title.isNotBlank(), Modifier.fillMaxWidth()) { Text("ذخیره تکلیف") }
        }
    }
}

@Composable fun SimplePage(title: String, nav: NavHostController, msg: String) = Shell(title, nav) { Spacer(Modifier.height(30.dp)); Text(msg, fontSize = 18.sp) }

@Composable fun Stats(tasks: List<Task>, nav: NavHostController) = Shell("آمار", nav) {
    val p = if (tasks.isEmpty()) 0 else tasks.count { it.done } * 100 / tasks.size
    Spacer(Modifier.height(30.dp)); Text("$p٪", fontSize = 50.sp, fontWeight = FontWeight.Bold); Text("درصد تکالیف انجام‌شده")
}
