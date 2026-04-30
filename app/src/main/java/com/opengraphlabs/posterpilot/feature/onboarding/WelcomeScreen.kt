package com.opengraphlabs.posterpilot.feature.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.opengraphlabs.posterpilot.core.theme.EyebrowStyle
import com.opengraphlabs.posterpilot.core.theme.Marigold
import com.opengraphlabs.posterpilot.core.theme.Saffron
import com.opengraphlabs.posterpilot.core.theme.Sindoor
import com.opengraphlabs.posterpilot.core.ui.PosterPilotScaffold

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    PosterPilotScaffold(horizontalPadding = 0.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            HeroBackdrop()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Sindoor)
                        )
                        Text(
                            text = "OPENGRAPH LABS · STUDIO 01",
                            style = EyebrowStyle,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
                        )
                    }
                }

                Column {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            ) { append("Poster") }
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                                append("\nPilot")
                            }
                            withStyle(SpanStyle(color = Sindoor)) { append(".") }
                        },
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 64.sp,
                        lineHeight = 64.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(width = 28.dp, height = 2.dp)
                                .background(Saffron)
                        )
                        Text(
                            text = "A small poster studio for India's local shops. Pick a template, edit a line of copy, share to status — in under a minute.",
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.82f)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        onClick = onContinue
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Begin setup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Saffron),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "→",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "Two short steps · No account needed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun HeroBackdrop() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Saffron flag — diagonal accent occupying the top-right corner.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = (-60).dp)
                .size(width = 280.dp, height = 280.dp)
                .rotate(18f)
                .background(
                    brush = Brush.linearGradient(listOf(Saffron, Marigold))
                )
        )
        // Stacked rule beneath the title field for editorial rhythm.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp, top = 220.dp)
                .size(width = 1.dp, height = 80.dp)
                .background(MaterialTheme.colorScheme.outline)
        )
        // Subtle parchment dot grid suggestion via a single faint bordered circle.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 156.dp)
                .size(120.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
        )
        Canvas(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 32.dp, bottom = 220.dp)
                .size(56.dp)
        ) {
            val r = size.minDimension / 2f
            drawCircle(color = Color(0xFFB71C3A), radius = r, alpha = 0.85f)
        }
    }
}
