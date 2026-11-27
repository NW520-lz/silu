package com.example

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompositionTemplate
import com.example.model.PoseStyle
import com.example.model.RetouchHistoryStep
import com.example.model.RetouchIntent
import com.example.model.SiluCatalog
import com.example.model.SiluPage
import com.example.model.TemplateCategory
import com.example.ui.components.decodeSampledBitmapFromUri
import com.example.ui.screens.AiRetouchScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.CompareSaveScreen
import com.example.ui.screens.TemplateDrawerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SiluBlack
import com.example.ui.theme.SiluBorderSubtle
import com.example.ui.theme.SiluEmerald
import com.example.ui.theme.SiluTextPrimary
import com.example.ui.theme.SiluTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            MyApplicationTheme {
                SiluAppRoot()
            }
        }
    }
}

@Composable
fun SiluAppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentPage by remember { mutableStateOf(SiluPage.CAMERA) }
    var templates by remember { mutableStateOf(SiluCatalog.defaultTemplates) }
    var selectedTemplate by remember { mutableStateOf(SiluCatalog.defaultTemplates.first()) }
    var activePose by remember { mutableStateOf(PoseStyle.CAFE_DAILY) }

    // Retouching workspace state (defaults to golden-hour editorial street portrait)
    var retouchDrawable by remember { mutableStateOf<Int?>(R.drawable.retouch_hero_portrait) }
    var retouchBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeIntents by remember {
        mutableStateOf(
            setOf(
                RetouchIntent.REMOVE_PASSERSBY,
                RetouchIntent.NATURAL_SKIN,
                RetouchIntent.WARM_BACKLIGHT
            )
        )
    }
    var faceLockEnabled by remember { mutableStateOf(true) }
    var promptText by remember { mutableStateOf("") }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    var historySteps by remember {
        mutableStateOf(
            listOf(
                RetouchHistoryStep(
                    id = "init_1",
                    title = "按模板一键精修 · 暖阳逆光",
                    timestamp = "14:32",
                    activeIntents = setOf(
                        RetouchIntent.REMOVE_PASSERSBY,
                        RetouchIntent.NATURAL_SKIN,
                        RetouchIntent.WARM_BACKLIGHT
                    ),
                    customNote = "五官原貌锁定 · 100% 肌理保真"
                ),
                RetouchHistoryStep(
                    id = "init_0",
                    title = "相机直出原图",
                    timestamp = "14:31",
                    activeIntents = emptySet(),
                    customNote = "Leica 35mm · f/1.4"
                )
            )
        )
    }

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            delay(2200)
            statusBanner = null
        }
    }

    // Native Android System Photo Picker to import any reference image & extract dashed outline
    val pickReferencePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    decodeSampledBitmapFromUri(context, uri)
                }
                if (bitmap != null) {
                    val customTemplate = CompositionTemplate(
                        id = "custom_${System.currentTimeMillis()}",
                        title = "自选样图 · 虚线提取",
                        category = TemplateCategory.RECOMMENDED,
                        poseStyle = PoseStyle.STREET_SIDE,
                        usageText = "刚刚导入",
                        toneDescription = "已提取同款色调",
                        drawableRes = null,
                        customBitmap = bitmap
                    )
                    templates = listOf(customTemplate) + templates
                    selectedTemplate = customTemplate
                    activePose = customTemplate.poseStyle
                    currentPage = SiluPage.CAMERA
                    statusBanner = "已自动提取人物构图虚线与同款色调"
                }
            }
        }
    }

    fun recordHistory(title: String, intents: Set<RetouchIntent>, note: String? = null) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val step = RetouchHistoryStep(
            id = "step_${System.currentTimeMillis()}",
            title = title,
            timestamp = time,
            activeIntents = intents,
            customNote = note
        )
        historySteps = listOf(step) + historySteps.take(6)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SiluBlack)
    ) {
        // Main Active Page Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    if (targetState == SiluPage.TEMPLATES) {
                        (slideInVertically(tween(240)) { it / 5 } + fadeIn(tween(200)))
                            .togetherWith(fadeOut(tween(160)))
                    } else {
                        fadeIn(tween(200)).togetherWith(fadeOut(tween(160)))
                    }
                },
                label = "siluPageTransition"
            ) { page ->
                when (page) {
                    SiluPage.CAMERA -> CameraScreen(
                        viewfinderDrawable = selectedTemplate.drawableRes,
                        viewfinderBitmap = selectedTemplate.customBitmap,
                        albumThumbDrawable = retouchDrawable,
                        albumThumbBitmap = retouchBitmap,
                        activePose = activePose,
                        activeTemplateTone = selectedTemplate.toneDescription,
                        onSelectPose = { pose ->
                            activePose = pose
                            templates.firstOrNull { it.poseStyle == pose }?.let { matched ->
                                selectedTemplate = matched
                            }
                        },
                        onOpenTemplateDrawer = { currentPage = SiluPage.TEMPLATES },
                        onCapturePhoto = {
                            retouchDrawable = selectedTemplate.drawableRes
                            retouchBitmap = selectedTemplate.customBitmap
                            currentPage = SiluPage.RETOUCH
                        },
                        onOpenAlbumRetouch = {
                            currentPage = SiluPage.RETOUCH
                        }
                    )

                    SiluPage.TEMPLATES -> TemplateDrawerScreen(
                        backdropDrawable = selectedTemplate.drawableRes,
                        backdropBitmap = selectedTemplate.customBitmap,
                        templates = templates,
                        selectedTemplateId = selectedTemplate.id,
                        onSelectTemplate = { template ->
                            selectedTemplate = template
                            activePose = template.poseStyle
                            currentPage = SiluPage.CAMERA
                            statusBanner = "已应用「${template.title}」构图虚线"
                        },
                        onImportCustomReference = {
                            pickReferencePhoto.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onDismiss = { currentPage = SiluPage.CAMERA }
                    )

                    SiluPage.RETOUCH -> AiRetouchScreen(
                        photoDrawable = retouchDrawable,
                        photoBitmap = retouchBitmap,
                        activeIntents = activeIntents,
                        faceLockEnabled = faceLockEnabled,
                        promptText = promptText,
                        onPromptChange = { promptText = it },
                        onToggleIntent = { intent ->
                            activeIntents = if (intent in activeIntents) {
                                activeIntents - intent
                            } else {
                                activeIntents + intent
                            }
                        },
                        onOneTapTemplateRetouch = {
                            val enhanced = activeIntents.ifEmpty {
                                setOf(
                                    RetouchIntent.REMOVE_PASSERSBY,
                                    RetouchIntent.NATURAL_SKIN,
                                    RetouchIntent.WARM_BACKLIGHT
                                )
                            }
                            activeIntents = enhanced
                            recordHistory(
                                title = "按构图模板一键精修",
                                intents = enhanced,
                                note = "${selectedTemplate.toneDescription} · 五官锁定"
                            )
                            currentPage = SiluPage.COMPARE
                        },
                        onSubmitCustomPrompt = { custom ->
                            val updated = buildSet {
                                addAll(activeIntents)
                                if (custom.contains("路人")) add(RetouchIntent.REMOVE_PASSERSBY)
                                if (custom.contains("脸") || custom.contains("皮肤") || custom.contains("真实")) {
                                    add(RetouchIntent.NATURAL_SKIN)
                                }
                                if (custom.contains("暖") || custom.contains("光") || custom.contains("阳")) {
                                    add(RetouchIntent.WARM_BACKLIGHT)
                                }
                                if (custom.contains("衣") || custom.contains("褶")) {
                                    add(RetouchIntent.SMOOTH_CLOTHES)
                                }
                            }.ifEmpty { setOf(RetouchIntent.NATURAL_SKIN, RetouchIntent.WARM_BACKLIGHT) }
                            activeIntents = updated
                            recordHistory(
                                title = "对话微调精修",
                                intents = updated,
                                note = custom
                            )
                            promptText = ""
                            currentPage = SiluPage.COMPARE
                        },
                        onBackToCamera = { currentPage = SiluPage.CAMERA }
                    )

                    SiluPage.COMPARE -> CompareSaveScreen(
                        photoDrawable = retouchDrawable,
                        photoBitmap = retouchBitmap,
                        activeIntents = activeIntents,
                        faceLockEnabled = faceLockEnabled,
                        historySteps = historySteps,
                        onToggleFaceLock = { faceLockEnabled = it },
                        onRestoreHistoryStep = { step ->
                            activeIntents = step.activeIntents
                        },
                        onContinueRetouch = { currentPage = SiluPage.RETOUCH }
                    )
                }
            }

            // Quiet Top Floating Notification Toast (for template extraction / selection)
            androidx.compose.animation.AnimatedVisibility(
                visible = statusBanner != null,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 56.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xE60E1C16))
                        .border(1.dp, SiluEmerald.copy(alpha = 0.50f), CircleShape)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = statusBanner.orEmpty(),
                        color = SiluEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Ultra-Restrained Leica Mode Strip (Direct 1-tap access to all 4 core screens)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SiluBlack)
                .border(0.5.dp, SiluBorderSubtle)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SiluPage.entries.forEach { page ->
                val isSelected = page == currentPage
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { currentPage = page }
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = page.label,
                        color = if (isSelected) SiluTextPrimary else SiluTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) SiluEmerald else Color.Transparent)
                    )
                }
            }
        }
    }
}
