package com.quraan.teacher.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quraan.teacher.app.presentation.theme.Gold
import com.quraan.teacher.app.presentation.theme.GoldLight
import com.quraan.teacher.app.presentation.theme.QuranAyahStyle
import com.quraan.teacher.app.presentation.theme.TextMuted

@Composable
fun QuranAyahCard(
    ayahNumber: Int,
    ayahText: String,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    val bgColor = when {
        isHighlighted -> GoldLight.copy(alpha = 0.3f)
        ayahNumber % 2 == 0 -> Color(0xFFfafafa)
        else -> Color.White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(enabled = onLongClick != null) { onLongClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50))
                .background(Gold.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u0661\u0660\u0660\u0660\u0660\u0660".takeLast(ayahNumber.toString().length).let {
                    ayahNumber.toString().toCharArray().joinToString("") { char ->
                        when (char) {
                            '0' -> "\u0660"
                            '1' -> "\u0661"
                            '2' -> "\u0662"
                            '3' -> "\u0663"
                            '4' -> "\u0664"
                            '5' -> "\u0665"
                            '6' -> "\u0666"
                            '7' -> "\u0667"
                            '8' -> "\u0668"
                            '9' -> "\u0669"
                            else -> "$char"
                        }
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B6914)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = ayahText,
            style = QuranAyahStyle,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(1f)
        )
    }
}
