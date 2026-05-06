package com.example.sakuranotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SakuraNotesApp() }
    }
}

data class SakuraNote(
    val id: Int,
    val title: String,
    val content: String,
    val tag: String,
    val date: String,
    val color: Color,
    val pinned: Boolean = false
)

private val Ink = Color(0xFF20131F)
private val Muted = Color(0xFF7A6474)
private val Sakura = Color(0xFFFF7FA3)
private val SakuraDark = Color(0xFFC2275C)
private val Cream = Color(0xFFFFF7F9)
private val Blue = Color(0xFF7AC7FF)
private val Green = Color(0xFFBEE8BC)
private val Lavender = Color(0xFFE8D8FF)
private val Yellow = Color(0xFFFFE8A3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SakuraNotesApp() {
    val notes = remember {
        mutableStateListOf(
            SakuraNote(1, "App Sakura Notes", "Thiết kế lại app theo style modern: clean, glass, pastel, card grid, bottom bar.", "Design", "Hôm nay", Color(0xFFFFD7E2), true),
            SakuraNote(2, "Todo hôm nay", "Build APK, cài thử trên Android, chỉnh UI cho giống app 2026 hơn.", "Todo", "Hôm nay", Yellow),
            SakuraNote(3, "Private note", "Sau này thêm khóa vân tay và mã hóa dữ liệu local.", "Private", "Hôm qua", Lavender),
            SakuraNote(4, "Ý tưởng tính năng", "Room Database, Reminder, Search, Widget, Cloud Sync.", "Idea", "2 ngày trước", Green),
            SakuraNote(5, "Moodboard", "Rosewater, Sakura Pink, cream background, glass panel, soft shadow.", "Mood", "Tuần này", Blue)
        )
    }

    var search by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") }
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<SakuraNote?>(null) }

    val tags = listOf("All", "Design", "Todo", "Idea", "Private", "Mood")
    val filtered = notes.filter { note ->
        val matchSearch = note.title.contains(search, true) || note.content.contains(search, true) || note.tag.contains(search, true)
        val matchTag = activeFilter == "All" || note.tag == activeFilter
        matchSearch && matchTag
    }.sortedByDescending { it.pinned }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFBFC), Color(0xFFFFEFF4), Color(0xFFF8F4FF))
                    )
                )
        ) {
            ModernSakuraBackground()

            Scaffold(
                containerColor = Color.Transparent,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            editingNote = null
                            showEditor = true
                        },
                        containerColor = Ink,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.padding(bottom = 82.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("New", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                bottomBar = { ModernBottomBar() }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 18.dp)
                        .fillMaxSize()
                ) {
                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sakura", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Ink)
                            Text("Notes", fontSize = 36.sp, fontWeight = FontWeight.Black, color = SakuraDark)
                        }
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.72f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌸", fontSize = 28.sp)
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    HeroGlassCard(totalNotes = notes.size)

                    Spacer(Modifier.height(16.dp))

                    ModernSearch(value = search, onChange = { search = it })

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.take(4).forEach { tag ->
                            FilterPill(
                                text = tag,
                                selected = activeFilter == tag,
                                onClick = { activeFilter = tag }
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent notes", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
                        Spacer(Modifier.weight(1f))
                        Text("${filtered.size} notes", color = Muted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Spacer(Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.id }) { note ->
                            ModernNoteCard(
                                note = note,
                                onEdit = {
                                    editingNote = note
                                    showEditor = true
                                },
                                onDelete = { notes.remove(note) }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(showEditor) {
                ModernEditorSheet(
                    note = editingNote,
                    onClose = { showEditor = false },
                    onSave = { title, content, tag ->
                        if (editingNote == null) {
                            notes.add(
                                0,
                                SakuraNote(
                                    id = (notes.maxOfOrNull { it.id } ?: 0) + 1,
                                    title = title.ifBlank { "Untitled" },
                                    content = content.ifBlank { "No content yet" },
                                    tag = tag.ifBlank { "Idea" },
                                    date = "Vừa xong",
                                    color = listOf(Color(0xFFFFD7E2), Yellow, Lavender, Green, Blue).random()
                                )
                            )
                        } else {
                            val index = notes.indexOfFirst { it.id == editingNote!!.id }
                            if (index >= 0) {
                                notes[index] = editingNote!!.copy(
                                    title = title.ifBlank { "Untitled" },
                                    content = content.ifBlank { "No content yet" },
                                    tag = tag.ifBlank { "Idea" }
                                )
                            }
                        }
                        showEditor = false
                    }
                )
            }
        }
    }
}

@Composable
fun ModernSakuraBackground() {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Sakura.copy(alpha = 0.18f), 210.dp.toPx(), Offset(size.width * 0.9f, size.height * 0.08f))
        drawCircle(Blue.copy(alpha = 0.14f), 160.dp.toPx(), Offset(size.width * 0.05f, size.height * 0.22f))
        drawCircle(Lavender.copy(alpha = 0.45f), 230.dp.toPx(), Offset(size.width * 0.85f, size.height * 0.86f))

        val branch = Path().apply {
            moveTo(size.width * 0.54f, 0f)
            cubicTo(size.width * 0.67f, size.height * 0.06f, size.width * 0.82f, size.height * 0.10f, size.width, size.height * 0.15f)
        }
        drawPath(branch, Ink.copy(alpha = 0.08f), style = Stroke(width = 7.dp.toPx()))

        repeat(18) { i ->
            val x = size.width * (0.58f + (i % 6) * 0.075f)
            val y = size.height * (0.025f + (i / 6) * 0.05f + (i % 3) * 0.018f)
            drawCircle(Sakura.copy(alpha = 0.22f), 7.dp.toPx(), Offset(x, y))
            drawCircle(Color.White.copy(alpha = 0.45f), 3.dp.toPx(), Offset(x - 2.dp.toPx(), y - 1.dp.toPx()))
        }
    }
}

@Composable
fun HeroGlassCard(totalNotes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.72f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.8f), Color(0xFFFFDDE7).copy(alpha = 0.78f))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text("Capture your calm ideas", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Ink)
                Spacer(Modifier.height(6.dp))
                Text("A soft workspace for quick notes, tasks and private thoughts.", color = Muted, fontSize = 14.sp)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("$totalNotes", "notes")
                    StatPill("4", "tags")
                    StatPill("1", "pinned")
                }
            }
        }
    }
}

@Composable
fun StatPill(number: String, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.68f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(number, fontWeight = FontWeight.Black, color = SakuraDark)
        Spacer(Modifier.width(5.dp))
        Text(label, color = Muted, fontSize = 12.sp)
    }
}

@Composable
fun ModernSearch(value: String, onChange: (String) -> Unit) {
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
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.04f else 1f, label = "pillScale")
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Ink else Color.White.copy(alpha = 0.68f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else Muted,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ModernNoteCard(note: SakuraNote, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (note.pinned) 188.dp else 170.dp)
            .clickable { onEdit() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = note.color),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(note.tag, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Ink.copy(alpha = 0.75f))
                }
                Spacer(Modifier.weight(1f))
                if (note.pinned) Icon(Icons.Filled.Star, contentDescription = null, tint = SakuraDark, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.height(12.dp))

            Text(
                note.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Text(
                note.content,
                fontSize = 13.sp,
                color = Ink.copy(alpha = 0.62f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(note.date, color = Ink.copy(alpha = 0.45f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Ink.copy(alpha = 0.62f), modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Ink.copy(alpha = 0.62f), modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable
fun ModernBottomBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(Ink.copy(alpha = 0.92f))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(26.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem("Notes", true)
            BottomItem("Tasks", false)
            BottomItem("Tags", false)
            BottomItem("Me", false)
        }
    }
}

@Composable
fun BottomItem(label: String, active: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) Color.White.copy(alpha = 0.16f) else Color.Transparent)
            .padding(horizontal = if (active) 12.dp else 2.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (active) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Sakura, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = if (active) Color.White else Color.White.copy(alpha = 0.56f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun ModernEditorSheet(
    note: SakuraNote?,
    onClose: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var tag by remember { mutableStateOf(note?.tag ?: "Idea") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            color = Cream
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (note == null) "New note" else "Edit note", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Ink)
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write something soft...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(22.dp)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Tag") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp)
                )

                Spacer(Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                        Text("Cancel", color = Muted, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onSave(title, content, tag) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
