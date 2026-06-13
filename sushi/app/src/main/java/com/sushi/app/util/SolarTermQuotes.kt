package com.sushi.app.util

import java.util.Calendar
import java.util.Date

/**
 * 24 节气古语库
 *
 * 预置 24 节气各 1-2 句经典古诗词/古语。
 * 启动时按节气随机显示。
 *
 * 来源: 24 节气经典诗词,公共领域。
 */
object SolarTermQuotes {

    private val QUOTES: Map<SolarTerm, List<String>> = mapOf(
        SolarTerm.LI_CHUN to listOf(
            "东风带雨逐西风,大地阳和暖气生。",
            "律回岁晚冰霜少,春到人间草木知。"
        ),
        SolarTerm.YU_SHUI to listOf(
            "好雨知时节,当春乃发生。",
            "天街小雨润如酥,草色遥看近却无。"
        ),
        SolarTerm.JING_ZHE to listOf(
            "微雨众卉新,一雷惊蛰始。",
            "田家几日闲,耕种从此起。"
        ),
        SolarTerm.CHUN_FEN to listOf(
            "风和日丽万花艳,蝶舞蜂飞绿草萋。",
            "雪入春分省见稀,半开桃李不胜威。"
        ),
        SolarTerm.QING_MING to listOf(
            "清明时节雨纷纷,路上行人欲断魂。",
            "清明暮春里,怅望北山陲。"
        ),
        SolarTerm.GU_YU to listOf(
            "谷雨春光晓,山川黛色青。",
            "邵平瓜地接吾庐,谷雨初晴叫采菽。"
        ),
        SolarTerm.LI_XIA to listOf(
            "绿树阴浓夏日长,楼台倒影入池塘。",
            "泥新巢燕闹,花尽蜜蜂稀。"
        ),
        SolarTerm.XIAO_MAN to listOf(
            "夜莺啼绿柳,皓月醒长空。",
            "小满麦渐黄,东风桃李香。"
        ),
        SolarTerm.MANG_ZHONG to listOf(
            "时雨及芒种,四野皆插秧。",
            "家家麦饭美,处处菱歌长。"
        ),
        SolarTerm.XIA_ZHI to listOf(
            "昼晷已云极,宵漏自此长。",
            "绿筠尚含粉,圆荷始散芳。"
        ),
        SolarTerm.XIAO_SHU to listOf(
            "倏忽温风至,因循小暑来。",
            "竹喧先觉雨,山暗已闻雷。"
        ),
        SolarTerm.DA_SHU to listOf(
            "赤日几时过,清风无处寻。",
            "大暑三秋近,林钟九夏移。"
        ),
        SolarTerm.LI_QIU to listOf(
            "自古逢秋悲寂寥,我言秋日胜春朝。",
            "云天收夏色,木叶动秋声。"
        ),
        SolarTerm.CHU_SHU to listOf(
            "离离暑云散,袅袅凉风起。",
            "处暑无三日,新凉直万金。"
        ),
        SolarTerm.BAI_LU to listOf(
            "蒹葭苍苍,白露为霜。所谓伊人,在水一方。",
            "白露横江水,秋风肃客衣。"
        ),
        SolarTerm.QIU_FEN to listOf(
            "金气秋分,风清露冷秋期半。",
            "燕将明日去,秋向此时分。"
        ),
        SolarTerm.HAN_LU to listOf(
            "袅袅凉风动,凄凄寒露零。",
            "寒露惊秋晚,朝看菊渐黄。"
        ),
        SolarTerm.SHUANG_JIANG to listOf(
            "霜降水返壑,风落木归山。",
            "泊舟淮水次,霜降夕流清。"
        ),
        SolarTerm.LI_DONG to listOf(
            "细雨生寒未有霜,庭前木叶半青黄。",
            "秋风吹尽旧庭柯,黄叶丹枫客里过。"
        ),
        SolarTerm.XIAO_XUE to listOf(
            "莫怪虹无影,如今小雪时。",
            "片片互玲珑,飞扬玉漏终。"
        ),
        SolarTerm.DA_XUE to listOf(
            "忽如一夜春风来,千树万树梨花开。",
            "大雪江南见未曾,今年方始是严凝。"
        ),
        SolarTerm.DONG_ZHI to listOf(
            "冬至大如年,人间小团圆。",
            "邯郸驿里逢冬至,抱膝灯前影伴身。"
        ),
        SolarTerm.XIAO_HAN to listOf(
            "小寒大寒,准备过年。",
            "岸容待腊将舒柳,山意冲寒欲放梅。"
        ),
        SolarTerm.DA_HAN to listOf(
            "旧雪未及消,新雪又拥户。",
            "大寒凛冽砭肌骨,一夜梅花傲雪开。"
        )
    )

    /**
     * 获取当前节气的随机古语
     */
    fun randomForToday(today: Date = Date()): String? {
        val term = SolarTerm.atDate(today)
        if (term == null) return null
        val list = QUOTES[term] ?: return null
        // 简单的"基于日期的随机" — 同一天始终返回同一句
        val cal = Calendar.getInstance().apply { time = today }
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % list.size
        return list[index]
    }

    /**
     * 获取指定节气的所有古语
     */
    fun quotesFor(term: SolarTerm): List<String> = QUOTES[term] ?: emptyList()
}
