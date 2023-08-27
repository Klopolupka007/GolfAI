package com.scrollz.golfai.presentation.reportScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.scrollz.golfai.ui.theme.DarkGrayD
import com.scrollz.golfai.ui.theme.WhiteD
import java.io.File

@Composable
fun ImagePreview(
    modifier: Modifier = Modifier,
    title: String,
    image: File,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .build(),
            contentDescription = image.name,
            contentScale = ContentScale.Crop
        )
        Row(
            modifier = Modifier.fillMaxWidth().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        DarkGrayD
                    )
                )
            ),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = WhiteD
            )
        }
    }
}
