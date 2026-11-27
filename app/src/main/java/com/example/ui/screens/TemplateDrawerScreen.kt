package com.example.ui.screens

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompositionTemplate
import com.example.model.TemplateCategory
import com.example.ui.components.PoseSilhouetteIcon
import com.example.ui.components.SiluPhotoLayer
import com.example.ui.theme.SiluBlack
import com.example.ui.theme.SiluBorder
import com.example.ui.theme.SiluEmerald
import com.example.ui.theme.SiluGlassStrong
import com.example.ui.theme.SiluTextPrimary
import com.example.ui.theme.SiluTextSecondary

@Composable
fun TemplateDrawerScreen(
    @DrawableRes backdropDrawable: Int?,
    backdropBitmap: Bitmap?,
    templates: List<CompositionTemplate>,
    selectedTemplateId: String,
    onSelectTemplate: (CompositionTemplate) -> Unit,
    onImportCustomReference: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeCategory by remember { mutableStateOf(TemplateCategory.RECOMMENDED) }

    val filteredTemplates = remember(templates, activeCategory) {
        if (activeCategory == TemplateCategory.RECOMMENDED) {
            templates
        } else {
            templates.filter { it.category == activeCategory }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SiluBlack)
    ) {
        // Blurred Viewfinder Backdrop Peeking at Top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            SiluPhotoLayer(
                drawableRes = backdropDrawable,
                customBitmap = backdropBitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(22.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x990A0A0C))
            )
            // Subtle viewfinder corner marks peeking behind drawer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "构图模板库",
                    color = SiluTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "返回取景",
                    color = SiluTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Translucent Frosted Bottom Sheet Panel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 112.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(SiluGlassStrong)
                .border(
                    width = 1.dp,
                    color = SiluBorder,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
        ) {
            // Drag handle bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .padding(top = 10.dp, bottom = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.28f))
                )
            }

            // Top Highlight Banner Card: Import any photo to auto-extract dashed outline & tone
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC222226))
                    .border(
                        width = 1.dp,
                        color = SiluEmerald.copy(alpha = 0.30f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(onClick = onImportCustomReference)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom line-art Photo -> Dashed Frame extraction glyph
                ExtractOutlineGlyph(modifier = Modifier.size(width = 44.dp, height = 30.dp))

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "导入任意美图",
                        color = SiluTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "自动提取构图虚线与同款色调",
                        color = SiluTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "选择照片",
                    tint = SiluTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Filter Tabs (Clean Typography + Thin Active Underline, Zero Pill Boxes)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TemplateCategory.entries) { category ->
                    val isSelected = category == activeCategory
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { activeCategory = category }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = category.label,
                            color = if (isSelected) SiluTextPrimary else SiluTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(2.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SiluTextPrimary else Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2-Column Photography Sample Template Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredTemplates, key = { it.id }) { template ->
                    val isSelected = template.id == selectedTemplateId
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTemplate(template) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                    color = if (isSelected) SiluEmerald else SiluBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            SiluPhotoLayer(
                                drawableRes = template.drawableRes,
                                customBitmap = template.customBitmap,
                                contentDescription = template.title,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Top-right Dashed Silhouette Thumbnail Badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x99121215))
                                    .border(0.8.dp, SiluBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                PoseSilhouetteIcon(
                                    poseStyle = template.poseStyle,
                                    dashed = true,
                                    tint = if (isSelected) SiluEmerald else Color.White.copy(alpha = 0.92f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Active selection check mark at bottom-right
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(SiluEmerald),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "已选中",
                                        tint = SiluBlack,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(7.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = template.title,
                                color = SiluTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = template.usageText,
                                color = SiluTextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtractOutlineGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeSolid = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
        val strokeDash = Stroke(
            width = 1.4.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.5.dp.toPx()))
        )

        // Left solid photo frame
        drawRoundRect(
            color = Color(0xFFD4D4D8),
            topLeft = Offset(1.dp.toPx(), 3.dp.toPx()),
            size = Size(w * 0.44f, h - 6.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = strokeSolid
        )
        // Small sun dot inside left photo
        drawCircle(
            color = Color(0xFFD4D4D8),
            radius = 2.dp.toPx(),
            center = Offset(w * 0.16f, h * 0.38f)
        )

        // Right dashed silhouette target frame
        drawRoundRect(
            color = SiluEmerald.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.54f, 3.dp.toPx()),
            size = Size(w * 0.44f, h - 6.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = strokeDash
        )

        // Small extraction arrow in center
        drawLine(
            color = SiluEmerald,
            start = Offset(w * 0.41f, h * 0.50f),
            end = Offset(w * 0.62f, h * 0.50f),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
