package com.example.sakuranotes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SakuraNotesApp() }
    }
}

@Entity(tableName = "notes")
data class SakuraNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val tag: String,
    val date: String,
    val colorArgb: Int,
    val pinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class SakuraTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface SakuraNoteDao {
    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<SakuraNote>>

    @Insert
    suspend fun insertNote(note: SakuraNote)

    @Update
    suspend fun updateNote(note: SakuraNote)

    @Delete
    suspend fun deleteNote(note: SakuraNote)

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countNotes(): Int
}

@Dao
interface SakuraTaskDao {
    @Query("SELECT * FROM tasks ORDER BY completed ASC, updatedAt DESC")
    fun getAllTasks(): Flow<List<SakuraTask>>

    @Insert
    suspend fun insertTask(task: SakuraTask)

    @Update
    suspend fun updateTask(task: SakuraTask)

    @Delete
    suspend fun deleteTask(task: SakuraTask)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int
}

@Database(entities = [SakuraNote::class, SakuraTask::class], version = 2, exportSchema = false)
abstract class SakuraDatabase : RoomDatabase() {
    abstract fun noteDao(): SakuraNoteDao
    abstract fun taskDao(): SakuraTaskDao

    companion object {
        @Volatile private var INSTANCE: SakuraDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        completed INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): SakuraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SakuraDatabase::class.java,
                    "sakura_notes_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

enum class SakuraTab { Notes, Tasks, Me }

private val Sakura = Color(0xFFFF7FA3)
private val SakuraDark = Color(0xFFC2275C)
private val Blue = Color(0xFF7AC7FF)
private val Green = Color(0xFFBEE8BC)
private val Lavender = Color(0xFFE8D8FF)
private val Yellow = Color(0xFFFFE8A3)

data class SakuraUiColors(
    val ink: Color,
    val muted: Color,
    val backgroundTop: Color,
    val backgroundMid: Color,
    val backgroundBottom: Color,
    val surface: Color,
    val glass: Color,
    val card: Color,
    val nav: Color
)

fun colorsFor(darkMode: Boolean): SakuraUiColors {
    return if (darkMode) {
        SakuraUiColors(
            ink = Color(0xFFFCECF2),
            muted = Color(0xFFC9AEBB),
            backgroundTop = Color(0xFF160D16),
            backgroundMid = Color(0xFF241422),
            backgroundBottom = Color(0xFF101427),
            surface = Color(0xFF211620),
            glass = Color.White.copy(alpha = 0.08f),
            card = Color(0xFF2D1C29),
            nav = Color(0xFFFCECF2)
        )
    } else {
        SakuraUiColors(
            ink = Color(0xFF20131F),
            muted = Color(0xFF7A6474),
            backgroundTop = Color(0xFFFFFBFC),
            backgroundMid = Color(0xFFFFEFF4),
            backgroundBottom = Color(0xFFF8F4FF),
            surface = Color(0xFFFFF7F9),
            glass = Color.White.copy(alpha = 0.72f),
            card = Color.White.copy(alpha = 0.72f),
            nav = Color(0xFF20131F)
        )
    }
}

@Composable
fun SakuraNotesApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sakura_settings", Context.MODE_PRIVATE) }
    var darkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
    val ui = colorsFor(darkMode)

    val scope = rememberCoroutineScope()
    val db = remember { SakuraDatabase.getDatabase(context) }
    val noteDao = remember { db.noteDao() }
    val taskDao = remember { db.taskDao() }
    val notes by noteDao.getAllNotes().collectAsState(initial = emptyList())
    val tasks by taskDao.getAllTasks().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(SakuraTab.Notes) }
    var search by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") }
    var showNoteEditor by remember { mutableStateOf(false) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<SakuraNote?>(null) }
    var detailNote by remember { mutableStateOf<SakuraNote?>(null) }

    LaunchedEffect(Unit) {
        if (noteDao.countNotes() == 0) {
            noteDao.insertNote(SakuraNote(title = "App Sakura Notes", content = "Bản này đã có tab Notes, Tasks, Me, Room Database, animation mềm hơn và setting Dark Mode.", tag = "Design", date = "Hôm nay", colorArgb = 0xFFFFD7E2.toInt(), pinned = true))
            noteDao.insertNote(SakuraNote(title = "Todo hôm nay", content = "Test tạo note, sửa note, xóa note, tìm kiếm, lọc tag và chuyển tab.", tag = "Todo", date = "Hôm nay", colorArgb = 0xFFFFE8A3.toInt()))
            noteDao.insertNote(SakuraNote(title = "Private note", content = "Sau này có thể thêm khóa vân tay và mã hóa dữ liệu local.", tag = "Private", date = "Hôm qua", colorArgb = 0xFFE8D8FF.toInt()))
        }
        if (taskDao.countTasks() == 0) {
            taskDao.insertTask(SakuraTask(title = "Hoàn thiện UI modern"))
            taskDao.insertTask(SakuraTask(title = "Thêm Room Database", completed = true))
            taskDao.insertTask(SakuraTask(title = "Build APK mới và test trên máy thật"))
        }
    }

    val filteredNotes = notes.filter { note ->
        val matchSearch = note.title.contains(search, true) || note.content.contains(search, true) || note.tag.contains(search, true)
        val matchTag = activeFilter == "All" || note.tag == activeFilter
        matchSearch && matchTag
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ui.backgroundTop, ui.backgroundMid, ui.backgroundBottom)))
        ) {
            ModernSakuraBackground(ui = ui, darkMode = darkMode)

            Scaffold(
                containerColor = Color.Transparent,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            when (selectedTab) {
                                SakuraTab.Notes -> {
                                    editingNote = null
                                    showNoteEditor = true
                                }
                                SakuraTab.Tasks -> showTaskEditor = true
                                SakuraTab.Me -> selectedTab = SakuraTab.Notes
                            }
                        },
                        containerColor = if (darkMode) SakuraDark else ui.ink,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.padding(bottom = 82.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (selectedTab == SakuraTab.Tasks) "Task" else "New", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                bottomBar = {
                    ModernBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        ui = ui,
                        darkMode = darkMode
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 18.dp)
                        .fillMaxSize()
                ) {
                    Spacer(Modifier.height(18.dp))

                    AppHeader(selectedTab = selectedTab, ui = ui, darkMode = darkMode)

                    Spacer(Modifier.height(18.dp))

                    Crossfade(
                        targetState = selectedTab,
                        animationSpec = tween(durationMillis = 260),
                        label = "tabCrossfade"
                    ) { tab ->
                        when (tab) {
                            SakuraTab.Notes -> NotesTab(
                                notes = filteredNotes,
                                totalNotes = notes.size,
                                search = search,
                                activeFilter = activeFilter,
                                ui = ui,
                                darkMode = darkMode,
                                onSearchChange = { search = it },
                                onFilterChange = { activeFilter = it },
                                onOpenNote = { detailNote = it },
                                onEditNote = {
                                    editingNote = it
                                    showNoteEditor = true
                                },
                                onDeleteNote = { note -> scope.launch { noteDao.deleteNote(note) } }
                            )
                            SakuraTab.Tasks -> TasksTab(
                                tasks = tasks,
                                ui = ui,
                                onToggle = { task ->
                                    scope.launch {
                                        taskDao.updateTask(task.copy(completed = !task.completed, updatedAt = System.currentTimeMillis()))
                                    }
                                },
                                onDelete = { task -> scope.launch { taskDao.deleteTask(task) } }
                            )
                            SakuraTab.Me -> MeTab(
                                notesCount = notes.size,
                                tasksCount = tasks.size,
                                darkMode = darkMode,
                                ui = ui,
                                onDarkModeChange = {
                                    darkMode = it
                                    prefs.edit().putBoolean("dark_mode", it).apply()
                                }
                            )
                        }
                    }
                }
            }

            detailNote?.let { note ->
                NoteDetailSheet(
                    note = note,
                    ui = ui,
                    onClose = { detailNote = null },
                    onEdit = {
                        detailNote = null
                        editingNote = note
                        showNoteEditor = true
                    },
                    onDelete = {
                        scope.launch { noteDao.deleteNote(note) }
                        detailNote = null
                    }
                )
            }

            AnimatedVisibility(showNoteEditor) {
                ModernEditorSheet(
                    note = editingNote,
                    ui = ui,
                    onClose = { showNoteEditor = false },
                    onSave = { title, content, tag ->
                        scope.launch {
                            if (editingNote == null) {
                                noteDao.insertNote(
                                    SakuraNote(
                                        title = title.ifBlank { "Untitled" },
                                        content = content.ifBlank { "No content yet" },
                                        tag = tag.ifBlank { "Idea" },
                                        date = "Vừa xong",
                                        colorArgb = randomNoteColor().value.toInt(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                noteDao.updateNote(
                                    editingNote!!.copy(
                                        title = title.ifBlank { "Untitled" },
                                        content = content.ifBlank { "No content yet" },
                                        tag = tag.ifBlank { "Idea" },
                                        date = "Vừa xong",
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                            }
                            showNoteEditor = false
                        }
                    }
                )
            }

            AnimatedVisibility(showTaskEditor) {
                TaskEditorSheet(
                    ui = ui,
                    onClose = { showTaskEditor = false },
                    onSave = { title ->
                        scope.launch {
                            taskDao.insertTask(SakuraTask(title = title.ifBlank { "New task" }))
                            showTaskEditor = false
                        }
                    }
                )
            }
        }
    }
}

fun randomNoteColor(): Color = listOf(Color(0xFFFFD7E2), Yellow, Lavender, Green, Blue).random()

@Composable
fun AppHeader(selectedTab: SakuraTab, ui: SakuraUiColors, darkMode: Boolean) {
    val title = when (selectedTab) {
        SakuraTab.Notes -> "Notes"
        SakuraTab.Tasks -> "Tasks"
        SakuraTab.Me -> "Me"
    }
    val subtitle = when (selectedTab) {
        SakuraTab.Notes -> "Capture your calm ideas"
        SakuraTab.Tasks -> "Checklist for today"
        SakuraTab.Me -> "Settings & profile"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Sakura", fontSize = 34.sp, fontWeight = FontWeight.Black, color = ui.ink)
            Text(title, fontSize = 34.sp, fontWeight = FontWeight.Black, color = if (darkMode) Sakura else SakuraDark)
            Text(subtitle, fontSize = 13.sp, color = ui.muted, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(ui.glass),
            contentAlignment = Alignment.Center
        ) {
            Text(if (selectedTab == SakuraTab.Me) "⚙️" else "🌸", fontSize = 28.sp)
        }
    }
}

@Composable
fun NotesTab(
    notes: List<SakuraNote>,
    totalNotes: Int,
    search: String,
    activeFilter: String,
    ui: SakuraUiColors,
    darkMode: Boolean,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onOpenNote: (SakuraNote) -> Unit,
    onEditNote: (SakuraNote) -> Unit,
    onDeleteNote: (SakuraNote) -> Unit
) {
    val tags = listOf("All", "Design", "Todo", "Idea", "Private", "Mood")
    Column {
        HeroGlassCard(totalNotes = totalNotes, ui = ui, darkMode = darkMode)
        Spacer(Modifier.height(16.dp))
        ModernSearch(value = search, onChange = onSearchChange, ui = ui)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.take(4).forEach { tag ->
                FilterPill(text = tag, selected = activeFilter == tag, ui = ui, onClick = { onFilterChange(tag) })
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recent notes", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ui.ink)
            Spacer(Modifier.weight(1f))
            Text("${notes.size} notes", color = ui.muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(12.dp))
        if (notes.isEmpty()) {
            EmptyState(ui = ui, message = "Không có ghi chú", hint = "Tạo note mới bằng nút New bên dưới.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes, key = { it.id }) { note ->
                    ModernNoteCard(
                        note = note,
                        ui = ui,
                        onOpen = { onOpenNote(note) },
                        onEdit = { onEditNote(note) },
                        onDelete = { onDeleteNote(note) }
                    )
                }
            }
        }
    }
}

@Composable
fun TasksTab(
    tasks: List<SakuraTask>,
    ui: SakuraUiColors,
    onToggle: (SakuraTask) -> Unit,
    onDelete: (SakuraTask) -> Unit
) {
    val done = tasks.count { it.completed }
    Column {
        TaskSummaryCard(total = tasks.size, done = done, ui = ui)
        Spacer(Modifier.height(18.dp))
        Text("Checklist", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ui.ink)
        Spacer(Modifier.height(12.dp))
        if (tasks.isEmpty()) {
            EmptyState(ui = ui, message = "Chưa có task", hint = "Bấm Task bên dưới để thêm việc cần làm.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                items(tasks, key = { it.id }) { task ->
                    TaskItem(task = task, ui = ui, onToggle = { onToggle(task) }, onDelete = { onDelete(task) })
                }
            }
        }
    }
}

@Composable
fun MeTab(
    notesCount: Int,
    tasksCount: Int,
    darkMode: Boolean,
    ui: SakuraUiColors,
    onDarkModeChange: (Boolean) -> Unit
) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = ui.glass),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Sakura, SakuraDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌸", fontSize = 30.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Sakura Workspace", fontSize = 21.sp, fontWeight = FontWeight.Black, color = ui.ink)
                        Text("$notesCount notes • $tasksCount tasks", color = ui.muted, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        SettingsRow(
            title = "Dark mode",
            subtitle = "Đổi giao diện sáng/tối",
            ui = ui,
            trailing = {
                Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
            }
        )

        SettingsRow(
            title = "120Hz friendly motion",
            subtitle = "Animation ngắn, nhẹ, ưu tiên cảm giác mượt",
            ui = ui,
            trailing = { Text("On", color = SakuraDark, fontWeight = FontWeight.Black) }
        )

        SettingsRow(
            title = "Local database",
            subtitle = "Notes và tasks đang lưu bằng Room",
            ui = ui,
            trailing = { Icon(Icons.Filled.Check, contentDescription = null, tint = SakuraDark) }
        )
    }
}

@Composable
fun SettingsRow(title: String, subtitle: String, ui: SakuraUiColors, trailing: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = ui.glass),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = ui.ink, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = ui.muted, fontSize = 13.sp)
            }
            trailing()
        }
    }
}

@Composable
fun EmptyState(ui: SakuraUiColors, message: String, hint: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌸", fontSize = 44.sp)
            Spacer(Modifier.height(10.dp))
            Text(message, fontSize = 20.sp, fontWeight = FontWeight.Black, color = ui.ink)
            Text(hint, fontSize = 14.sp, color = ui.muted)
        }
    }
}

@Composable
fun ModernSakuraBackground(ui: SakuraUiColors, darkMode: Boolean) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Sakura.copy(alpha = if (darkMode) 0.12f else 0.18f), 210.dp.toPx(), Offset(size.width * 0.9f, size.height * 0.08f))
        drawCircle(Blue.copy(alpha = if (darkMode) 0.10f else 0.14f), 160.dp.toPx(), Offset(size.width * 0.05f, size.height * 0.22f))
        drawCircle(Lavender.copy(alpha = if (darkMode) 0.12f else 0.45f), 230.dp.toPx(), Offset(size.width * 0.85f, size.height * 0.86f))
        val branch = Path().apply {
            moveTo(size.width * 0.54f, 0f)
            cubicTo(size.width * 0.67f, size.height * 0.06f, size.width * 0.82f, size.height * 0.10f, size.width, size.height * 0.15f)
        }
        drawPath(branch, ui.ink.copy(alpha = 0.08f), style = Stroke(width = 7.dp.toPx()))
        repeat(18) { i ->
            val x = size.width * (0.58f + (i % 6) * 0.075f)
            val y = size.height * (0.025f + (i / 6) * 0.05f + (i % 3) * 0.018f)
            drawCircle(Sakura.copy(alpha = 0.22f), 7.dp.toPx(), Offset(x, y))
            drawCircle(Color.White.copy(alpha = 0.35f), 3.dp.toPx(), Offset(x - 2.dp.toPx(), y - 1.dp.toPx()))
        }
    }
}

@Composable
fun HeroGlassCard(totalNotes: Int, ui: SakuraUiColors, darkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ui.glass),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(ui.glass, Sakura.copy(alpha = if (darkMode) 0.12f else 0.18f))))
                .padding(20.dp)
        ) {
            Column {
                Text("Capture your calm ideas", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ui.ink)
                Spacer(Modifier.height(6.dp))
                Text("Open a note with a softer detail view, then edit only when needed.", color = ui.muted, fontSize = 14.sp)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("$totalNotes", "notes", ui)
                    StatPill("Room", "saved", ui)
                    StatPill("120Hz", "ready", ui)
                }
            }
        }
    }
}

@Composable
fun TaskSummaryCard(total: Int, done: Int, ui: SakuraUiColors) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = ui.glass),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Today checklist", fontSize = 20.sp, fontWeight = FontWeight.Black, color = ui.ink)
            Spacer(Modifier.height(6.dp))
            Text("$done / $total completed", color = ui.muted, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill("$total", "tasks", ui)
                StatPill("$done", "done", ui)
                StatPill("${total - done}", "left", ui)
            }
        }
    }
}

@Composable
fun StatPill(number: String, label: String, ui: SakuraUiColors) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = if (ui.ink == Color(0xFFFCECF2)) 0.10f else 0.68f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(number, fontWeight = FontWeight.Black, color = SakuraDark)
        Spacer(Modifier.width(5.dp))
        Text(label, color = ui.muted, fontSize = 12.sp)
    }
}

@Composable
fun ModernSearch(value: String, onChange: (String) -> Unit, ui: SakuraUiColors) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(26.dp),
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SakuraDark) },
        placeholder = { Text("Search your notes...") }
    )
}

@Composable
fun FilterPill(text: String, selected: Boolean, ui: SakuraUiColors, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, animationSpec = tween(160), label = "pillScale")
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) ui.ink else ui.glass)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) ui.backgroundTop else ui.muted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun ModernNoteCard(note: SakuraNote, ui: SakuraUiColors, onOpen: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val scale by animateFloatAsState(1f, animationSpec = tween(120), label = "noteScale")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (note.pinned) 188.dp else 170.dp)
            .scale(scale)
            .animateContentSize(animationSpec = tween(220))
            .clickable { onOpen() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(note.colorArgb)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color.White.copy(alpha = 0.55f)).padding(horizontal = 9.dp, vertical = 5.dp)) {
                    Text(note.tag, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF20131F).copy(alpha = 0.75f))
                }
                Spacer(Modifier.weight(1f))
                if (note.pinned) Icon(Icons.Filled.Star, contentDescription = null, tint = SakuraDark, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(note.title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF20131F), maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(note.content, fontSize = 13.sp, color = Color(0xFF20131F).copy(alpha = 0.62f), maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(note.date, color = Color(0xFF20131F).copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Color(0xFF20131F).copy(alpha = 0.62f), modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFF20131F).copy(alpha = 0.62f), modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: SakuraTask, ui: SakuraUiColors, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(animationSpec = tween(180)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ui.glass),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.completed, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Text(
                task.title,
                modifier = Modifier.weight(1f),
                color = if (task.completed) ui.muted else ui.ink,
                fontWeight = FontWeight.Bold,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = ui.muted)
            }
        }
    }
}

@Composable
fun ModernBottomBar(selectedTab: SakuraTab, onTabSelected: (SakuraTab) -> Unit, ui: SakuraUiColors, darkMode: Boolean) {
    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.clip(RoundedCornerShape(30.dp)).background(if (darkMode) Color.White.copy(alpha = 0.10f) else ui.nav.copy(alpha = 0.92f)).padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem("Notes", SakuraTab.Notes, selectedTab, onTabSelected, ui)
            BottomItem("Tasks", SakuraTab.Tasks, selectedTab, onTabSelected, ui)
            BottomItem("Me", SakuraTab.Me, selectedTab, onTabSelected, ui)
        }
    }
}

@Composable
fun BottomItem(label: String, tab: SakuraTab, selectedTab: SakuraTab, onTabSelected: (SakuraTab) -> Unit, ui: SakuraUiColors) {
    val active = tab == selectedTab
    val scale by animateFloatAsState(if (active) 1.04f else 1f, animationSpec = tween(150), label = "bottomScale")
    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) Color.White.copy(alpha = 0.16f) else Color.Transparent)
            .clickable { onTabSelected(tab) }
            .padding(horizontal = if (active) 14.dp else 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (active) {
            Icon(if (tab == SakuraTab.Me) Icons.Filled.Settings else Icons.Filled.Favorite, contentDescription = null, tint = Sakura, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = Color.White.copy(alpha = if (active) 1f else 0.62f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun NoteDetailSheet(note: SakuraNote, ui: SakuraUiColors, onClose: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.34f)), contentAlignment = Alignment.BottomCenter) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp), color = ui.surface) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color(note.colorArgb)).padding(horizontal = 12.dp, vertical = 7.dp)) {
                        Text(note.tag, color = Color(0xFF20131F), fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = null, tint = ui.ink) }
                }
                Spacer(Modifier.height(10.dp))
                Text(note.title, fontSize = 28.sp, fontWeight = FontWeight.Black, color = ui.ink)
                Spacer(Modifier.height(8.dp))
                Text(note.date, fontSize = 13.sp, color = ui.muted, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(16.dp))
                Divider(color = ui.muted.copy(alpha = 0.18f))
                Spacer(Modifier.height(16.dp))
                Text(note.content, fontSize = 16.sp, color = ui.ink.copy(alpha = 0.86f), lineHeight = 24.sp)
                Spacer(Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) { Text("Delete", color = SakuraDark, fontWeight = FontWeight.Bold) }
                    Button(onClick = onEdit, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ui.ink, contentColor = ui.backgroundTop), shape = RoundedCornerShape(20.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernEditorSheet(note: SakuraNote?, ui: SakuraUiColors, onClose: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var tag by remember { mutableStateOf(note?.tag ?: "Idea") }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)), contentAlignment = Alignment.BottomCenter) {
        Surface(modifier = Modifier.fillMaxWidth().imePadding(), shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp), color = ui.surface) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (note == null) "New note" else "Edit note", fontSize = 26.sp, fontWeight = FontWeight.Black, color = ui.ink, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = null, tint = ui.ink) }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(22.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Write something soft...") }, modifier = Modifier.fillMaxWidth().height(160.dp), shape = RoundedCornerShape(22.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = tag, onValueChange = { tag = it }, label = { Text("Tag") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(22.dp))
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Cancel", color = ui.muted, fontWeight = FontWeight.Bold) }
                    Button(onClick = { onSave(title, content, tag) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ui.ink, contentColor = ui.backgroundTop), shape = RoundedCornerShape(22.dp)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskEditorSheet(ui: SakuraUiColors, onClose: () -> Unit, onSave: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)), contentAlignment = Alignment.BottomCenter) {
        Surface(modifier = Modifier.fillMaxWidth().imePadding(), shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp), color = ui.surface) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("New task", fontSize = 26.sp, fontWeight = FontWeight.Black, color = ui.ink, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = null, tint = ui.ink) }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task title") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(22.dp))
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Cancel", color = ui.muted, fontWeight = FontWeight.Bold) }
                    Button(onClick = { onSave(title) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = ui.ink, contentColor = ui.backgroundTop), shape = RoundedCornerShape(22.dp)) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
