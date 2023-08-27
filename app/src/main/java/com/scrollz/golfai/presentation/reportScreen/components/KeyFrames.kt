package com.scrollz.golfai.presentation.reportScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scrollz.golfai.utils.toLastNumber
import java.io.File

@Composable
fun KeyFrames(
    modifier: Modifier = Modifier,
    images: List<File>,
    openImage: (File) -> Unit
) {
    val photoSize = (LocalConfiguration.current.screenWidthDp.dp - 104.dp) / 2
    val gridHeight = photoSize * images.size / 2 + 24.dp * (images.size / 2 + 1)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Позиции",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            if (images.isEmpty()) {
                Text(
                    modifier = Modifier.padding(24.dp),
                    text = "Не удалось загрузить",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.height(gridHeight),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(24.dp),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(
                        items = images
                    ) {image ->
                        ImagePreview(
                            modifier = Modifier
                                .size((LocalConfiguration.current.screenWidthDp.dp - 104.dp) / 2)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { openImage(image) },
                            title = "P${image.name.toLastNumber()}",
                            image = image
                        )
                    }
                }
            }
        }
    }
}
