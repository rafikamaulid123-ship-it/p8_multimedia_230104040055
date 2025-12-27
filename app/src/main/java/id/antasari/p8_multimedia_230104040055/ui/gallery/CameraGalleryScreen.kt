package id.antasari.p8_multimedia_230104040055.ui.gallery

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.*
import id.antasari.p8_multimedia_230104040055.ui.home.DarkBackground
import id.antasari.p8_multimedia_230104040055.ui.home.SoftTeal
import id.antasari.p8_multimedia_230104040055.ui.home.SoftMint
import id.antasari.p8_multimedia_230104040055.ui.home.LightGray
import id.antasari.p8_multimedia_230104040055.util.FileManagerUtility
import java.io.File
import java.text.SimpleDateFormat
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import java.util.*

// Helper function to save image to public gallery
private fun saveImageToPublicGallery(context: Context, sourceUri: Uri) {
    try {
        val contentResolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MultimediaStudio")
            }
        }
        
        val uri = contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
        
        uri?.let { targetUri ->
            contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraGalleryScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var showCamera by remember { mutableStateOf(false) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var showChooseOption by remember { mutableStateOf(false) }
    
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val storagePermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_MEDIA_IMAGES))
    } else {
        rememberMultiplePermissionsState(listOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }
    
    LaunchedEffect(refreshTrigger, storagePermissions.allPermissionsGranted) {
        if(storagePermissions.allPermissionsGranted) {
            imageUris = FileManagerUtility.getImageFiles(context)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camera & Gallery", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Open Camera Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (cameraPermission.status.isGranted) {
                            showCamera = true
                        } else {
                            cameraPermission.launchPermissionRequest()
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SoftMint)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Open Camera",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Take a new photo or Video",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Choose from Gallery Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (storagePermissions.allPermissionsGranted) {
                            showChooseOption = true
                        } else {
                            storagePermissions.launchMultiplePermissionRequest()
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = SoftTeal
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Choose from Gallery",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Select existing photo or video",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Preview Area with Zoom & Pan
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = LightGray)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImage != null) {
                        var scale by remember { mutableStateOf(1f) }
                        var offsetX by remember { mutableStateOf(0f) }
                        var offsetY by remember { mutableStateOf(0f) }
                        
                        Image(
                            painter = rememberAsyncImagePainter(selectedImage),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        if (scale > 1f) {
                                            val maxX = (size.width * (scale - 1)) / 2
                                            val maxY = (size.height * (scale - 1)) / 2
                                            offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                                            offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                                        } else {
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                    }
                                },
                            contentScale = ContentScale.Fit
                        )
                        
                        // Reset zoom button
                        if (scale > 1f) {
                            FloatingActionButton(
                                onClick = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .size(48.dp),
                                containerColor = SoftTeal
                            ) {
                                Icon(
                                    Icons.Default.ZoomOut,
                                    "Reset Zoom",
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = SoftTeal
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No image selected",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = "Choose an option above to get started",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Save to Gallery Button
            if (selectedImage != null) {
                Button(
                    onClick = {
                        selectedImage?.let { uri ->
                            saveImageToPublicGallery(context, uri)
                            Toast.makeText(context, "Foto disimpan ke Galeri!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.SaveAlt,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Simpan ke Galeri",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Copyright Footer
            Text(
                text = "Copyright © 2025\nPraktikum #8 Menggunakan Multimedia\nS1 Teknologi Informasi UIN Antasari\nBanjarmasin",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    if (showCamera) {
        CameraView(
            onImageCaptured = { uri ->
                showCamera = false
                selectedImage = uri
                refreshTrigger++
                Toast.makeText(context, "Foto tersimpan!", Toast.LENGTH_SHORT).show()
            },
            onError = { exc ->
                showCamera = false
                Toast.makeText(context, "Error: ${exc.message}", Toast.LENGTH_SHORT).show()
            },
            onClose = { showCamera = false }
        )
    }
    
    if (showChooseOption && imageUris.isNotEmpty()) {
        Dialog(onDismissRequest = { showChooseOption = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TopAppBar(
                        title = { Text("Pilih Foto") },
                        navigationIcon = {
                            IconButton(onClick = { showChooseOption = false }) {
                                Icon(Icons.Default.Close, "Close")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftTeal)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(imageUris) { uri ->
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedImage = uri
                                        showChooseOption = false
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraView(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(32.dp))
        }
        
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
            }) {
                Icon(Icons.Default.Cameraswitch, "Switch Camera", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            
            FloatingActionButton(
                onClick = {
                    val photoFile = File(
                        context.getExternalFilesDir(null),
                        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                            .format(System.currentTimeMillis()) + ".jpg"
                    )
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                onImageCaptured(Uri.fromFile(photoFile))
                            }
                            override fun onError(exc: ImageCaptureException) {
                                onError(exc)
                            }
                        }
                    )
                },
                containerColor = Color.White,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, "Capture", tint = SoftTeal, modifier = Modifier.size(32.dp))
            }
        }
    }
}
