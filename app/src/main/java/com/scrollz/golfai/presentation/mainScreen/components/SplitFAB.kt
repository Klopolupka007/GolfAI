package com.scrollz.golfai.presentation.mainScreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SplitFAB(
    modifier: Modifier = Modifier,
    startRecording: () -> Unit = {},
    pickVideo: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        var isSplit by remember { mutableStateOf(false) }

        AnimatedVisibility(
            visible = isSplit,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.height(40.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    text = {
                        Text(
                            text = "Начать запись",
                            style = MaterialTheme.typography.displayMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = "Start recording"
                        )
                    },
                    onClick = {
                        isSplit = !isSplit
                        startRecording()
                    }
                )
                ExtendedFloatingActionButton(
                    modifier = Modifier.height(40.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        focusedElevation = 0.dp,
                        hoveredElevation = 0.dp
                    ),
                    text = {
                        Text(
                            text = "Галерея",
                            style = MaterialTheme.typography.displayMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = "Gallery"
                        )
                    },
                    onClick = {
                        isSplit = !isSplit
                        pickVideo()
                    }
                )
            }
        }
        FloatingActionButton(
            onClick = { isSplit = !isSplit },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            if (isSplit) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }
}
