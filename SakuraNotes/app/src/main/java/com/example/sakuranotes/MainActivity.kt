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
    totalNote