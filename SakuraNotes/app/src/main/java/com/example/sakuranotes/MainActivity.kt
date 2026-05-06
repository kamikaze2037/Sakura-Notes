package com.example.sakuranotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    val pinned: Boolean = false
)

private val SakuraPink = Color(0xFFF9BDC2)
private val SakuraDeep = Color(0xFFC2275C)
private val SakuraBlue = Color(0xFF458DC4)
private val SakuraGreen = Color(0xFF638045)
private val SakuraInk = Color(0xFF2B1D2A)
private val SakuraSurface = Color(0xFFFFF7F8)
private val CardPink = Color(0xFFFFEEF2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SakuraNotesApp() {
    val notes = remember {
        mutableStateListOf(
            SakuraNote(1, "Ý tưởng app", "Ứng dụng ghi chú giao diện hoa anh đào, hỗ trợ ghi nhanh, tag, nhắc nhở và bảo mật.", "Idea", "Hôm nay", true),
            SakuraNote(2, "Việc cần làm", "Thiết kế splash screen, màn hình Home, màn hình tạo ghi chú và hệ thống màu Material 3.", "Todo", "Hôm nay"),
            SakuraNote(3, "Ghi chú riêng", "Thêm khóa vân tay cho những note cá nhân.", "Private", "Hôm qua"),
            SakuraNote(4, "Moodboard", "Rosewater, Rose Red, Blue Grotto, Kelly Green. Bố cục trắng, mềm, bo góc lớn.", "Design", "2 ngày trước")
        )
    }

    var search by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<SakuraNote?>(null) }

    val filteredNotes = notes.filter {
        it.title.contains(search, true) || it.content.contains(search, true) || it.tag.contains(search, true)
    }.sortedByDescending { it.pinned }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.White, SakuraSurface, Color(0xFFFFE7EC))))
        ) {
            SakuraBackground()

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Sakura Notes", fontWeight = FontWeight.Bold, color = SakuraInk)
                                Text("Ghi nhanh mọi ý tưởng", fontSize = 12.sp, color = SakuraDeep.copy(alpha = 0.75f))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        actions = {
                            IconButton(onClick = { search = "" }) {
                                Icon(Icons.Filled.Favorite, contentDescription = "Theme", tint = SakuraDeep)
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            editingNote = null
                            showEditor = true
                        },
                        containerColor = SakuraDeep,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Tạo ghi chú")
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 18.dp)
                        .fillMaxSize()
                ) {
                    HeroCard()
                    Spacer(Modifier.height(16.dp))
                    SearchBox(value = search, onChange = { search = it })
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TagChip("All", SakuraDeep)
                        TagChip("Idea", SakuraBlue)
                        TagChip("Todo", SakuraGreen)
                        TagChip("Private", SakuraInk)
                    }

                    Spacer(Modifier.height(14.dp))
                    Text("Ghi chú gần đây", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SakuraInk)
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                        items(filteredNotes, key = { it.id }) { note ->
                            NoteCard(
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
                NoteEditorSheet(
                    note = editingNote,
                    onClose = { showEditor = false },
                    onSave = { title, content, tag ->
                        if (editingNote == null) {
                            notes.add(
                                0,
                                SakuraNote(
                                    id = (notes.maxOfOrNull { it.id } ?: 0) + 1,
                                    title = title.ifBlank { "Ghi chú mới" },
                                    content = content.ifBlank { "Chưa có nội dung" },
                                    tag = tag.ifBlank { "Idea" },
                                    date = "Vừa xong"
                                )
                            )
                        } else {
                            val index = notes.indexOfFirst { it.id == editingNote!!.id }
                            if (index >= 0) {
                                notes[index] = editingNote!!.copy(
                                    title = title.ifBlank { "Ghi chú" },
                                    content = content.ifBlank { "Chưa có nội dung" },
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
fun SakuraBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(SakuraPink.copy(alpha = 0.22f), radius = 180.dp.toPx(), center = Offset(size.width * 0.88f, size.height * 0.12f))
        drawCircle(SakuraBlue.copy(alpha = 0.08f), radius = 130.dp.toPx(), center = Offset(size.width * 0.05f, size.height * 0.32f))

        val branch = Path().apply {
            moveTo(size.width * 0.58f, 0f)
            cubicTo(size.width * 0.72f, size.height * 0.08f, size.width * 0.82f, size.height * 0.16f, size.width, size.height * 0.20f)
        }
        drawPath(branch, SakuraGreen.copy(alpha = 0.25f), style = Stroke(width = 5.dp.toPx()))

        repeat(12) { i ->
            val x = size.width * (0.60f + (i % 6) * 0.07f)
            val y = size.height * (0.03f + (i / 6) * 0.11f + (i % 3) * 0.025f)
            drawOval(SakuraPink.copy(alpha = 0.45f), topLeft = Offset(x, y), size = Size(18.dp.toPx(), 10.dp.toPx()))
        }
    }
}

@Composable
fun HeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.78f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(58.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(SakuraPink, SakuraDeep.copy(alpha = 0.8f)))),
                contentAlignment = Alignment.Center
            ) { Text("🌸", fontSize = 28.sp) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Hôm nay bạn muốn ghi gì?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SakuraInk)
                Text("Ghi nhanh, gắn tag, tìm kiếm và bảo vệ note riêng tư.", fontSize = 13.sp, color = SakuraInk.copy(alpha = 0.68f))
            }
        }
    }
}

@Composable
fun SearchBox(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(22.dp),
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Tìm kiếm", tint = SakuraDeep) },
        placeholder = { Text("Tìm ghi chú, tag, nội dung...") }
    )
}

@Composable
fun TagChip(label: String, color: Color) {
    AssistChip(
        onClick = {},
        label = { Text(label, color = color, fontWeight = FontWeight.SemiBold) },
        leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(color)) }
    )
}

@Composable
fun NoteCard(note: SakuraNote, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (note.pinned) CardPink else Color.White.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = note.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SakuraInk,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.pinned) Icon(Icons.Filled.Star, contentDescription = "Ghim", tint = SakuraDeep, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(note.content, color = SakuraInk.copy(alpha = 0.70f), fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("#${note.tag}", color = SakuraDeep, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.weight(1f))
                Text(note.date, color = SakuraInk.copy(alpha = 0.48f), fontSize = 12.sp)
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Sửa", tint = SakuraBlue) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Xóa", tint = SakuraDeep) }
            }
        }
    }
}

@Composable
fun NoteEditorSheet(note: SakuraNote?, onClose: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var tag by remember { mutableStateOf(note?.tag ?: "Idea") }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.22f)), contentAlignment = Alignment.BottomCenter) {
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp), color = Color.White) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (note == null) "Tạo ghi chú mới" else "Chỉnh sửa ghi chú", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SakuraInk, modifier = Modifier.weight(1f))
                    IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Đóng") }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tiêu đề") }, shape = RoundedCornerShape(18.dp), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, modifier = Modifier.fillMaxWidth().height(150.dp), label = { Text("Nội dung") }, shape = RoundedCornerShape(18.dp))
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = tag, onValueChange = { tag = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tag") }, shape = RoundedCornerShape(18.dp), singleLine = true)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onClose, modifier = Modifier.weight(1f)) { Text("Hủy", color = SakuraInk) }
                    Button(onClick = { onSave(title, content, tag) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = SakuraDeep), shape = RoundedCornerShape(18.dp)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Lưu")
                    }
                }
            }
        }
    }
}
