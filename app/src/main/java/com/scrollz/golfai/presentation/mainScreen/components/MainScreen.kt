package com.scrollz.golfai.presentation.mainScreen.components

import android.Manifest
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scrollz.golfai.presentation.mainScreen.MainEvent
import com.scrollz.golfai.presentation.mainScreen.MainState
import com.scrollz.golfai.utils.RecordVideoContract
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    state: MainState,
    onEvent: (MainEvent) -> Unit
) {
    val context = LocalContext.current

    val pickVideoResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Log.e("recording", "${uri?.encodedPath}")
        }
    )

    val captureVideoResultLauncher = rememberLauncherForActivityResult(
        contract = RecordVideoContract(),
        onResult = { success ->
            if (success) {
                Log.e("recording", "success")
            }
        }
    )

    val cameraPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val date = SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(Date())
                val fileName = "GolfAI_$date.mp4"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_MOVIES + File.separator + "GolfAI")
                }

                val resolver = context.contentResolver
                val videoCollection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                val uri = resolver.insert(videoCollection, contentValues)

                captureVideoResultLauncher.launch(uri)
            }
        }
    )

    val startRecording = remember { {
        cameraPermissionResultLauncher.launch(Manifest.permission.CAMERA)
    } }

    val pickVideo = remember { {
        pickVideoResultLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    } }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar()
        },
        floatingActionButton = {
            SplitFAB(
                startRecording = startRecording,
                pickVideo = pickVideo
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Список",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
