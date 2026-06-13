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

            // 24 节气近似日期(公历),按从小寒开始排列的 month*100+day
            return when (month * 100 + day) {
                in 101..106 -> XIAO_HAN
                in 120..121 -> DA_HAN
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
         * 获取指定日期之后(或当天)的下一个节气。
         *
         * 算法:
         * 1. 如果当前日期精确匹配某个节气,返回它的下一个;
         * 2. 否则遍历所有节气的近似日期(从小寒到大雪),返回第一个
         *    monthDay 大于当前 monthDay 的节气;
         * 3. 跨年:如果当前 monthDay 已晚于 DONG_ZHI(1221~1223),
         *    则下一年的第一个节气是 XIAO_HAN。
         */
        fun nextTerm(date: Date): SolarTerm {
            val current = atDate(date)
            if (current != null) {
                return current.nextOrFirst()
            }

            val cal = Calendar.getInstance().apply { time = date }
            val monthDay = (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH)

            // 按顺序遍历所有 24 节气的"中间日"(中值),找第一个 > 当前 monthDay 的
            val termCenters = ORDERED_TERM_CENTERS
            for ((term, center) in termCenters) {
                if (center > monthDay) return term
            }
            // 已过冬至,下一节气是下一年小寒
            return XIAO_HAN
        }

        /**
         * 各节气在"monthDay"中的中间日(近似日期),按节气顺序。
         * 用于在没有精确匹配时,计算下一个节气。
         */
        private val ORDERED_TERM_CENTERS: List<Pair<SolarTerm, Int>> = listOf(
            XIAO_HAN to 105,       // 1/5
            DA_HAN to 120,         // 1/20
            LI_CHUN to 204,        // 2/4
            YU_SHUI to 219,        // 2/19
            JING_ZHE to 306,       // 3/6
            CHUN_FEN to 321,       // 3/21
            QING_MING to 405,      // 4/5
            GU_YU to 420,          // 4/20
            LI_XIA to 506,         // 5/6
            XIAO_MAN to 521,       // 5/21
            MANG_ZHONG to 606,     // 6/6
            XIA_ZHI to 621,        // 6/21
            XIAO_SHU to 707,       // 7/7
            DA_SHU to 723,         // 7/23
            LI_QIU to 808,         // 8/8
            CHU_SHU to 823,        // 8/23
            BAI_LU to 908,         // 9/8
            QIU_FEN to 923,        // 9/23
            HAN_LU to 1009,        // 10/9
            SHUANG_JIANG to 1024,  // 10/24
            LI_DONG to 1108,       // 11/8
            XIAO_XUE to 1122,      // 11/22
            DA_XUE to 1207,        // 12/7
            DONG_ZHI to 1222       // 12/22
        )
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
