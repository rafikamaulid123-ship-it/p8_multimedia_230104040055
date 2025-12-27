package id.antasari.p8_multimedia_230104040055.ui.video

import android.Manifest
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.*
import id.antasari.p8_multimedia_230104040055.ui.home.SoftTeal
import id.antasari.p8_multimedia_230104040055.util.FileManagerUtility
import id.antasari.p8_multimedia_230104040055.util.VideoFileData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VideoPlayerScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var videoFiles by remember { mutableStateOf<List<VideoFileData>>(emptyList()) }
    var selectedVideo by remember { mutableStateOf<VideoFileData?>(null) }
    
    val storagePermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_MEDIA_VIDEO))
    } else {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }
    
    LaunchedEffect(storagePermissions.allPermissionsGranted) {
        if (storagePermissions.allPermissionsGranted) {
            videoFiles = FileManagerUtility.getVideoFiles(context)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video Player", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftTeal)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!storagePermissions.allPermissionsGranted) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.VideoLibrary, null, modifier = Modifier.size(64.dp), tint = SoftTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Izinkan akses storage untuk melihat video")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { storagePermissions.launchMultiplePermissionRequest() }) {
                        Text("Berikan Izin")
                    }
                }
            } else if (videoFiles.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.VideoLibrary, null, modifier = Modifier.size(64.dp), tint = SoftTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tidak ada video")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(videoFiles) { video ->
                        VideoItem(
                            video = video,
                            onClick = { selectedVideo = video }
                        )
                    }
                }
            }
        }
        
        selectedVideo?.let { video ->
            VideoPlayerDialog(
                video = video,
                onDismiss = { selectedVideo = null }
            )
        }
    }
}

@Composable
fun VideoItem(
    video: VideoFileData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(video.uri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            
            Icon(
                Icons.Default.PlayCircle,
                contentDescription = "Play",
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center),
                tint = Color.White
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(
                    text = video.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = FileManagerUtility.formatDuration(video.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = FileManagerUtility.formatFileSize(video.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerDialog(
    video: VideoFileData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.uri))
            prepare()
            playWhenReady = true
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        useController = true
                        controllerShowTimeoutMs = 3000
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            IconButton(
                onClick = {
                    exoPlayer.stop()
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
