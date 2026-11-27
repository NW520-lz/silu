package com.example.ui.screens

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RetouchHistoryStep
import com.example.model.RetouchIntent
import com.example.ui.components.SiluPhotoLayer
import com.example.ui.components.savePhotoLosslessToGallery
import com.example.ui.theme.SiluBlack
import com.example.ui.theme.SiluBorder
import com.example.ui.theme.SiluEmerald
import com.example.ui.theme.SiluGlass
import com.example.ui.theme.SiluGlassStrong
import com.example.ui.theme.SiluSurface
import com.example.ui.theme.SiluTextPrimary
import com.example.ui.theme.SiluTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun CompareSaveScreen(
    @DrawableRes photoDrawable: Int?,
    photoBitmap: Bitmap?,
    activeIntents: Set<RetouchIntent>,
    faceLockEnabled: Boolean,
    historySteps: List<RetouchHistoryStep>,
    onToggleFaceLock: (Boolean) -> Unit,
    onRestoreHistoryStep: (RetouchHistoryStep) -> Unit,
    onContinueRetouch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var splitFraction by remember { mutableFloatStateOf(0.50f) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(saveFeedback) {
        if (saveFeedback != null) {
            delay(2600)
            saveFeedback = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SiluBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Minimal Top Header: Back | AI 智能精修 | 历史
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onContinueRetouch),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBackIos,
                        contentDescription = "返回精修",
                        tint = SiluTextPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 3.dp)
                    )
                }

                Text(
                    text = "AI 智能精修",
                    color = SiluTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showHistorySheet = !showHistorySheet }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "历史记录",
                        tint = if (showHistorySheet) SiluEmerald else SiluTextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "历史",
                        color = if (showHistorySheet) SiluEmerald else SiluTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Central Interactive Before / After Comparison Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(0.8.dp, SiluBorder, RoundedCornerShape(20.dp))
            ) {
                val canvasWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                val handleRadiusPx = with(LocalDensity.current) { 17.dp.toPx() }

                val leftClipShape = remember(splitFraction) {
                    GenericShape { size, _ ->
                        addRect(Rect(0f, 0f, size.width * splitFraction, size.height))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(canvasWidthPx) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (canvasWidthPx > 0f) {
                                    splitFraction = (splitFraction + dragAmount / canvasWidthPx)
                                        .coerceIn(0.06f, 0.94f)
                                }
                            }
                        }
                        .pointerInput(canvasWidthPx) {
                            detectTapGestures { offset ->
                                if (canvasWidthPx > 0f) {
                                    splitFraction = (offset.x / canvasWidthPx).coerceIn(0.06f, 0.94f)
                                }
                            }
                        }
                ) {
                    // Full Base Layer (Right side visible): AI 精修
                    SiluPhotoLayer(
                        drawableRes = photoDrawable,
                        customBitmap = photoBitmap,
                        contentDescription = "AI 精修效果",
                        isOriginalPreview = false,
                        activeIntents = activeIntents.ifEmpty {
                            setOf(RetouchIntent.WARM_BACKLIGHT, RetouchIntent.NATURAL_SKIN)
                        },
                        faceLockEnabled = faceLockEnabled,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Left Clipped Layer: 原图 (Sub-pixel aligned)
                    SiluPhotoLayer(
                        drawableRes = photoDrawable,
                        customBitmap = photoBitmap,
                        contentDescription = "原图",
                        isOriginalPreview = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(leftClipShape)
                    )

                    // Top-left "原图" Label
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x99121215))
                            .border(0.8.dp, SiluBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "原图",
                            color = SiluTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Top-right "AI 精修" Label
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x99121215))
                            .border(0.8.dp, SiluBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AI 精修",
                            color = SiluTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Draggable Vertical Pure White Split Divider Line
                    val dividerX = (canvasWidthPx * splitFraction).roundToInt()
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dividerX - 1, 0) }
                            .width(1.6.dp)
                            .fillMaxHeight()
                            .background(Color.White.copy(alpha = 0.95f))
                    )

                    // Centered Circular Drag Handle with Minimalist Arrows
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset { IntOffset(dividerX - handleRadiusPx.roundToInt(), 0) }
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0x22000000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Code,
                            contentDescription = "拖动对比原图与精修",
                            tint = SiluBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compact Bottom Action Dock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(SiluSurface)
                    .border(
                        width = 1.dp,
                        color = SiluBorder,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Row 1: Facial Identity Lock Switch (Default ON)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "五官原貌锁定",
                            color = SiluTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (faceLockEnabled) "已锁定原生骨骼与皮肤质感" else "已解除锁定 · 允许光影重塑",
                            color = SiluTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = faceLockEnabled,
                        onCheckedChange = onToggleFaceLock,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SiluEmerald,
                            uncheckedThumbColor = Color(0xFFD4D4D8),
                            uncheckedTrackColor = Color(0xFF2C2C30),
                            uncheckedBorderColor = SiluBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Row 2: Secondary Continue Retouch + Primary Lossless Save CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(CircleShape)
                            .background(SiluGlass)
                            .border(1.dp, SiluBorder, CircleShape)
                            .clickable(onClick = onContinueRetouch),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "继续对话微调",
                            color = SiluTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.32f)
                            .height(50.dp)
                            .clip(CircleShape)
                            .background(SiluEmerald)
                            .clickable(enabled = !isSaving) {
                                isSaving = true
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        savePhotoLosslessToGallery(
                                            context = context,
                                            drawableRes = photoDrawable,
                                            customBitmap = photoBitmap,
                                            activeIntents = activeIntents.ifEmpty {
                                                setOf(RetouchIntent.WARM_BACKLIGHT, RetouchIntent.NATURAL_SKIN)
                                            },
                                            faceLockEnabled = faceLockEnabled
                                        )
                                    }
                                    isSaving = false
                                    saveFeedback = if (result.isSuccess) {
                                        "已无损保存至相册 · 原画质 PNG"
                                    } else {
                                        "保存失败：${result.exceptionOrNull()?.localizedMessage ?: "请检查存储权限"}"
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSaving) "无损导出中…" else "无损保存到相册",
                            color = SiluBlack,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Frosted Edit History Overlay (when clicking "历史")
        AnimatedVisibility(
            visible = showHistorySheet,
            enter = fadeIn() + slideInVertically { -it / 4 },
            exit = fadeOut() + slideOutVertically { -it / 4 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 56.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(248.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SiluGlassStrong)
                    .border(1.dp, SiluBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "精修版本历史",
                    color = SiluTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                historySteps.forEach { step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x55242428))
                            .clickable {
                                onRestoreHistoryStep(step)
                                showHistorySheet = false
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title,
                                color = SiluTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            step.customNote?.let { note ->
                                Text(
                                    text = note,
                                    color = SiluTextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = step.timestamp,
                            color = SiluTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Lossless Save Confirmation Toast
        AnimatedVisibility(
            visible = saveFeedback != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 58.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xE60E1C16))
                    .border(1.dp, SiluEmerald.copy(alpha = 0.55f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = SiluEmerald,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = saveFeedback.orEmpty(),
                    color = SiluTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
