package com.sushi.app.util

import java.util.Calendar
import java.util.Date

/**
 * 24 节气枚举
 *
 * 节气是太阳在黄道上的位置,每年日期固定在 ±1 天内。
 * 本枚举按"小寒"→"大寒"顺序排列,作为"年内序号"基准。
 */
enum class SolarTerm(val displayName: String, val season: Season) {
    XIAO_HAN("小寒", Season.WINTER),
    DA_HAN("大寒", Season.WINTER),
    LI_CHUN("立春", Season.SPRING),
    YU_SHUI("雨水", Season.SPRING),
    JING_ZHE("惊蛰", Season.SPRING),
    CHUN_FEN("春分", Season.SPRING),
    QING_MING("清明", Season.SPRING),
    GU_YU("谷雨", Season.SPRING),
    LI_XIA("立夏", Season.SUMMER),
    XIAO_MAN("小满", Season.SUMMER),
    MANG_ZHONG("芒种", Season.SUMMER),
    XIA_ZHI("夏至", Season.SUMMER),
    XIAO_SHU("小暑", Season.SUMMER),
    DA_SHU("大暑", Season.SUMMER),
    LI_QIU("立秋", Season.AUTUMN),
    CHU_SHU("处暑", Season.AUTUMN),
    BAI_LU("白露", Season.AUTUMN),
    QIU_FEN("秋分", Season.AUTUMN),
    HAN_LU("寒露", Season.AUTUMN),
    SHUANG_JIANG("霜降", Season.AUTUMN),
    LI_DONG("立冬", Season.WINTER),
    XIAO_XUE("小雪", Season.WINTER),
    DA_XUE("大雪", Season.WINTER),
    DONG_ZHI("冬至", Season.WINTER);

    val index: Int get() = ordinal  // 0-23

    companion object {
        /**
         * 获取指定日期的节气(若有)
         * 2025-2050 范围,日期 ±1 天容差
         */
        fun atDate(date: Date): SolarTerm? {
            val cal = Calendar.getInstance().apply { time = date }
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val year = cal.get(Calendar.YEAR)
            if (year < 2025 || year > 2050) return null

            // 24 节气近似日期(公历)
            val monthDay = month * 100 + day
            return when (monthDay) {
                in 101..106 -> XIAO_HAN      // 1/5-1/6 前后
                in 120..121 -> DA_HAN        // 1/20 前后
                in 203..205 -> LI_CHUN
                in 218..220 -> YU_SHUI
                in 305..307 -> JING_ZHE
                in 320..322 -> CHUN_FEN
                in 404..406 -> QING_MING
                in 419..421 -> GU_YU
                in 505..507 -> LI_XIA
                in 520..522 -> XIAO_MAN
                in 605..607 -> MANG_ZHONG
                in 621..622 -> XIA_ZHI
                in 706..708 -> XIAO_SHU
                in 722..724 -> DA_SHU
                in 807..809 -> LI_QIU
                in 822..824 -> CHU_SHU
                in 907..910 -> BAI_LU
                in 922..924 -> QIU_FEN
                in 1008..1010 -> HAN_LU
                in 1023..1025 -> SHUANG_JIANG
                in 1107..1109 -> LI_DONG
                in 1122..1123 -> XIAO_XUE
                in 1206..1208 -> DA_XUE
                in 1221..1223 -> DONG_ZHI
                else -> null
            }
        }

        /**
         * 获取指定日期的下一个节气
         */
        fun nextTerm(date: Date): SolarTerm {
            val cal = Calendar.getInstance().apply { time = date }
            val current = atDate(date)
            return current?.nextOrFirst() ?: XIAO_HAN
        }
    }
}

/** 季节 */
enum class Season(val displayName: String) {
    SPRING("春"),
    SUMMER("夏"),
    AUTUMN("秋"),
    WINTER("冬");
}

/** 枚举循环辅助 */
private fun SolarTerm.nextOrFirst(): SolarTerm {
    val all = SolarTerm.values()
    return all[(this.index + 1) % all.size]
}
