package com.example.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.utils.TtsHelper
import com.example.utils.toBase64
import com.example.viewmodel.LingoLensViewModel
import com.example.viewmodel.TranslationState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: LingoLensViewModel,
    ttsHelper: TtsHelper
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    
    if (cameraPermissionState.status.isGranted) {
        CameraContent(viewModel, ttsHelper)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera permission is required to use this feature.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Grant Permission")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraContent(viewModel: LingoLensViewModel, ttsHelper: TtsHelper) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val translationState by viewModel.translationState.collectAsState()
    val targetLanguage by viewModel.targetLanguage.collectAsState()
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        
                        imageCapture = ImageCapture.Builder().build()
                        
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview, imageCapture
                            )
                        } catch (exc: Exception) {
                            // Handle exception
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Result Overlay
            when (val state = translationState) {
                is TranslationState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is TranslationState.Success -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "English: ${state.result.englishWord}", style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$targetLanguage: ${state.result.translatedWord}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    ttsHelper.speak(state.result.translatedWord, targetLanguage)
                                }) {
                                    Icon(Icons.Filled.VolumeUp, contentDescription = "Speak")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.saveCurrentTranslation() }, modifier = Modifier.weight(1f)) {
                                    Text("Save to Deck")
                                }
                                OutlinedButton(onClick = { viewModel.dismissTranslation() }, modifier = Modifier.weight(1f)) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
                is TranslationState.Error -> {
                    Card(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.dismissTranslation() }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
                TranslationState.Idle -> {}
            }
        }
        
        // Controls
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            var expanded by remember { mutableStateOf(false) }
            val languages = listOf("Spanish", "French", "German", "Japanese", "Italian")
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = targetLanguage,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                viewModel.setTargetLanguage(lang)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    imageCapture?.let { capture ->
                        takePhoto(capture, ContextCompat.getMainExecutor(context), onImageCaptured = { bitmap ->
                            viewModel.translateImage(bitmap.toBase64())
                        }, onError = {
                            // Handle capture error
                        })
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = translationState is TranslationState.Idle
            ) {
                Text("Capture & Translate", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun takePhoto(
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.capacity())
            buffer.get(bytes)
            var bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            
            val matrix = Matrix()
            matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            
            onImageCaptured(bitmap)
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            onError(exception)
        }
    })
}
