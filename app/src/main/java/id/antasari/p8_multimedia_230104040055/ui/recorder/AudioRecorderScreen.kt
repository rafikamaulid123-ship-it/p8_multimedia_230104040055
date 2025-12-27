package id.antasari.p8_multimedia_230104040055.ui.recorder

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.permissions.*
import id.antasari.p8_multimedia_230104040055.ui.home.DarkBackground
import id.antasari.p8_multimedia_230104040055.ui.home.SoftTeal
import id.antasari.p8_multimedia_230104040055.ui.home.SoftMint
import id.antasari.p8_multimedia_230104040055.ui.home.LightGray
import id.antasari.p8_multimedia_230104040055.util.FileManagerUtility
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AudioRecorderScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0L) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentRecordingFile by remember { mutableStateOf<File?>(null) }
    var recordings by remember { mutableStateOf<List<File>>(emptyList()) }
    var playingRecording by remember { mutableStateOf<File?>(null) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    LaunchedEffect(Unit) {
        loadRecordings(context).let { recordings = it }
    }
    
    LaunchedEffect(isRecording) {
        while (isRecording && !isPaused) {
            delay(1000)
            recordingTime++
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            exoPlayer.release()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Recorder", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftTeal)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
        ) {
            if (!audioPermission.status.isGranted) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Mic, null, modifier = Modifier.size(64.dp), tint = SoftTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Izinkan akses mikrofon untuk merekam audio", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { audioPermission.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTeal)
                    ) {
                        Text("Berikan Izin")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Recording Status
                    Text(
                        text = if (isRecording) {
                            if (isPaused) "Paused" else "Recording"
                        } else "Ready to Record",
                        fontSize = 16.sp,
                        color = if (isRecording) Color.White else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Mic Icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
                            initialValue = 1f,
                            targetValue = if (isRecording && !isPaused) 1.2f else 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(SoftMint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Start/Stop Recording Button
                    if (!isRecording) {
                        Button(
                            onClick = {
                                val file = createAudioFile(context)
                                currentRecordingFile = file
                                mediaRecorder = createMediaRecorder(context, file).apply {
                                    prepare()
                                    start()
                                }
                                isRecording = true
                                isPaused = false
                                recordingTime = 0L
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftMint),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                "Start Recording",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Button(
                                    onClick = {
                                        if (isPaused) {
                                            mediaRecorder?.resume()
                                        } else {
                                            mediaRecorder?.pause()
                                        }
                                        isPaused = !isPaused
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF424242)
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text(if (isPaused) "Resume" else "Pause")
                                }
                            }
                            
                            Button(
                                onClick = {
                                    try {
                                        mediaRecorder?.stop()
                                        mediaRecorder?.release()
                                        mediaRecorder = null
                                        isRecording = false
                                        isPaused = false
                                        recordingTime = 0L
                                        recordings = loadRecordings(context)
                                        Toast.makeText(context, "Rekaman tersimpan!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text("Stop")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Divider
                    Divider(color = Color.Gray.copy(alpha = 0.3f))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recordings List Title
                    Text(
                        text = "Daftar Rekaman",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (recordings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.GraphicEq,
                                    null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Belum ada rekaman", color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recordings) { file ->
                                RecordingItem(
                                    file = file,
                                    isPlaying = playingRecording == file,
                                    onPlay = {
                                        if (playingRecording == file) {
                                            exoPlayer.stop()
                                            playingRecording = null
                                        } else {
                                            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                                            exoPlayer.prepare()
                                            exoPlayer.play()
                                            playingRecording = file
                                        }
                                    },
                                    onDelete = {
                                        file.delete()
                                        recordings = loadRecordings(context)
                                        if (playingRecording == file) {
                                            exoPlayer.stop()
                                            playingRecording = null
                                        }
                                        Toast.makeText(context, "Rekaman dihapus", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Footer
                    Text(
                        text = "Copyright © 2025\nPraktikum #8 Menggunakan Multimedia\nS1 Teknologi Informasi UIN Antasari\nBanjarmasin",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingItem(
    file: File,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AudioFile,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = SoftTeal
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = "${formatTime(file.lastModified())} • ${FileManagerUtility.formatFileSize(file.length())}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onPlay) {
                    Text(
                        if (isPlaying) "[Stop]" else "[Edit]",
                        color = SoftTeal,
                        fontSize = 13.sp
                    )
                }
                TextButton(onClick = { showDeleteDialog = true }) {
                    Text(
                        "[Delete]",
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Rekaman?") },
            text = { Text("Rekaman akan dihapus permanen") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

private fun createMediaRecorder(context: Context, file: File): MediaRecorder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }.apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(file.absolutePath)
    }
}

private fun createAudioFile(context: Context): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "audio_$timestamp.mp4"
    return File(context.getExternalFilesDir(null), fileName)
}

private fun loadRecordings(context: Context): List<File> {
    val dir = context.getExternalFilesDir(null)
    return dir?.listFiles { file -> file.name.startsWith("audio_") && file.name.endsWith(".mp4") }
        ?.sortedByDescending { it.lastModified() }
        ?: emptyList()
}

private fun formatTime(millis: Long): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date(millis))
}
