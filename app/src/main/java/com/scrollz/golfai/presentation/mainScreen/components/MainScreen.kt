package com.scrollz.golfai.presentation.mainScreen.components

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.scrollz.golfai.presentation.mainScreen.MainEvent
import com.scrollz.golfai.presentation.mainScreen.MainState
import com.scrollz.golfai.utils.RecordVideoContract
import com.scrollz.golfai.utils.Status
import com.scrollz.golfai.utils.toFineDateTime
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    state: MainState,
    onEvent: (MainEvent) -> Unit,
    onReportClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var uri: Uri? = null

    val processVideo: (Uri) -> Unit = remember { { uri ->
        val dateTime = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        onEvent(MainEvent.ProcessVideo(uri, dateTime))
    } }

    val pickVideoResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { result ->
            result?.let { uri -> processVideo(uri) }
        }
    )

    val captureVideoResultLauncher = rememberLauncherForActivityResult(
        contract = RecordVideoContract(),
        onResult = { success ->
            if (success) { uri?.let { uri -> processVideo(uri) } }
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
                uri = resolver.insert(videoCollection, contentValues)

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
                pickVideo = pickVideo,
                enabled = state.screenStatus != Status.Loading
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.screenStatus == Status.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp),
                    color = MaterialTheme.colorScheme.onBackground,
                    trackColor = MaterialTheme.colorScheme.background,
                    strokeCap = StrokeCap.Round
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f, fill = true),
            ) {
                itemsIndexed(
                    items = state.reports,
                    key = { index, _ -> index }
                ) { index, report ->
                    ReportItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReportClick(report.id ?: -1) },
                        index = report.id ?: -1,
                        dateTime = report.dateTime.toFineDateTime(),
                        deleteReport = { onEvent(MainEvent.DeleteReport(report.id)) }
                    )
                    if (index < state.reports.lastIndex) {
                        Divider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
