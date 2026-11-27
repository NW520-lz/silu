package com.example.ui.screens

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectMode
import com.example.model.PoseStyle
import com.example.ui.components.PoseOverlayCanvas
import com.example.ui.components.PoseSilhouetteIcon
import com.example.ui.components.SiluPhotoLayer
import com.example.ui.components.TiltRulerOverlay
import com.example.ui.theme.SiluBlack
import com.example.ui.theme.SiluBorder
import com.example.ui.theme.SiluEmerald
import com.example.ui.theme.SiluEmeraldGlow
import com.example.ui.theme.SiluGlass
import com.example.ui.theme.SiluGlassStrong
import com.example.ui.theme.SiluTextPrimary
import com.example.ui.theme.SiluTextSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs

@Composable
fun CameraScreen(
    @DrawableRes viewfinderDrawable: Int?,
    viewfinderBitmap: Bitmap?,
    @DrawableRes albumThumbDrawable: Int?,
    albumThumbBitmap: Bitmap?,
    activePose: PoseStyle,
    activeTemplateTone: String,
    onSelectPose: (PoseStyle) -> Unit,
    onOpenTemplateDrawer: () -> Unit,
    onCapturePhoto: () -> Unit,
    onOpenAlbumRetouch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var flashOn by remember { mutableStateOf(false) }
    var aspectMode by remember { mutableStateOf(AspectMode.RATIO_3_4) }
    var showGuide by remember { mutableStateOf(true) }
    var tiltDegrees by remember { mutableFloatStateOf(0.0f) }
    var shutterFlash by remember { mutableStateOf(false) }

    val isAligned = abs(tiltDegrees) <= 0.6f

    LaunchedEffect(shutterFlash) {
        if (shutterFlash) {
            delay(180)
            shutterFlash = false
            onCapturePhoto()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SiluBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Minimal Leica Camera Control Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SiluBlack)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Flash toggle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { flashOn = !flashOn },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (flashOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                            contentDescription = "闪光灯",
                            tint = if (flashOn) SiluEmerald else SiluTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Aspect ratio selector
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, SiluBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                val entries = AspectMode.entries
                                aspectMode = entries[(entries.indexOf(aspectMode) + 1) % entries.size]
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = aspectMode.label,
                            color = SiluTextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Center Minimalist Alignment Status Capsule with Emerald Micro-Glow
                val capsuleBorder by animateColorAsState(
                    targetValue = if (isAligned) SiluEmerald.copy(alpha = 0.45f) else SiluBorder,
                    animationSpec = tween(180),
                    label = "capsuleBorder"
                )
                val capsuleGlow = if (isAligned) SiluEmeraldGlow else Color.Transparent

                Row(
                    modifier = Modifier
                        .shadow(
                            elevation = if (isAligned) 12.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = capsuleGlow,
                            spotColor = capsuleGlow
                        )
                        .clip(CircleShape)
                        .background(if (isAligned) Color(0xCC111C17) else SiluGlass)
                        .border(1.dp, capsuleBorder, CircleShape)
                        .clickable {
                            // Tap to toggle between perfectly aligned & slight tilt demo
                            tiltDegrees = if (isAligned) 1.8f else 0.0f
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (isAligned) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = SiluEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = if (isAligned) {
                            "构图已对齐 · 角度完美"
                        } else {
                            "微调倾角 · ${String.format(Locale.US, "%.1f", abs(tiltDegrees))}°"
                        },
                        color = if (isAligned) SiluTextPrimary else SiluTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Pose Outline Visibility Toggle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (showGuide) SiluBorder else Color.Transparent, CircleShape)
                        .clickable { showGuide = !showGuide },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showGuide) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "切换构图虚线",
                        tint = if (showGuide) SiluTextPrimary else SiluTextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Full-Screen Portrait Viewfinder (Dominant Visual Center)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (abs(tiltDegrees) < 0.7f) tiltDegrees = 0.0f
                            }
                        ) { _, dragAmount ->
                            tiltDegrees = (tiltDegrees + dragAmount * 0.025f).coerceIn(-4.5f, 4.5f)
                        }
                    }
            ) {
                SiluPhotoLayer(
                    drawableRes = viewfinderDrawable,
                    customBitmap = viewfinderBitmap,
                    contentDescription = "实时取景画面",
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle top & bottom optical edge vignette for legibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x55000000), Color.Transparent)
                            )
                        )
                )

                // Central White Semi-Transparent Human Contour Dashed Overlay
                PoseOverlayCanvas(
                    poseStyle = activePose,
                    isAligned = isAligned,
                    visible = showGuide,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle Left & Right Horizon Level / Tilt Angle Rulers
                TiltRulerOverlay(
                    tiltDegrees = tiltDegrees,
                    isAligned = isAligned,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtle degree indicator on left edge of ruler
                Text(
                    text = String.format(Locale.US, "%.1f°", abs(tiltDegrees)),
                    color = if (isAligned) SiluEmerald.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.75f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 30.dp)
                )

                // Quiet active tone readout at bottom-left above dock
                Text(
                    text = "${activePose.label} · $activeTemplateTone",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 12.dp)
                )

                // Brief shutter blink overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = shutterFlash,
                    enter = fadeIn(tween(40)),
                    exit = fadeOut(tween(180))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.78f))
                    )
                }
            }

            // Bottom Frosted Glass Control Deck
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(SiluGlassStrong)
                    .border(
                        width = 1.dp,
                        color = SiluBorder,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                    )
                    .padding(top = 10.dp, bottom = 14.dp)
            ) {
                // Grab Handle -> Tapping opens Page 2 Template Drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenTemplateDrawer
                        )
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.28f))
                    )
                }

                // Horizontally Scrollable Composition Pose Mini-Cards
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Quick template drawer / reference import tile
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable(onClick = onOpenTemplateDrawer)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x55121215))
                                    .border(1.dp, SiluBorder, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = "模板库",
                                    tint = SiluTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "模板库",
                                color = SiluTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    items(PoseStyle.entries) { pose ->
                        val selected = pose == activePose
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { onSelectPose(pose) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) Color(0x99242428) else Color(0x55121215)
                                    )
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) Color.White else SiluBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                PoseSilhouetteIcon(
                                    poseStyle = pose,
                                    dashed = false,
                                    tint = if (selected) Color.White else SiluTextSecondary,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = pose.label,
                                color = if (selected) SiluTextPrimary else SiluTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Action Row: Left Import Shortcut | Center Shutter | Right Album Thumbnail
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Import Reference Image to Extract Dashed Outline
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenTemplateDrawer)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "导入样图提虚线",
                            tint = SiluTextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "导入样图",
                            color = SiluTextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    // Center: Precision Concentric Ring Shutter Button (Leica / Apple Tactile)
                    val shutterInteraction = remember { MutableInteractionSource() }
                    val isPressed by shutterInteraction.collectIsPressedAsState()
                    val shutterScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.91f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "shutterScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .scale(shutterScale)
                            .clip(CircleShape)
                            .border(
                                width = 2.5.dp,
                                color = if (isAligned) Color.White else Color.White.copy(alpha = 0.78f),
                                shape = CircleShape
                            )
                            .clickable(
                                interactionSource = shutterInteraction,
                                indication = null,
                                onClick = { shutterFlash = true }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(62.dp)) {
                            val r = size.minDimension / 2f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFEAEAEF),
                                        Color(0xFFC4C4CC),
                                        Color(0xFF9E9EA8)
                                    ),
                                    center = Offset(size.width * 0.42f, size.height * 0.38f),
                                    radius = r * 1.25f
                                ),
                                radius = r
                            )
                            drawCircle(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFDCDCE2),
                                        Color(0xFFAEAEB8),
                                        Color(0xFFE8E8EE)
                                    )
                                ),
                                radius = r * 0.42f
                            )
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.22f),
                                radius = r * 0.42f,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                            )
                        }
                    }

                    // Right: Latest Photo Album Thumbnail -> Opens AI Retouch Page
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, SiluBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onOpenAlbumRetouch)
                    ) {
                        SiluPhotoLayer(
                            drawableRes = albumThumbDrawable,
                            customBitmap = albumThumbBitmap,
                            contentDescription = "相册最新照片",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
