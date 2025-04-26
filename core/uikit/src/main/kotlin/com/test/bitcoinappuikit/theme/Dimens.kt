package com.test.bitcoinappuikit.theme

val DEFAULT_APP_DIMENS = AppDimensEnum.MEDIUM

enum class AppDimensEnum(val type: Int) {
    SMALL(0), MEDIUM(1), LARGE(2);

    fun getDimensClass(): DimensImpl {
        return when (this) {
            SMALL -> smallDimens
            MEDIUM -> mediumDimens
            LARGE -> largeDimens
        }
    }

    companion object {
        fun fromInt(value1: Int): AppDimensEnum {
            val value = values().firstOrNull { it.type == value1 }
            return value ?: DEFAULT_APP_DIMENS
        }

        fun fromString(value1: String): AppDimensEnum {
            val intValue = value1.toIntOrNull() ?: DEFAULT_APP_DIMENS.type
            return fromInt(intValue)
        }
    }
}

class DimensImpl(
    val textH1Size: Int,
    val textH2Size: Int,
    val textH3Size: Int,
    val textH4Size: Int,
    val textH5Size: Int,
    val textH6Size: Int,
    val textSubTitle1Size: Int,
    val textSubTitle2Size: Int,
    val textBody1Size: Int,
    val textBody2Size: Int,
    val textButtonSize: Int,
    val textCaptionSize: Int,
    val textOverlineSize: Int,

    val marginXXTinySize: Int,
    val marginXTinySize: Int,
    val marginTinySize: Int,
    val marginSmallSize: Int,
    val marginMediumSize: Int,
    val marginLargeSize: Int,
    val marginXLargeSize: Int,
    val marginXXLargeSize: Int,

    val iconXXTinySize: Int,
    val iconXTinySize: Int,
    val iconTinySize: Int,
    val iconSmallSize: Int,
    val iconMediumSize: Int,
    val iconLargeSize: Int,
    val iconXLargeSize: Int,
    val iconXXLargeSize: Int,

    val progressSmallSize: Int,
    val progressMediumSize: Int,
    val progressLargeSize: Int,
    val progressStrokeWidth: Int,

    val cardBorderStrokeWidth: Int,
    val cardCornerRadius: Int,

    val scrollbarThickness: Int,
    val scrollbarHeight: Int,

    val dividerThickness: Int,

    val statusBarHeight: Int,

    val buttonVerticalPadding: Int,
    val buttonHorizontalPadding: Int,

    val toolbarHeight: Int,
    val toolbarSmallHeight: Int,
) {

    companion object {
        const val TEXT_SIZE_TINY_COEF = 0.8f
        const val TEXT_SIZE_LARGE_COEF = 1.2f
    }
}

val smallDimens = DimensImpl(
    textH1Size = 76,
    textH2Size = 48,
    textH3Size = 38,
    textH4Size = 28,
    textH5Size = 24,
    textH6Size = 19,
    textSubTitle1Size = 19,
    textSubTitle2Size = 16,
    textBody1Size = 14,
    textBody2Size = 11,
    textButtonSize = 14,
    textCaptionSize = 13,
    textOverlineSize = 11,

    marginXXTinySize = 1,
    marginXTinySize = 2,
    marginTinySize = 4,
    marginSmallSize = 6,
    marginMediumSize = 9,
    marginLargeSize = 12,
    marginXLargeSize = 18,
    marginXXLargeSize = 24,

    iconXXTinySize = 4,
    iconXTinySize = 8,
    iconTinySize = 12,
    iconSmallSize = 18,
    iconMediumSize = 24,
    iconLargeSize = 36,
    iconXLargeSize = 48,
    iconXXLargeSize = 84,

    progressSmallSize = 24,
    progressMediumSize = 36,
    progressLargeSize = 48,
    progressStrokeWidth = 2,

    cardBorderStrokeWidth = 2,
    cardCornerRadius = 4,

    scrollbarThickness = 8,
    scrollbarHeight = 4,

    dividerThickness = 1,

    statusBarHeight = 24,


    buttonVerticalPadding = 4,
    buttonHorizontalPadding = 8,

    toolbarHeight = 42,
    toolbarSmallHeight = 26
)

val mediumDimens = DimensImpl(
    textH1Size = 96,
    textH2Size = 60,
    textH3Size = 48,
    textH4Size = 36,
    textH5Size = 24,
    textH6Size = 20,
    textSubTitle1Size = 24,
    textSubTitle2Size = 20,
    textBody1Size = 18,
    textBody2Size = 14,
    textButtonSize = 18,
    textCaptionSize = 16,
    textOverlineSize = 14,

    marginXXTinySize = 2,
    marginXTinySize = 4,
    marginTinySize = 6,
    marginSmallSize = 8,
    marginMediumSize = 12,
    marginLargeSize = 20,
    marginXLargeSize = 36,
    marginXXLargeSize = 48,

    iconXXTinySize = 8,
    iconXTinySize = 12,
    iconTinySize = 16,
    iconSmallSize = 24,
    iconMediumSize = 30,
    iconLargeSize = 44,
    iconXLargeSize = 64,
    iconXXLargeSize = 100,

    progressSmallSize = 32,
    progressMediumSize = 48,
    progressLargeSize = 64,
    progressStrokeWidth = 2,

    cardBorderStrokeWidth = 2,
    cardCornerRadius = 4,

    scrollbarThickness = 8,
    scrollbarHeight = 4,

    dividerThickness = 1,

    buttonVerticalPadding = 8,
    buttonHorizontalPadding = 12,

    statusBarHeight = 24,

    toolbarHeight = 60,
    toolbarSmallHeight = 32
)

val largeDimens = DimensImpl(
    textH1Size = 115,
    textH2Size = 72,
    textH3Size = 57,
    textH4Size = 43,
    textH5Size = 28,
    textH6Size = 24,
    textSubTitle1Size = 28,
    textSubTitle2Size = 24,
    textBody1Size = 21,
    textBody2Size = 17,
    textButtonSize = 21,
    textCaptionSize = 19,
    textOverlineSize = 17,

    marginXXTinySize = 2,
    marginXTinySize = 4,
    marginTinySize = 6,
    marginSmallSize = 8,
    marginMediumSize = 12,
    marginLargeSize = 20,
    marginXLargeSize = 36,
    marginXXLargeSize = 48,

    iconXXTinySize = 12,
    iconXTinySize = 16,
    iconTinySize = 20,
    iconSmallSize = 28,
    iconMediumSize = 36,
    iconLargeSize = 54,
    iconXLargeSize = 72,
    iconXXLargeSize = 116,

    progressSmallSize = 40,
    progressMediumSize = 54,
    progressLargeSize = 72,
    progressStrokeWidth = 2,

    cardBorderStrokeWidth = 2,
    cardCornerRadius = 4,

    scrollbarThickness = 10,
    scrollbarHeight = 4,

    dividerThickness = 1,

    buttonVerticalPadding = 8,
    buttonHorizontalPadding = 12,

    statusBarHeight = 24,

    toolbarHeight = 60,
    toolbarSmallHeight = 40
)
