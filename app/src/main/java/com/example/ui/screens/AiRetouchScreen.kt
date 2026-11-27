package com.example.ui.screens

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Compare
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RetouchIntent
import com.example.ui.components.SiluPhotoLayer
import com.example.ui.theme.SiluBlack
import com.example.ui.theme.SiluBorder
import com.example.ui.theme.SiluEmerald
import com.example.ui.theme.SiluEmeraldDarkBg
import com.example.ui.theme.SiluEmeraldGlow
import com.example.ui.theme.SiluEmeraldTint
import com.example.ui.theme.SiluGlass
import com.example.ui.theme.SiluGlassStrong
import com.example.ui.theme.SiluTextPrimary
import com.example.ui.theme.SiluTextSecondary
import kotlinx.coroutines.delay

@Composable
fun AiRetouchScreen(
    @DrawableRes photoDrawable: Int?,
    photoBitmap: Bitmap?,
    activeIntents: Set<RetouchIntent>,
    faceLockEnabled: Boolean,
    promptText: String,
    onPromptChange: (String) -> Unit,
    onToggleIntent: (RetouchIntent) -> Unit,
    onOneTapTemplateRetouch: () -> Unit,
    onSubmitCustomPrompt: (String) -> Unit,
    onBackToCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    var holdingOriginal by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isListeningVoice by remember { mutableStateOf(false) }
    var pendingCustomSubmit by remember { mutableStateOf<String?>(null) }

    val scanProgress by animateFloatAsState(
        targetValue = if (isProcessing) 1f else 0f,
        animationSpec = tween(durationMillis = 760, easing = LinearEasing),
        label = "aiScanSweep"
    )

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(820)
            isProcessing = false
            val custom = pendingCustomSubmit
            pendingCustomSubmit = null
            if (custom != null) {
                onSubmitCustomPrompt(custom)
            } else {
                onOneTapTemplateRetouch()
            }
        }
    }

    LaunchedEffect(isListeningVoice) {
        if (isListeningVoice) {
            delay(1300)
            onPromptChange("把背景路人去掉，保留脸部真实感与暖阳发丝光")
            isListeningVoice = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SiluBlack)
            .imePadding()
    ) {
        // Top 65% — High-Resolution Portrait Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
        ) {
            SiluPhotoLayer(
                drawableRes = photoDrawable,
                customBitmap = photoBitmap,
                contentDescription = "待精修人像预览",
                isOriginalPreview = holdingOriginal || activeIntents.isEmpty(),
                activeIntents = activeIntents,
                faceLockEnabled = faceLockEnabled,
                modifier = Modifier.fillMaxSize()
            )

            // Top Optical Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0x880A0A0C), Color.Transparent)
                        )
                    )
            )

            // Top-left Back Button & Top-right Minimal Compare-Original Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SiluGlass)
                        .border(0.8.dp, SiluBorder, CircleShape)
                        .clickable(onClick = onBackToCamera),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBackIos,
                        contentDescription = "返回相机",
                        tint = SiluTextPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(start = 3.dp)
                    )
                }

                // Minimalist Compare Original Pill (Hold or Tap)
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (holdingOriginal) Color(0xCC242428) else SiluGlass)
                        .border(
                            width = 1.dp,
                            color = if (holdingOriginal) Color.White.copy(alpha = 0.45f) else SiluBorder,
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    holdingOriginal = true
                                    tryAwaitRelease()
                                    holdingOriginal = false
                                }
                            )
                        }
                        .padding(horizontal = 13.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Compare,
                        contentDescription = null,
                        tint = SiluTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (holdingOriginal) "原图预览中" else "对比原图",
                        color = SiluTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            // Sleek Optical Light Sweep while AI Retouch Agent processes
            androidx.compose.animation.AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(180)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = (scanProgress * 360).dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        SiluEmerald.copy(alpha = 0.24f),
                                        SiluEmerald.copy(alpha = 0.72f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 18.dp)
                            .clip(CircleShape)
                            .background(Color(0xCC0D1914))
                            .border(1.dp, SiluEmerald.copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "AI 精修引擎正在保留原生肌理并优化光影…",
                            color = SiluEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Bottom 35% — Floating Dark Frosted Glass Agent Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(SiluGlassStrong)
                .border(
                    width = 1.dp,
                    color = SiluBorder,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(top = 10.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Subtle Top Grab Handle
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.26f))
                )
            }

            // Prominent Core AI Action Button: ✨ 按构图模板一键精修
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            ambientColor = SiluEmeraldGlow,
                            spotColor = SiluEmeraldGlow
                        )
                        .clip(CircleShape)
                        .background(SiluEmeraldDarkBg)
                        .border(1.4.dp, SiluEmerald, CircleShape)
                        .clickable(enabled = !isProcessing) {
                            pendingCustomSubmit = null
                            isProcessing = true
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = SiluEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProcessing) "正在按构图模板精修…" else "按构图模板一键精修",
                        color = SiluEmerald,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            // Middle Row: Horizontally Scrollable Intent Quick Capsules
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(RetouchIntent.entries) { intent ->
                    val isActive = intent in activeIntents
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isActive) SiluEmeraldTint else Color(0x801C1C1E))
                            .border(
                                width = 1.dp,
                                color = if (isActive) SiluEmerald.copy(alpha = 0.65f) else SiluBorder,
                                shape = CircleShape
                            )
                            .clickable { onToggleIntent(intent) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = intent.label,
                            color = if (isActive) SiluEmerald else SiluTextPrimary.copy(alpha = 0.88f),
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }

            // Bottom Minimalist Agent Dialog Bar with Microphone / Send
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp)
                    .clip(CircleShape)
                    .background(Color(0x990E0E11))
                    .border(1.dp, SiluBorder, CircleShape)
                    .padding(start = 10.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restrained subtle optical lens ring accent
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF34D399),
                                    Color(0xFF93C5FD),
                                    Color(0xFFE5E7EB),
                                    Color(0xFF34D399)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (promptText.isEmpty()) {
                        Text(
                            text = if (isListeningVoice) {
                                "正在聆听你的修图指令…"
                            } else {
                                "补充你的微调需求，例如：把背景路人去掉，保留脸部真实感…"
                            },
                            color = if (isListeningVoice) SiluEmerald else SiluTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = promptText,
                        onValueChange = onPromptChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = SiluTextPrimary,
                            fontSize = 13.sp
                        ),
                        cursorBrush = SolidColor(SiluEmerald),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (promptText.isNotBlank() && !isProcessing) {
                                    pendingCustomSubmit = promptText.trim()
                                    isProcessing = true
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Microphone or Send button
                val hasText = promptText.isNotBlank()
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                hasText -> SiluEmerald
                                isListeningVoice -> SiluEmeraldTint
                                else -> Color(0x9926262A)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isListeningVoice) SiluEmerald else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable(enabled = !isProcessing) {
                            if (hasText) {
                                pendingCustomSubmit = promptText.trim()
                                isProcessing = true
                            } else {
                                isListeningVoice = !isListeningVoice
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasText) Icons.AutoMirrored.Outlined.Send else Icons.Outlined.Mic,
                        contentDescription = if (hasText) "发送微调指令" else "语音输入",
                        tint = when {
                            hasText -> SiluBlack
                            isListeningVoice -> SiluEmerald
                            else -> SiluTextPrimary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
