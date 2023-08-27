package com.scrollz.golfai.presentation.reportScreen.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.scrollz.golfai.presentation.reportScreen.ReportEvent
import com.scrollz.golfai.presentation.reportScreen.ReportState

@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    state: ReportState,
    onEvent: (ReportEvent) -> Unit,
    navigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val shadowElevation = if (isSystemInDarkTheme()) 0.dp else 10.dp

    Crossfade(
        modifier = modifier,
        targetState = state.isImageOpen,
        label = "photoView",
        animationSpec = tween(400)
    ) { isImageOpen ->
        if (isImageOpen) {
            ImageView(
                image = state.openImage,
                imageDescription = state.openImage?.name,
                closeImage = { onEvent(ReportEvent.CloseImage) }
            )
        } else {
            Scaffold(
                modifier = modifier,
                topBar = {
                    TopBar(
                        id = state.id,
                        dateTime = state.dateTime,
                        navigateBack = navigateBack
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TechniqueErrors(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(shadowElevation, RoundedCornerShape(32.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    KeyFrames(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(shadowElevation, RoundedCornerShape(32.dp)),
                        images = state.images,
                        openImage = { image -> onEvent(ReportEvent.OpenImage(image)) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
