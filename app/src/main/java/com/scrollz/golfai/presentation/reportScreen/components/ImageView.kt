package com.scrollz.golfai.presentation.reportScreen.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

@Composable
fun ImageView(
    image: File?,
    imageDescription: String?,
    closeImage: () -> Unit
) {
    var layout: LayoutCoordinates? = null
    var scale by remember { mutableFloatStateOf(1f) }
    var translation by remember { mutableStateOf(Offset.Zero) }
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale *= zoomChange
        translation += panChange.times(scale*0.4f)
    }

    BackHandler(onBack = closeImage)

    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize()
            .clipToBounds()
            .transformable(state = transformableState),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier
                .onGloballyPositioned { layout = it }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = translation.x,
                    translationY = translation.y
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(image)
                .build(),
            contentDescription = imageDescription,
            contentScale = ContentScale.Fit
        )
    }

    LaunchedEffect(transformableState.isTransformInProgress) {
        if (!transformableState.isTransformInProgress) {
            if (scale < 1f) {
                val originScale = scale
                val originTranslation = translation
                AnimationState(initialValue = 0f).animateTo(
                    targetValue =  1f,
                    animationSpec = SpringSpec(stiffness = Spring.StiffnessMediumLow)
                ) {
                    scale = originScale + (1 - originScale) * this.value
                    translation = originTranslation * (1 - this.value)
                }
            }
            if (scale > 4f) {
                val originScale = scale
                AnimationState(initialValue = 1f).animateTo(
                    targetValue = 0f,
                    animationSpec = SpringSpec(stiffness = Spring.StiffnessMediumLow)
                ) {
                    scale = 3 + (originScale - 3) * this.value
                }
            }
            if (layout == null) return@LaunchedEffect
            val maxX = layout!!.size.width * (scale - 1) / 2f
            val maxY = layout!!.size.height * (scale - 1) / 2f
            val target = Offset(
                translation.x.coerceIn(-maxX, maxX),
                translation.y.coerceIn(-maxY, maxY)
            )
            AnimationState(
                typeConverter = Offset.VectorConverter,
                initialValue = translation
            ).animateTo(
                targetValue = target,
                animationSpec = SpringSpec(stiffness = Spring.StiffnessMediumLow)
            ) {
                translation = this.value
            }
        }
    }
}
