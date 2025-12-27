package id.antasari.p8_multimedia_230104040055.ui.player

import android.Manifest
import android.net.Uri
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.accompanist.permissions.*
import id.antasari.p8_multimedia_230104040055.ui.home.SoftTeal
import id.antasari.p8_multimedia_230104040055.ui.home.SoftPink
import id.antasari.p8_multimedia_230104040055.ui.home.SoftLavender
import id.antasari.p8_multimedia_230104040055.util.AudioFileData
import id.antasari.p8_multimedia_230104040055.util.FileManagerUtility
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AudioPlayerScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var audioFiles by remember { mutableStateOf<List<AudioFileData>>(emptyList()) }
    var currentAudio by remember { mutableStateOf<AudioFileData?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    
    val storagePermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_MEDIA_AUDIO))
    } else {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }
    
    LaunchedEffect(storagePermissions.allPermissionsGranted) {
        if (storagePermissions.allPermissionsGranted) {
            audioFiles = FileManagerUtility.getAudioFiles(context)
        }
    }
    
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (isPlaying) {
                currentPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration.coerceAtLeast(0L)
            }
            delay(100)
        }
    }
    
    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Player", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(padding)
        ) {
            if (!storagePermissions.allPermissionsGranted) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(64.dp), tint = SoftTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Izinkan akses storage untuk memutar audio")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { storagePermissions.launchMultiplePermissionRequest() }) {
                        Text("Berikan Izin")
                    }
                }
            } else if (audioFiles.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(64.dp), tint = SoftTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tidak ada file audio")
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (currentAudio != null) {
                        NowPlayingCard(
                            audio = currentAudio!!,
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            onPlayPause = {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            },
                            onSeek = { position ->
                                exoPlayer.seekTo(position)
                            }
                        )
                    }
                    
                    Text(
                        text = "Daftar Audio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(audioFiles) { audio ->
                            AudioItem(
                                audio = audio,
                                isPlaying = currentAudio == audio && isPlaying,
                                onClick = {
                                    currentAudio = audio
                                    exoPlayer.setMediaItem(MediaItem.fromUri(audio.uri))
                                    exoPlayer.prepare()
                                    exoPlayer.play()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NowPlayingCard(
    audio: AudioFileData,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "music")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SoftPink.copy(alpha = 0.3f),
                            SoftLavender.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                SoftPink,
                                SoftLavender
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = audio.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = audio.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                    onValueChange = { value ->
                        onSeek((value * duration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = FileManagerUtility.formatDuration(currentPosition),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = FileManagerUtility.formatDuration(duration),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FloatingActionButton(
                onClick = onPlayPause,
                containerColor = SoftTeal,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun AudioItem(
    audio: AudioFileData,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) SoftTeal.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) SoftTeal else SoftTeal.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Default.BarChart else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = audio.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${audio.artist} • ${FileManagerUtility.formatDuration(audio.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (isPlaying) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = SoftTeal
                )
            }
        }
    }
}
