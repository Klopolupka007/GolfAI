package com.scrollz.golfai.presentation.reportScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scrollz.golfai.presentation.reportScreen.ReportState

@Composable
fun TechniqueErrors(
    modifier: Modifier = Modifier,
    state: ReportState
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Ракурс съемки",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (state.orientation) {
                    -1 -> "Сбоку, взгляд направлен влево"
                    0 -> "Лицом к камере"
                    1 -> "Сбоку, взгляд направлен вправо"
                    else -> ""
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Базовые ошибки",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (state.noErrors) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Отсутствуют",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            if (state.elbowCornerErrorP1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "- Сгиб локтя относительно туловища в начальной позиции",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            if (state.elbowCornerErrorP7) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "- Сгиб локтя относительно туловища в момент удара",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            if (state.kneeCornerErrorP1) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "- Сгиб коленей в начальной позиции",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            if (state.kneeCornerErrorP7) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "- Сгиб коленей в момент удара",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            if (state.headError) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "- Ширина постановки ног",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
            if (state.legsError) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "- Смещение головы",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}
