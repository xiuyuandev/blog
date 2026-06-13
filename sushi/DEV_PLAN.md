# 素时 · 开发计划(V1.0 修订版)

> **目标**: 在现有 31 项功能工程基础上,**保留 30 项核心 + 剥离 1 项(白噪音) + 换皮 Material 3 设计语言 + 保留坚果云 WebDAV 同步**,交付符合"Google 风格 + 中国气质 + 离线公益"的素时 V1.0 APK。
> **策略**: 不推翻,而是**减法换皮**。
> **撰写日期**: 2026-06-13 · 修订版(保留 WebDAV)

---

## 目录

1. [总体策略](#1-总体策略)
2. [技术栈](#2-技术栈)
3. [架构设计](#3-架构设计)
4. [数据模型](#4-数据模型)
5. [代码改造清单](#5-代码改造清单)
6. [阶段划分与里程碑](#6-阶段划分与里程碑)
7. [任务清单(28 天)](#7-任务清单28-天)
8. [质量保障](#8-质量保障)
9. [发布计划](#9-发布计划)
10. [长期维护](#10-长期维护)

---

## 1. 总体策略

### 1.1 核心原则
> **核心不动(技能为核),只做减法换皮**

| 维度 | 决策 |
|---|---|
| **功能层** | 30 项保留 + 1 项剥离(白噪音) + 保留 WebDAV 同步 |
| **设计层** | 全 UI 换皮为 Material 3 + 中国色板 |
| **交互层** | 改用 Material 3 标准组件(NavigationBar / ExtendedFAB / ModalBottomSheet) |
| **数据层** | 完全保留(已经过验证) |
| **业务逻辑** | 完全保留(经验引擎、毕业逻辑、Streak、成就) |
| **依赖层** | 移除 Hilt(用顶层单例)、保留 Compose/Room/OkHttp(仅 WebDAV) |

### 1.2 三件大事
1. **换皮**(60% 工作量): 重写所有 Composable 主题
2. **减负**(20% 工作量): 移除 2 项功能,瘦身依赖
3. **打磨**(20% 工作量): 中国本土化、节气/农历、备份

### 1.3 关键决策
| 决策项 | 选择 | 理由 |
|---|---|---|
| DI 框架 | **不用 Hilt,改用顶层单例** | 减依赖、减方法数 |
| 网络库 | **保留 OkHttp(仅 WebDAV),移除 S3 SDK/Retrofit** | WebDAV 是用户自主选择 |
| 数据库 | **Room 保留**(已建 v3 迁移) | 稳定,本地 |
| UI 框架 | **Compose 保留 + Material 3 升级** | 主流、Jetpack |
| 主题 | **Material 3 baseline + 中国静态色** | 不用动态取色 |
| 协程/Flow | **保留** | 必要 |
| 序列化 | **Gson 保留**(已在用) | 兼容性 |
| 字体 | **思源黑体/霞鹜文楷内置子集** | 离线可用 |
| 图标 | **Material Symbols Rounded 内置** | 离线、无版权 |
| 备份 | **本地文件 + SAF** | 离线 |
| 云同步 | **仅保留 WebDAV(坚果云)**,用户主动配置 | 用户自托管,数据不经我们 |

---

## 2. 技术栈

### 2.1 当前依赖(保留)
```kotlin
// build.gradle.kts (V1.0 精简版)
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
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")  // 仅用于 WebDAV
    
    // 移除的(原工程有,V1.0 不要)
    // - hilt-android / hilt-compiler
    // - retrofit(直接用 OkHttp)
    // - aws-s3-sdk(改 WebDAV)
    // - material dynamic color(改用静态)
}
```

### 2.2 目标 APK 大小
- 基础 Compose + Material 3: ~3MB
- Room: +0.5MB
- 字体子集(思源黑/霞鹜文楷/思源宋体,各 1MB): +3MB
- 图标(仅按需引入): +0.5MB
- **总计: ~7MB** ✅

### 2.3 权限申请
**目标: 1 权限(仅 WebDAV 同步使用)**
- ✅ **INTERNET** — 仅用于用户主动配置的坚果云 WebDAV 同步,App 不主动连接任何服务器
- ❌ 不申请存储(用 SAF)
- ❌ 不申请通知
- ❌ 不申请位置
- ❌ 不申请任何其他

---

## 3. 架构设计

### 3.1 总体架构
**保留现有 MVVM + Repository 模式**,用顶层单例替代 Hilt。

```
┌─────────────────────────────┐
│  UI (Compose Screens)       │  ← 5 Tab + FAB
└──────────────┬──────────────┘
               │ collectAsState
┌──────────────▼──────────────┐
│  ViewModel (StateFlow)      │  ← 现有 ViewModel 全部保留
└──────────────┬──────────────┘
               │ suspend / Flow
┌──────────────▼──────────────┐
│  SushiContainer(单例,顶层)   │  ← 替代 Hilt
│  ├─ Repository               │
│  ├─ ExperienceEngine         │
│  └─ SolarTerm/Lunar/Backup   │
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│  Room (SQLite v3) + Prefs   │  ← 完全保留
└─────────────────────────────┘
```

### 3.2 顶层单例容器
替代 Hilt,引入 `SushiContainer` 单例:

```kotlin
// SushiContainer.kt
object SushiContainer {
    lateinit var database: SushiDatabase private set
    lateinit var repository: SushiRepository private set
    lateinit var engine: ExperienceEngine private set
    lateinit var backupManager: BackupManager private set
    lateinit var solarTermCalculator: SolarTermCalculator private set
    lateinit var lunarConverter: LunarConverter private set

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context, SushiDatabase::class.java, "sushi.db"
        ).build()
        repository = SushiRepository(
            timeRecordDao = database.timeRecordDao(),
            skillDao = database.skillDao(),
            // ... 11 个 DAO
        )
        engine = ExperienceEngine(repository)
        solarTermCalculator = SolarTermCalculator()
        lunarConverter = LunarConverter()
        backupManager = BackupManager(context, repository)
    }
}
```

### 3.3 ViewModel 改造
将现有 ViewModel 中的 `@Inject` 改为手动注入:

```kotlin
// 现有
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: SushiRepository
) : ViewModel()

// 改为
class HomeViewModel(
    private val repository: SushiRepository = SushiContainer.repository
) : ViewModel() {
    companion object {
        fun create() = HomeViewModel()
    }
}
```

### 3.4 状态管理
- 每个 ViewModel 暴露 `StateFlow<UiState>`
- UI 用 `collectAsStateWithLifecycle()` 订阅
- 一次性事件用 `SharedFlow` 或 `Channel`
- **不引入任何第三方状态库**

### 3.5 目录结构(V1.0 精简)
```
app/src/main/java/com/sushi/app/
├── SushiApp.kt              # Application(精简)
├── SushiContainer.kt        # 单例容器(新增)
├── MainActivity.kt          # 单 Activity + 底部导航
├── data/
│   ├── db/
│   │   ├── SushiDatabase.kt        # v3,保留
│   │   ├── TimeRecordDao.kt        # 保留
│   │   ├── SkillDao.kt             # 保留
│   │   ├── ProfessionDao.kt        # 保留
│   │   ├── TaskDao.kt              # 保留
│   │   ├── AchievementDao.kt       # 保留
│   │   ├── GoalDao.kt              # 保留
│   │   ├── DailyReflectionDao.kt   # 保留
│   │   ├── PauseLogDao.kt          # 保留
│   │   ├── HelpEntryDao.kt         # 保留
│   │   └── Migrations.kt           # 保留
│   ├── model/              # 全部保留
│   │   ├── TimeRecord.kt
│   │   ├── Skill.kt
│   │   ├── Profession.kt
│   │   ├── Task.kt
│   │   ├── Achievement.kt
│   │   ├── Goal.kt
│   │   ├── DailyReflection.kt
│   │   ├── PauseLog.kt
│   │   ├── HelpEntry.kt
│   │   ├── SolarTerm.kt            # 新增
│   │   └── LunarDate.kt            # 新增
│   └── repository/
│       ├── SushiRepository.kt      # 保留
│       ├── SeedData.kt             # 保留(精简)
│       └── BackupManager.kt        # 新增(替代云同步)
├── logic/
│   └── ExperienceEngine.kt        # 保留(产品灵魂)
├── sync/                    # ❌ 整目录删除
│   ├── BackupManager.kt
│   ├── S3SyncService.kt
│   ├── SyncConfig.kt
│   ├── SyncConfigManager.kt
│   ├── SyncService.kt
│   └── WebDavSyncService.kt
├── di/                      # ❌ 整目录删除
│   ├── DatabaseModule.kt
│   ├── RepositoryModule.kt
│   └── SyncModule.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt                # 重写:中国静态色板
│   │   ├── Theme.kt                # 重写:Material 3
│   │   ├── Type.kt                 # 重写:中文字体
│   │   └── Shape.kt                # 新增:Material 3 圆角
│   ├── components/                 # 重写
│   │   ├── SushiButton.kt          # 重写:Material 3 Button
│   │   ├── SushiDialog.kt          # 重写:Material 3 AlertDialog
│   │   ├── SushiSheet.kt           # 重写:ModalBottomSheet
│   │   ├── SushiSnackbar.kt        # 新增:统一反馈
│   │   ├── SushiCharts.kt          # 重写:用 Material 3 配色
│   │   ├── SushiHeatmap.kt         # 重写:GitHub 风格
│   │   ├── SushiIcons.kt           # 改用 Material Symbols
│   │   └── LevelUpCelebration.kt   # 重写:克制版
│   ├── home/                       # 重写:5 Tab 主页
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── skill/                      # 保留并重写
│   │   ├── SkillScreen.kt
│   │   ├── SkillDetailScreen.kt    # 新增
│   │   ├── SkillViewModel.kt
│   │   └── tree/
│   │       ├── SkillTreeScreen.kt
│   │       └── SkillTreeViewModel.kt
│   ├── profession/                 # 保留
│   ├── task/                       # 新增独立目录
│   │   ├── TaskScreen.kt
│   │   └── TaskViewModel.kt
│   ├── goal/                       # 保留
│   ├── achievement/                # 保留
│   ├── report/                     # 保留
│   ├── reflection/                 # 保留
│   ├── onboarding/                 # 保留
│   ├── help/                       # 保留
│   ├── settings/                   # 新增(原 SyncScreen 拆出)
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── navigation/
│       └── SushiNavHost.kt         # 重写:5 Tab 布局
├── util/
│   ├── HapticFeedback.kt           # 保留
│   ├── TimeFormatter.kt            # 保留
│   ├── SolarTermCalculator.kt      # 新增
│   ├── LunarConverter.kt           # 新增
│   └── SolarTermQuotes.kt          # 新增
├── viewmodel/                      # 精简
│   ├── AppViewModel.kt             # 合并所有公共状态
│   └── ...
├── MainActivity.kt                 # 重写:用 NavHost
└── SushiApp.kt                     # 精简
```

---

## 4. 数据模型

### 4.1 数据库:完全保留 v3
**v3 已包含 17 张表 + 31 项功能所需全部字段**,V1.0 不动结构。

主要表:
- `time_records`(时间记录)
- `skills`(技能)
- `professions`(职业)
- `tasks`(任务)
- `achievements`(成就)
- `goals`(目标)
- `daily_reflections`(复盘)
- `pause_logs`(暂停日志)
- `help_entries`(帮助)
- ... 等

### 4.2 预置数据
- 8 大域(职业)
- 默认技能
- 14 项成就
- 17 条帮助词条
- 48 句节气古语
- 24 节气 + 农历(2025-2050 硬编码)

### 4.3 备份格式(JSON)
完全保留现有 BackupManager 的 JSON 格式,加 V1.0 版本号。

```json
{
  "version": 1,
  "exportedAt": "2026-06-13T10:00:00Z",
  "appVersion": "1.0.0",
  "skills": [...],
  "professions": [...],
  "tasks": [...],
  "timeRecords": [...],
  "achievements": [...],
  "goals": [...],
  "dailyReflections": [...],
  "pauseLogs": [...]
}
```

---

## 5. 代码改造清单

### 5.1 删除清单(从现有工程中删除)

| 路径 | 原因 |
|---|---|
| `di/` 整目录 | 移除 Hilt,改用 SushiContainer |
| `sync/S3SyncService.kt` | 国内访问不便,完全移除 |
| `sync/SyncConfig.kt`(复杂版) | 简化为只支持 WebDAV |
| `ui/sync/SyncScreen.kt` | 改写为 WebDAV 配置页 |
| `viewmodel/SyncViewModel.kt` | 改写为 WebDAV ViewModel |
| `sync/BackupManager.kt`(原云备份版) | 改写为本地 JSON 备份(在 data/repository/) |
| Retrofit 引用 | 直接用 OkHttp |
| AWS S3 SDK | 不需要 |
| `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />` | 不需要 |
| `<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />` | 改用 SAF |
| **保留** `sync/WebDavSyncService.kt` | 用户自主选择同步 |
| **保留** `sync/SyncService.kt`(接口) | 接口保留,只实现 WebDAV |

**预计删除**: ~15% 现有代码(主要是 S3 + 复杂 Sync)

### 5.2 重写清单(换皮)

| 文件 | 改造点 |
|---|---|
| `ui/theme/Color.kt` | Material 3 baseline + 中国静态色 |
| `ui/theme/Theme.kt` | Material 3 浅/深色主题 |
| `ui/theme/Type.kt` | 思源黑体/霞鹜文楷 + Roboto Mono |
| `ui/theme/Shape.kt`(新增) | Material 3 圆角 |
| `ui/components/*` | 全部用 Material 3 组件重写 |
| `ui/navigation/SushiNavHost.kt` | 改用 NavigationBar 5 Tab |
| `MainActivity.kt` | 集成 NavHost |
| `SushiApp.kt` | 初始化 SushiContainer |
| `ui/celebration/LevelUpCelebration.kt` | 改克制版(无粒子) |
| `ui/sync/SyncScreen.kt` | 改写为只支持 WebDAV 的配置页 |
| `viewmodel/SyncViewModel.kt` | 改写为 WebDAV ViewModel |

### 5.3 新建清单

| 文件 | 用途 |
|---|---|
| `SushiContainer.kt` | 顶层单例容器 |
| `ui/theme/Shape.kt` | Material 3 形状 |
| `ui/settings/SettingsScreen.kt` | 独立的设置页(从原 SyncScreen 拆出) |
| `ui/settings/SettingsViewModel.kt` | 设置 ViewModel |
| `util/SolarTermCalculator.kt` | 24 节气计算 |
| `util/LunarConverter.kt` | 农历转换 |
| `util/SolarTermQuotes.kt` | 48 句节气古语 |
| `data/model/SolarTerm.kt` | 节气枚举 |
| `data/model/LunarDate.kt` | 农历数据类 |
| `data/repository/BackupManager.kt`(新写) | 本地 JSON 备份/恢复 |
| `assets/fonts/SourceHanSansSC-Regular.otf` | 思源黑体 |
| `assets/fonts/SourceHanSerifSC-Bold.otf` | 思源宋体 |
| `assets/fonts/LXGWWenKai-Regular.ttf` | 霞鹜文楷 |
| `res/values/strings.xml` | 全部中文字串 |
| `res/drawable/ic_launcher_*.xml` | 应用图标 |
| `res/xml/network_security_config.xml` | 允许用户配置的坚果云域名(可选 HTTPS) |

### 5.4 保留清单(无需改动)
- 全部 11 个 DAO
- 全部 data/model
- 全部 logic/ExperienceEngine
- 全部业务 ViewModel(改为手动注入)
- 全部 ui/focus、ui/skill、ui/goal 等业务屏幕(仅换主题)

---

## 6. 阶段划分与里程碑

### 6.1 总览
| 阶段 | 周次 | 主题 | 交付物 |
|---|---|---|---|
| **P1** | Week 1 | 减负 + 容器 + 主题骨架 | 编译通过,旧屏可显示 |
| **P2** | Week 2 | 5 Tab 导航 + Material 3 换皮 | 5 Tab 全通,Material 风格 |
| **P3** | Week 3 | 中国本土化(节气/农历) + 备份 | 完整功能 |
| **P4** | Week 4 | 打磨 + 性能 + 发布 | APK 上架 |

### 6.2 里程碑
- **M1** (Day 7): 减负完成,SushiContainer 启动,主题换皮,APK 跑通
- **M2** (Day 14): 5 Tab 全部 Material 3 化,所有屏幕可点击
- **M3** (Day 21): 节气、农历、本地备份全部可用
- **M4** (Day 28): 上架至少 3 个应用市场

---

## 7. 任务清单(28 天)

### Week 1: 减负 + 容器 + 主题骨架

| Day | 任务 | 验收 |
|---|---|---|
| 1 | 备份当前工程为 `archive-31features/` | 备份完成 |
| 1 | 删除 `di/` `sync/` 目录 | 编译报错清单 |
| 1 | 创建 `SushiContainer.kt` 顶层单例 | 容器可初始化 |
| 2 | 重写 `SushiApp.kt`,初始化 Container | App 启动无 Crash |
| 2 | 改写 `MainActivity.kt`,集成 NavHost | 单 Activity 跑通 |
| 3 | 重写 `ui/theme/Color.kt`(中国静态色) | 调色板可调 |
| 3 | 重写 `ui/theme/Type.kt`(中文字体) | 字体渲染 |
| 4 | 重写 `ui/theme/Theme.kt`(Material 3 主题) | 浅/深色切换 |
| 4 | 新建 `ui/theme/Shape.kt` | 圆角标准化 |
| 5 | 集成 Material Symbols Rounded | 图标正常 |
| 5 | 移除 Hilt 引用,改用 Container | 编译通过 |
| 6 | 移除 Retrofit / AWS S3 SDK | 编译通过 |
| 6 | 保留 INTERNET 权限(Manifest 标注用途) | Manifest 干净 |
| 7 | 全工程编译,记录 APK 大小 | APK < 12MB |

### Week 2: 5 Tab 导航 + Material 3 换皮

| Day | 任务 | 验收 |
|---|---|---|
| 8 | 新建 `ui/navigation/SushiNavHost.kt` 5 Tab | Tab 可切换 |
| 8 | 主页 `HomeScreen.kt` 改写为 Material 3 风格 | 主页美观 |
| 9 | 技能页 `SkillScreen.kt` 改写 | Material 3 卡片 |
| 9 | 技能详情页 `SkillDetailScreen.kt` 新建 | 点击进入 |
| 10 | 目标页 `GoalScreen.kt` 改写 | 目标卡片 |
| 10 | 报告页 `ReportScreen.kt` 改写(保留图表) | 4 段切换 |
| 11 | 我的页 `SettingsScreen.kt` 新建 | 列表样式 |
| 11 | `SyncScreen.kt` 改写为 WebDAV 配置页(输入坚果云账号/密码/路径) | 配置可用 |
| 11 | 成就页、复盘页、任务页全部 Material 化 | 风格统一 |
| 12 | FAB 改造(ExtendedFAB) | 浮动按钮正确 |
| 12 | ModalBottomSheet 替换 AlertDialog | 所有弹窗为底部弹出 |
| 13 | LevelUpCelebration 改克制版(数字翻牌) | 升级动画 |
| 13 | 12 边角 / 字体 / 颜色 全局统查 | 视觉一致 |
| 14 | 5 Tab 全流程联调 | 切换流畅,无报错 |

### Week 3: 中国本土化 + 备份

| Day | 任务 | 验收 |
|---|---|---|
| 15 | `SolarTermCalculator.kt`(2025-2050 节气) | 单元测试通过 |
| 15 | `LunarConverter.kt`(农历转换) | 单元测试通过 |
| 16 | `SolarTermQuotes.kt`(48 句古语) | 数据录入 |
| 16 | 主页 AppBar 显示节气 + 农历 | 显示正确 |
| 17 | 8 大域职业色用中国传统色命名 | 视觉调整 |
| 17 | 古语用霞鹜文楷渲染 | 字体显示 |
| 18 | 新建 `data/repository/BackupManager.kt`(本地) | JSON 导出 |
| 18 | 设置页接"导出/导入" | 文件对话框 |
| 19 | 设置页接"清空全部"(二次确认) | 二次弹窗 |
| 19 | 设置页接主题切换 | 即时生效 |
| 20 | CSV 导出(MediaStore 保留) | 导出成功 |
| 20 | 概念解释卡 + 帮助中心正常 | 文案显示 |
| 21 | Onboarding 流程跑通 | 首次启动 |
| 21 | 全功能回归测试 | 无 P0/P1 Bug |

### Week 4: 打磨 + 性能 + 发布

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
**目标: 1 权限(详见 §2.3)**
- INTERNET 仅用于用户主动配置的坚果云 WebDAV 同步
- App 不主动连接任何服务器
- 不收集任何使用数据、崩溃数据

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
- **副标题**: 离线技能成长记录器
- **类别**: 效率 / 工具
- **图标**: 宣纸白底 + 青墨"素"字
- **截图**: 5 张(主页/技能/目标/报告/我的)
- **描述**: 见文档 §8.1
- **隐私政策**: 一句话(无网络无收集)
- **开发者**: 个人实名

### 9.3 隐私政策(简短版)
```
素时隐私政策(2026年6月)

1. 本 App 不连接任何服务器(默认情况下)
2. 本 App 不收集任何数据
3. 唯一申请的权限:INTERNET,仅用于您主动配置的坚果云 WebDAV 同步
4. 同步数据直接存到您自己的坚果云账号,不经任何第三方(包括我们)
5. 所有数据仅存储在您设备本地 + 您自己的坚果云(若启用同步)
6. 卸载 App 后,设备本地数据自动清除
7. 开源地址: github.com/xxx/sushi
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
- **白噪音/在线音频**(违背离线原则)

### 10.3 应急预案
| 情况 | 应对 |
|---|---|
| Android 大版本不兼容 | 暂停应用,等待社区 PR |
| 国产系统杀后台 | 计时器用前台 Service(已实现) |
| 用户量暴增 | 无服务器,无压力 |
| 收到收购意向 | 礼貌拒绝(纯公益承诺) |

### 10.4 退出策略
**作者失去维护能力时**:
- 代码已在 GitHub,任何人可 fork
- 应用市场保留旧版本,用户不受影响
- 官网放"停止维护说明"

---

## 附录 A: V1.0 与原 31 项的对照

| 原 31 项 | V1.0 决策 | 改造方式 |
|---|---|---|
| 1 Streak | ✅ 保留 | 换 Material 3 卡片 |
| 2 跨日归属 | ✅ 保留 | 不动 |
| 3 年度热力图 | ✅ 保留 | 改 GitHub 风格 |
| 4 趋势曲线 | ✅ 保留 | 改 Material 3 配色 |
| 5 技能分布饼图 | ✅ 保留 | 同上 |
| 6 成就系统(14) | ✅ 保留 | 列表静默化 |
| 7 周/月目标 | ✅ 保留 | 卡片化 |
| 8 升级庆祝 | ✅ 保留(克制化) | 数字翻牌 |
| 9 周/月报 | ✅ 保留 | 4 段切换 |
| 10 每日复盘 | ✅ 保留 | FullScreenDialog |
| 11 暂停次数 | ✅ 保留 | 主页显示 |
| 12 暂停原因 | ✅ 保留 | ModalBottomSheet |
| 13 活跃时段 | ✅ 保留 | 柱图 |
| 14 标签 | ✅ 保留 | Chip 组件 |
| 15 周期任务 | ✅ 保留 | 不动 |
| 16 任务优先级 | ✅ 保留 | 不动 |
| 17 任务模板 | ✅ 保留 | 不动 |
| 18 预估时长 | ✅ 保留 | 不动 |
| 19 技能树 | ✅ 保留 | 2D 节点图 |
| 20 职业专属任务 | ✅ 保留 | 不动 |
| 21 技能毕业 | ✅ 保留 | 不动 |
| 22 每日一句 | ✅ 保留(改节气) | 古语库 |
| 23 深色模式 | ✅ 保留 | Material 3 |
| 24 全屏专注 | ✅ 保留 | 不动 |
| **25 白噪音** | ❌ **剥离** | 删除 |
| 26 ~~S3 同步~~ → 改 WebDAV | ✅ **保留(改 WebDAV)** | 用户自托管,数据存用户自己的坚果云 |
| 27 数据导入(本地) | ✅ 保留 | JSON |
| 28 Onboarding | ✅ 保留 | 流程跑通 |
| 29 概念解释 | ✅ 保留 | 卡片化 |
| 30 帮助中心 | ✅ 保留 | 列表化 |
| 31 CSV 导出 | ✅ 保留 | MediaStore |

**总账**: 31 项 → **30 项保留 + 1 项剥离(白噪音)**。
**S3 同步被 WebDAV(坚果云)取代**,作为可选同步方式保留。

---

## 附录 B: 风险登记

| # | 风险 | 概率 | 影响 | 应对 |
|---|---|---|---|---|
| 1 | 节气/农历算法不准 | 中 | 中 | 复用成熟开源算法 |
| 2 | 国产系统杀后台 | 高 | 低 | 计时用前台 Service |
| 3 | R8 误删反射 | 中 | 高 | 保留必要 keep 规则 |
| 4 | 用户找不到时间入口 | 中 | 中 | FAB 足够大,文案明确 |
| 5 | 数据丢失 | 低 | 高 | 鼓励用户定期备份 |
| 6 | 应用市场审核不通过 | 中 | 中 | 不收费,无敏感内容 |
| 7 | Material 3 主题换皮遗漏 | 高 | 低 | Week 2 统查 |

---

## 附录 C: 资源清单

### C.1 字体(开源)
- 思源黑体(Source Han Sans SC) — SIL OFL
- 思源宋体(Source Han Serif SC) — SIL OFL(备用)
- 霞鹜文楷(LXGW WenKai) — SIL OFL
- Roboto Mono — Apache 2.0

### C.2 图标
- Material Symbols Rounded — Apache 2.0

### C.3 节气古语
- 24 节气经典古诗词,公共领域

### C.4 农历算法
- 寿星天文历 — 开源
- 或简化查表法(2025-2050 硬编码,精度足够)

---

**文档结束。** 请审阅 [DOCUMENT.md](file:///workspace/sushi/DOCUMENT.md) 和本文档,如确认无误,即可启动 V1.0 开发。
