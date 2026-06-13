# 素时 · 开发计划(V1.0)

> **目标**: 用 4 周时间,从现有 31 项功能工程中**精简重构**,交付一个 8MB 以内、完全离线、纯公益的素时 V1.0 APK。
> **撰写日期**: 2026-06-13

---

## 目录

1. [总体策略](#1-总体策略)
2. [技术栈精简](#2-技术栈精简)
3. [架构设计](#3-架构设计)
4. [数据模型](#4-数据模型)
5. [代码改造清单](#5-代码改造清单)
6. [阶段划分与里程碑](#6-阶段划分与里程碑)
7. [任务清单(周维度)](#7-任务清单周维度)
8. [质量保障](#8-质量保障)
9. [发布计划](#9-发布计划)
10. [长期维护](#10-长期维护)

---

## 1. 总体策略

### 1.1 设计原则
- **减法优先**: 砍掉所有非核心功能,直到无法再减
- **离线为王**: 不引入任何网络依赖(SDK、库、字体网络下载)
- **公益级稳定**: 一次构建,运行 10 年不崩
- **极致性能**: APK < 8MB,启动 < 500ms,内存 < 100MB

### 1.2 重构思路
**不重写,而是瘦身**。当前工程已有 31 项功能实现,本次任务:
1. **删**: 砍掉云同步、AI、游戏化、社交、推送相关代码
2. **改**: 视觉/文案按宋式极简重做
3. **留**: 数据层、图表组件、基础 UI 组件保留核心
4. **补**: 补 24 节气、农历、备份/恢复 3 个核心功能

### 1.3 关键决策
| 决策项 | 选择 | 理由 |
|---|---|---|
| DI 框架 | **不用 Hilt,改用顶层单例** | 减依赖、减方法数、零反射 |
| 网络库 | **完全移除** | 不联网 |
| 数据库 | **Room 保留** | 稳定、本地、无网络 |
| UI 框架 | **Compose 保留** | 主流、声明式、Jetpack 支持 |
| 主题 | **Material 3 减配** | 只用其底层组件,不用动态色 |
| 协程 | **保留** | 必要 |
| Flow | **保留** | 必要 |
| 序列化 | **Gson → Moshi 或 kotlinx.serialization** | 更小更快 |
| 字体 | **思源宋体/黑体/楷体内置子集** | 离线可用 |
| 图标 | **Lucide Icons 内置** | 离线、无版权 |
| 备份 | **本地文件 + SAF** | 离线、无网络 |

---

## 2. 技术栈精简

### 2.1 移除的依赖
```kotlin
// 以下依赖全部移除
- hilt-android / hilt-compiler
- okhttp / retrofit / gson(网络相关)
- datastore-preferences(改用 SharedPreferences)
- navigation-compose(改用单 Activity + 自管理状态)
- material3 dynamic color
- 所有第三方统计/崩溃 SDK
```

### 2.2 保留的依赖(最小化)
```kotlin
// app/build.gradle.kts
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### 2.3 目标 APK 大小
- 基础: ~3MB
- 字体子集(思源宋/黑/楷体各 1MB): +3MB
- 图标(Lucide 全套): +1MB
- **总计: ~7MB** ✅

---

## 3. 架构设计

### 3.1 总体架构
**经典 MVVM + 单例 Repository**

```
┌─────────────────────────────┐
│  UI (Compose Screens)       │
└──────────────┬──────────────┘
               │ collectAsState
┌──────────────▼──────────────┐
│  ViewModel (StateFlow)      │
└──────────────┬──────────────┘
               │ suspend / Flow
┌──────────────▼──────────────┐
│  Repository(单例,顶层)       │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│  Room (SQLite) + DataStore  │
└─────────────────────────────┘
```

### 3.2 顶层单例容器
替代 Hilt,用一个简单的 `AppContainer`:

```kotlin
// SushiContainer.kt
object SushiContainer {
    lateinit var database: SushiDatabase
        private set
    lateinit var repository: SushiRepository
        private set
    lateinit var timeFormatter: TimeFormatter
        private set

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context, SushiDatabase::class.java, "sushi.db"
        ).build()
        repository = SushiRepository(
            database.timeRecordDao(),
            database.categoryDao(),
            database.settingsDao()
        )
        timeFormatter = TimeFormatter()
    }
}
```

### 3.3 状态管理
- 每个 ViewModel 暴露一个 `StateFlow<UiState>`
- UI 用 `collectAsStateWithLifecycle()` 订阅
- 一次性事件用 `SharedFlow` 或 `Channel`
- **不引入任何第三方状态库**

### 3.4 目录结构
```
app/src/main/java/com/sushi/app/
├── SushiApp.kt              # Application
├── SushiContainer.kt        # 单例容器
├── MainActivity.kt          # 单 Activity
├── data/
│   ├── db/
│   │   ├── SushiDatabase.kt
│   │   ├── TimeRecordDao.kt
│   │   ├── CategoryDao.kt
│   │   └── SettingsDao.kt
│   ├── model/
│   │   ├── TimeRecord.kt
│   │   ├── Category.kt
│   │   ├── SolarTerm.kt
│   │   └── LunarDate.kt
│   └── repository/
│       └── SushiRepository.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   ├── components/
│   │   ├── SushiButton.kt
│   │   ├── SushiDialog.kt
│   │   ├── SushiCharts.kt
│   │   └── SushiHeatmap.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── history/
│   │   ├── HistoryScreen.kt
│   │   └── HistoryViewModel.kt
│   ├── stats/
│   │   ├── StatsScreen.kt
│   │   └── StatsViewModel.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
├── util/
│   ├── TimeFormatter.kt
│   ├── SolarTermCalculator.kt
│   ├── LunarConverter.kt
│   ├── BackupManager.kt
│   └── HapticFeedback.kt
└── viewmodel/               # 公共 ViewModel
    └── AppViewModel.kt
```

---

## 4. 数据模型

### 4.1 数据表(V1.0 精简为 3 张表)

#### 4.1.1 `time_records`(时间记录)
```sql
CREATE TABLE time_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    start_time INTEGER NOT NULL,        -- 毫秒时间戳
    end_time INTEGER NOT NULL,          -- 毫秒时间戳
    duration_ms INTEGER NOT NULL,       -- 时长(冗余,便于查询)
    category_id INTEGER NOT NULL,       -- 分类 ID
    tag TEXT,                           -- 二级标签(可选)
    note TEXT,                          -- 备注
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
CREATE INDEX idx_records_start ON time_records(start_time);
CREATE INDEX idx_records_category ON time_records(category_id);
```

#### 4.1.2 `categories`(分类)
```sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,                 -- 名称(工作/学习/...)
    color INTEGER NOT NULL,             -- 颜色(ARGB)
    icon TEXT NOT NULL,                 -- 图标名
    sort_order INTEGER NOT NULL,        -- 排序
    is_visible INTEGER NOT NULL DEFAULT 1,
    is_preset INTEGER NOT NULL DEFAULT 0 -- 是否预置(不可删)
);
```

#### 4.1.3 `settings`(设置,K-V 表)
```sql
CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL
);
-- 常用 key: theme_mode, default_category_id, solar_term_enabled
```

### 4.2 预置数据
启动时若 `categories` 表为空,插入 8 大域:
```
1  工作   远山黛 #5A6B7C
2  学习   苔绿   #4A6741
3  家庭   朱砂   #B23A48
4  健康   落柿   #D97847
5  爱好   赭石   #8B6F47
6  社交   陶土   #A65D5D
7  休息   紫灰   #9B8AA0
8  修行   玄黑   #2A2A2A
```

### 4.3 数据库迁移
- 全新工程,**不保留旧数据**
- 老用户首次打开会看到"导入旧数据"选项(基于 JSON 导入)

### 4.4 备份格式(JSON)
```json
{
  "version": 1,
  "exportedAt": "2026-06-13T10:00:00Z",
  "categories": [...],
  "timeRecords": [...]
}
```

---

## 5. 代码改造清单

### 5.1 删除清单(从现有工程中删除)

| 文件/目录 | 原因 |
|---|---|
| `di/` | 移除 Hilt |
| `sync/` (WebDavSyncService, S3SyncService, BackupManager, SyncConfig*) | 不联网 |
| `data/dao/AchievementDao.kt` | 移除成就系统 |
| `data/dao/GoalDao.kt` | 移除目标系统 |
| `data/dao/DailyReflectionDao.kt` | 移除复盘系统 |
| `data/dao/PauseLogDao.kt` | 移除暂停日志 |
| `data/dao/SyncConflictDao.kt` | 移除冲突解决 |
| `data/dao/HelpEntryDao.kt` | 移除帮助系统 |
| `data/dao/AffixDao.kt` | 移除词缀系统 |
| `data/dao/ProfessionDao.kt` | 移除职业系统 |
| `data/dao/TaskDao.kt` | 移除任务系统 |
| `data/model/Achievement.kt` 及相关 | 同上 |
| `data/model/Goal.kt` 及相关 | 同上 |
| `data/model/DailyReflection.kt` 及相关 | 同上 |
| `data/model/PauseLog.kt` 及相关 | 同上 |
| `data/model/SyncConflict.kt` 及相关 | 同上 |
| `data/model/HelpEntry.kt` 及相关 | 同上 |
| `data/model/Affix.kt` 及相关 | 同上 |
| `data/model/Profession.kt` 及相关 | 同上 |
| `data/model/Task.kt` 及相关 | 同上 |
| `data/model/AttributeType.kt`, `SkillCategory.kt` | 移除技能分类系统 |
| `data/repository/SeedData.kt` | 重写为精简版 |
| `logic/ExperienceEngine.kt` | 移除经验系统 |
| `ui/achievement/`, `ui/goal/`, `ui/reflection/`, `ui/help/`, `ui/onboarding/`, `ui/skill/tree/`, `ui/celebration/`, `ui/report/` | 移除对应屏幕 |
| `ui/skill/` | 重写为简化版 |
| `ui/profession/` | 移除 |
| `ui/review/` | 移除 |
| `ui/panel/` | 重写为主页 |
| `ui/navigation/SushiNavHost.kt` | 重写 |
| `viewmodel/FocusViewModel.kt`, `SkillViewModel.kt`, `ProfessionViewModel.kt`, `ReviewViewModel.kt`, `PanelViewModel.kt` | 重写/删除 |
| `viewmodel/SyncViewModel.kt` | 删除 |
| `util/HapticFeedback.kt` | 保留 |
| `AndroidManifest.xml` 中的网络权限 | 移除 |

**预计删除**: ~70% 现有代码

### 5.2 改造清单(从现有工程中重写)

| 文件 | 改造点 |
|---|---|
| `SushiApp.kt` | 简化为初始化 Container |
| `MainActivity.kt` | 单 Activity,无 NavHost |
| `SushiDatabase.kt` | 3 张表(V1,V2) |
| `SushiRepository.kt` | 仅保留 TimeRecord + Category + Settings |
| `data/db/Migrations.kt` | 单一起始版本 |
| `ui/theme/Color.kt` | 宋式极简色板 |
| `ui/theme/Type.kt` | 思源宋/黑/楷体 + Roboto Mono |
| `ui/theme/Theme.kt` | 浅色/深色/跟随系统 |

### 5.3 新建清单(从零新增)

| 文件 | 用途 |
|---|---|
| `SushiContainer.kt` | 顶层单例容器 |
| `util/SolarTermCalculator.kt` | 24 节气计算 |
| `util/LunarConverter.kt` | 农历转换 |
| `util/BackupManager.kt` | 备份/恢复(本地文件) |
| `util/TimeFormatter.kt` | 时长格式化("2时35分") |
| `util/SolarTermQuotes.kt` | 48 句节气古语 |
| `data/model/SolarTerm.kt` | 节气枚举 |
| `data/model/LunarDate.kt` | 农历数据类 |
| `ui/components/SushiButton.kt` | 主按钮(朱砂色) |
| `ui/components/SushiDialog.kt` | 模态框 |
| `ui/components/SushiHeatmap.kt` | 365 天热力图 |
| `ui/components/SushiCharts.kt` | 精简图表(饼/柱) |
| `ui/home/HomeScreen.kt` | 主页 |
| `ui/home/HomeViewModel.kt` | 主页 ViewModel |
| `ui/history/HistoryScreen.kt` | 历史页 |
| `ui/history/HistoryViewModel.kt` | 历史 ViewModel |
| `ui/stats/StatsScreen.kt` | 统计页 |
| `ui/stats/StatsViewModel.kt` | 统计 ViewModel |
| `ui/settings/SettingsScreen.kt` | 设置页 |
| `ui/settings/SettingsViewModel.kt` | 设置 ViewModel |
| `assets/fonts/` | 思源字体子集(3 个) |
| `assets/icons/` | Lucide 图标 |
| `res/values/strings.xml` | 中文文案 |
| `res/values-zh-rTW/strings.xml` | 繁体(可选) |
| `res/drawable/ic_launcher_*.xml` | 应用图标 |
| `res/xml/backup_rules.xml` | 备份规则 |

---

## 6. 阶段划分与里程碑

### 6.1 总览
| 阶段 | 周次 | 主题 | 交付物 |
|---|---|---|---|
| **P1** | Week 1 | 工程瘦身 + 核心数据 | 可编译运行的精简工程 |
| **P2** | Week 2 | UI 与交互 | 三 Tab 完整可点击 |
| **P3** | Week 3 | 节气、农历、备份 | 功能完整 |
| **P4** | Week 4 | 打磨、测试、发布 | APK 上架 |

### 6.2 里程碑
- **M1** (Day 7): 工程瘦身完成,APK < 10MB,主页可显示
- **M2** (Day 14): 记录/历史/统计三 Tab 全通
- **M3** (Day 21): 节气、农历、备份恢复可用
- **M4** (Day 28): 上架至少 3 个应用市场

---

## 7. 任务清单(周维度)

### Week 1: 工程瘦身 + 核心数据

| Day | 任务 | 验收 |
|---|---|---|
| 1 | 备份当前工程为 `archive-31features/` | 备份完成 |
| 1 | 新建精简 `app/build.gradle.kts` | 依赖只剩核心 |
| 1 | 移除 `di/`, `sync/` | 编译通过 |
| 2 | 重写 `SushiDatabase.kt`(3 张表) | Migration OK |
| 2 | 重写 `SushiRepository.kt` | 暴露基础 CRUD |
| 3 | 新建 `SushiContainer.kt` 单例 | 可在 Activity 中获取 |
| 3 | 重写 `SushiApp.kt`, `MainActivity.kt` | App 可启动 |
| 4 | 新建 `theme/Color.kt` 极简色板 | 主题切换 |
| 4 | 新建 `theme/Type.kt` 字体 | 字体渲染正确 |
| 5 | 新建 `theme/Theme.kt` 浅/深色 | 主题正常 |
| 5 | 新建 `ui/components/SushiButton.kt` | 按钮样式 |
| 6 | 新建 `data/model/Category.kt`,`TimeRecord.kt` | 编译通过 |
| 6 | 新建 `util/TimeFormatter.kt` | 单元测试通过 |
| 7 | 新建主页空壳 `HomeScreen.kt` | 显示"开始"按钮 |
| 7 | 打包 APK,记录大小 | APK < 8MB ✅ |

### Week 2: 核心 UI

| Day | 任务 | 验收 |
|---|---|---|
| 8 | 主页:实现"开始-暂停-继续-结束"流 | 可记录一次完整会话 |
| 9 | 主页:接 8 大域分类选择弹窗 | 选择后保存 |
| 9 | 主页:当日累计显示 | 数据正确 |
| 10 | 历史页:月历视图 | 月份可切换 |
| 11 | 历史页:时间轴列表 | 当日记录显示 |
| 11 | 历史页:点击记录可编辑/删除 | 编辑弹窗正常 |
| 12 | 统计页:日饼图 | 8 色正确 |
| 12 | 统计页:周柱图 | 与上周对比 |
| 13 | 统计页:月饼图 | 含节气标注 |
| 13 | 统计页:年热力图 | 365 天色块 |
| 14 | 设置页:6 项基础 | 全部可点击 |
| 14 | 全流程联通测试 | 3 Tab 切换流畅 |

### Week 3: 节气、农历、备份

| Day | 任务 | 验收 |
|---|---|---|
| 15 | `SolarTermCalculator.kt`(2025-2050 节气) | 单元测试通过 |
| 15 | `LunarConverter.kt`(农历转换算法) | 单元测试通过 |
| 16 | `SolarTermQuotes.kt`(48 句古语) | 数据录入 |
| 16 | 主页显示节气 + 农历 | 显示正确 |
| 17 | `BackupManager.kt`(JSON 导出) | 导出文件可读 |
| 18 | `BackupManager.kt`(JSON 导入) | 导入后可恢复 |
| 19 | 设置页接备份/恢复 | 流程通 |
| 19 | 设置页接"清空全部" | 二次确认弹窗 |
| 20 | 主题模式 3 选(浅/深/跟随) | 切换即时生效 |
| 20 | 默认分类设置 | 下次记录默认选中 |
| 21 | 全功能回归测试 | 无 P0/P1 Bug |

### Week 4: 打磨、测试、发布

| Day | 任务 | 验收 |
|---|---|---|
| 22 | 应用图标(5 种尺寸) | Material 标准 |
| 22 | 启动屏 | 主题色 |
| 23 | 性能优化(启动速度) | < 500ms |
| 23 | APK 体积优化(R8/ProGuard) | < 8MB |
| 24 | 真机测试:小米/华为/OPPO/vivo 各 1 台 | 无闪退 |
| 25 | 应用市场资料:截图/描述/隐私政策 | 准备齐全 |
| 26 | 上架:酷安、应用宝、华为、小米 | 至少 3 家 |
| 27 | 写官网静态页(GitHub Pages 或 Gitee) | 在线可访问 |
| 27 | 写 README、开源协议 | MIT |
| 28 | 发布 1.0.0 tag | GitHub 上线 |

---

## 8. 质量保障

### 8.1 测试策略
| 类型 | 工具 | 范围 |
|---|---|---|
| 单元测试 | JUnit 4 + Truth | 工具类(节气和、农历、时长) |
| UI 测试 | Compose Test | 主页 4 状态、历史/统计渲染 |
| 真机测试 | 手动 | 4 大国产系统各 1 台 |
| 性能测试 | Android Profiler | 启动速度、内存、帧率 |

### 8.2 性能基准
- **启动时间**: 冷启动 < 500ms
- **页面切换**: < 100ms
- **APK 大小**: < 8MB
- **方法数**: < 30K(R8 后)
- **内存占用**: < 100MB
- **帧率**: 60fps 稳定

### 8.3 兼容性
- **minSdk**: 21(Android 5.0)
- **targetSdk**: 34(Android 14)
- **ABI**: arm64-v8a, armeabi-v7a, x86_64
- **屏幕**: 4.7" - 7" 全支持
- **系统**: MIUI/EMUI/OriginOS/ColorOS/HyperOS

### 8.4 隐私与权限
**目标: 0 权限**
- ❌ 不申请网络权限
- ❌ 不申请存储权限(用 SAF)
- ❌ 不申请通知权限
- ❌ 不申请位置权限
- ❌ 不申请任何其他权限

---

## 9. 发布计划

### 9.1 应用市场上架清单
| 市场 | 优先级 | 备注 |
|---|---|---|
| 酷安 | ⭐⭐⭐ | 极客用户首选 |
| 应用宝 | ⭐⭐⭐ | 腾讯系,覆盖广 |
| 华为应用市场 | ⭐⭐⭐ | 华为手机多 |
| 小米应用商店 | ⭐⭐⭐ | MIUI 装机 |
| OPPO 软件商店 | ⭐⭐ | |
| vivo 应用商店 | ⭐⭐ | |
| 谷歌 Play(海外) | ⭐ 选做 | 主要面向中文用户 |

### 9.2 应用市场资料
- **应用名**: 素时
- **副标题**: 离线时间账本
- **类别**: 效率 / 工具
- **图标**: 宣纸白底 + 朱砂"素"字
- **截图**: 5 张(主页/历史/统计/设置/关于)
- **描述**: 见 8.5
- **隐私政策**: 一句话(无网络无收集)
- **开发者**: 个人实名

### 9.3 隐私政策(简短版)
```
素时隐私政策(2026年6月)

1. 本 App 不连接任何服务器
2. 本 App 不收集任何数据
3. 本 App 不申请任何系统权限
4. 所有数据仅存储在您设备本地
5. 卸载 App 后,所有数据自动清除
6. 开源地址: github.com/xxx/sushi
```

### 9.4 官网
**单页静态网站**(1 个 HTML 文件):
- 标题 + 一句话定位
- 4 张 App 截图
- 3 个下载按钮(酷安/应用宝/华为)
- 开源地址 + 隐私政策链接

---

## 10. 长期维护

### 10.1 维护原则
**少即是多**:
- 只修 bug,不增功能
- 一年最多 1 次小版本(只改 UI 细节)
- 永远不引入网络/AI/统计/广告

### 10.2 接受 PR 类型
✅ 接受:
- Bug 修复
- 性能优化
- 新节气的古语
- 翻译(其他语言)
- 无障碍改进

❌ 拒绝:
- 任何需要联网的功能
- 任何 AI 功能
- 任何统计/追踪
- 任何形式的广告位

### 10.3 应急预案
| 情况 | 应对 |
|---|---|
| Android 大版本不兼容 | 暂停应用,等待社区 PR |
| 国产系统故意限制 | 在仓库说明,不强行适配 |
| 用户量暴增(可能性低) | 无服务器,无压力 |
| 收到收购意向 | 礼貌拒绝(纯公益承诺) |

### 10.4 退出策略
**作者失去维护能力时**:
- 代码已在 GitHub,任何人可 fork
- 应用市场保留旧版本,用户不受影响
- 官网放"停止维护说明"

---

## 附录 B: 风险登记

| # | 风险 | 概率 | 影响 | 应对 |
|---|---|---|---|---|
| 1 | 节气/农历算法不准 | 中 | 中 | 复用成熟开源算法 |
| 2 | 国产系统杀后台 | 高 | 低 | 计时用前台 Service |
| 3 | R8 误删反射 | 中 | 高 | 保留必要 keep 规则 |
| 4 | 用户找不到时间入口 | 中 | 中 | 主按钮足够大,文案明确 |
| 5 | 数据丢失 | 低 | 高 | 鼓励用户定期备份 |
| 6 | 应用市场审核不通过 | 中 | 中 | 不收费,无敏感内容 |

---

## 附录 C: 资源清单

### C.1 字体(开源)
- 思源宋体(Source Han Serif) — SIL OFL
- 思源黑体(Source Han Sans) — SIL OFL
- 方正楷体 — 需找替代(如霞鹜文楷,SIL OFL)
- Roboto Mono — Apache 2.0

### C.2 图标
- Lucide Icons — MIT

### C.3 节气古语
- 来自《二十四节气歌》及古人诗词,公共领域

### C.4 农历算法
- 寿星天文历 — 开源
- 或简化查表法(2025-2050 硬编码,精度足够)

---

**文档结束。** 请审阅 `DOCUMENT.md` 和 `DEV_PLAN.md`,如确认无误,即可启动 V1.0 开发。
