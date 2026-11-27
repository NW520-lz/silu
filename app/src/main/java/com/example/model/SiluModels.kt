package com.example.model

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import com.example.R

enum class SiluPage(val label: String) {
    CAMERA("实时构图"),
    TEMPLATES("构图模板"),
    RETOUCH("AI 精修"),
    COMPARE("对比保存")
}

enum class AspectMode(val label: String, val ratio: Float) {
    RATIO_3_4("3:4", 3f / 4f),
    RATIO_4_5("4:5", 4f / 5f),
    RATIO_9_16("9:16", 9f / 16f)
}

enum class PoseStyle(val id: String, val label: String) {
    STREET_SIDE("street_side", "街角侧身"),
    CAFE_DAILY("cafe_daily", "咖啡日常"),
    STREET_WALK("street_walk", "街头漫步"),
    SEASIDE_BACK("seaside_back", "海边逆光"),
    TALL_FULL("tall_full", "显高全身")
}

enum class TemplateCategory(val label: String) {
    RECOMMENDED("推荐"),
    STREET("街拍"),
    CAFE("咖啡馆"),
    SEASIDE("海边"),
    TALL_FULLBODY("显高全身")
}

data class CompositionTemplate(
    val id: String,
    val title: String,
    val category: TemplateCategory,
    val poseStyle: PoseStyle,
    val usageText: String,
    val toneDescription: String,
    @DrawableRes val drawableRes: Int? = null,
    val customBitmap: Bitmap? = null
)

enum class RetouchIntent(val label: String, val detail: String) {
    REMOVE_PASSERSBY("无痕去路人", "智能净化背景干扰人物，保留真实街景景深"),
    NATURAL_SKIN("自然皮肤保真", "保留毛孔与自然肌理，仅柔化局部倦态光影"),
    WARM_BACKLIGHT("重塑暖阳逆光", "模拟黄金时刻发丝轮廓光与胶片暖调"),
    SMOOTH_CLOTHES("衣服去褶皱", "抚平外套肩袖杂乱折痕，增强面料垂坠感")
}

data class RetouchHistoryStep(
    val id: String,
    val title: String,
    val timestamp: String,
    val activeIntents: Set<RetouchIntent>,
    val customNote: String? = null
)

object SiluCatalog {
    val defaultTemplates: List<CompositionTemplate> = listOf(
        CompositionTemplate(
            id = "tpl_cafe_daily",
            title = "咖啡日常 · 抬手遮阳",
            category = TemplateCategory.CAFE,
            poseStyle = PoseStyle.CAFE_DAILY,
            usageText = "2.4w 人使用",
            toneDescription = "徕卡暖棕胶片",
            drawableRes = R.drawable.camera_cafe_portrait
        ),
        CompositionTemplate(
            id = "tpl_cafe_afternoon",
            title = "午后咖啡馆",
            category = TemplateCategory.CAFE,
            poseStyle = PoseStyle.STREET_SIDE,
            usageText = "1.8w 人使用",
            toneDescription = "窗边自然柔光",
            drawableRes = R.drawable.tpl_cafe_afternoon
        ),
        CompositionTemplate(
            id = "tpl_street_walk",
            title = "街头漫游",
            category = TemplateCategory.STREET,
            poseStyle = PoseStyle.STREET_WALK,
            usageText = "3.1w 人使用",
            toneDescription = "低饱和胶片街拍",
            drawableRes = R.drawable.tpl_street_walk
        ),
        CompositionTemplate(
            id = "tpl_seaside_sunset",
            title = "日落余晖",
            category = TemplateCategory.SEASIDE,
            poseStyle = PoseStyle.SEASIDE_BACK,
            usageText = "2.9w 人使用",
            toneDescription = "落日琥珀逆光",
            drawableRes = R.drawable.tpl_seaside_sunset
        ),
        CompositionTemplate(
            id = "tpl_tall_arch",
            title = "建筑显高全身",
            category = TemplateCategory.TALL_FULLBODY,
            poseStyle = PoseStyle.TALL_FULL,
            usageText = "1.5w 人使用",
            toneDescription = "极简冷暖光影",
            drawableRes = R.drawable.tpl_tall_fullbody
        ),
        CompositionTemplate(
            id = "tpl_golden_turn",
            title = "街角回眸逆光",
            category = TemplateCategory.STREET,
            poseStyle = PoseStyle.STREET_SIDE,
            usageText = "2.1w 人使用",
            toneDescription = "黄金时刻轮廓光",
            drawableRes = R.drawable.retouch_hero_portrait
        )
    )
}
