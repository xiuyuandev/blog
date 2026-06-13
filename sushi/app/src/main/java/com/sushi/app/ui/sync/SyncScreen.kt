package com.sushi.app.ui.sync

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.sync.S3Config
import com.sushi.app.sync.SyncProvider
import com.sushi.app.sync.WebDavConfig
import com.sushi.app.ui.theme.CardShape
import com.sushi.app.ui.theme.CardShapeSmall
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.CinnabarFaint
import com.sushi.app.ui.theme.CinnabarLight
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkAlpha08
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkFaintest
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
import com.sushi.app.ui.theme.PaperWarm
import com.sushi.app.ui.theme.PillShape
import com.sushi.app.ui.theme.SushiAnim
import com.sushi.app.ui.theme.SushiSpacing
import com.sushi.app.viewmodel.SyncViewModel

@Composable
fun SyncScreen(
    viewModel: SyncViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromFile(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
    ) {
        // 返回按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .clickable(onClick = onBack)
                .padding(horizontal = SushiSpacing.xl, vertical = SushiSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Linen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkLight
                )
            }
            Spacer(modifier = Modifier.width(SushiSpacing.sm))
            Text(
                text = "返回",
                style = MaterialTheme.typography.bodyMedium,
                color = InkLight
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SushiSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(SushiSpacing.xxl)
        ) {
            Text(
                text = "同步与备份",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink
            )

            // 同步方式选择
            ProviderSelection(
                currentProvider = uiState.syncConfig.provider,
                onSelectProvider = viewModel::selectProvider
            )

            // 配置区域
            when (uiState.syncConfig.provider) {
                SyncProvider.WEBDAV -> WebDavConfigSection(
                    config = uiState.syncConfig.webDavConfig,
                    onSave = viewModel::saveWebDavConfig
                )
                SyncProvider.S3 -> S3ConfigSection(
                    config = uiState.syncConfig.s3Config,
                    onSave = viewModel::saveS3Config
                )
                SyncProvider.NONE -> {}
            }

            // 云端操作
            if (uiState.syncConfig.provider != SyncProvider.NONE) {
                SectionDivider()

                CloudActions(
                    isPushing = uiState.isPushing,
                    isPulling = uiState.isPulling,
                    isTesting = uiState.isTesting,
                    lastSyncTime = uiState.lastSyncTime,
                    onPush = viewModel::pushToCloud,
                    onPull = viewModel::pullFromCloud,
                    onTest = viewModel::testConnection
                )
            }

            SectionDivider()

            // 本地备份
            LocalBackupSection(
                isExporting = uiState.isExporting,
                isImporting = uiState.isImporting,
                onExport = { viewModel.exportToFile() },
                onImport = { filePickerLauncher.launch(arrayOf("application/json")) }
            )

            // 状态消息
            AnimatedVisibility(
                visible = uiState.statusMessage.isNotBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                StatusMessage(
                    message = uiState.statusMessage,
                    onDismiss = viewModel::clearStatusMessage
                )
            }

            // 成功消息自动消失
            if (uiState.statusMessage.isNotBlank()) {
                val isSuccess = !uiState.statusMessage.contains("失败") &&
                        !uiState.statusMessage.contains("错误")
                if (isSuccess) {
                    LaunchedEffect(uiState.statusMessage) {
                        kotlinx.coroutines.delay(5000)
                        viewModel.clearStatusMessage()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(SushiSpacing.xxxl))
    }
}

@Composable
private fun SectionDivider() {
    Divider(
        color = InkFaintest,
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = SushiSpacing.xs)
    )
}

@Composable
private fun ProviderSelection(
    currentProvider: SyncProvider,
    onSelectProvider: (SyncProvider) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.sm)) {
        SectionTitle(title = "同步方式")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(Linen)
                .padding(SushiSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.xs)
        ) {
            ProviderOption(
                label = "坚果云 WebDAV",
                isSelected = currentProvider == SyncProvider.WEBDAV,
                onClick = { onSelectProvider(SyncProvider.WEBDAV) },
                modifier = Modifier.weight(1f)
            )
            ProviderOption(
                label = "七牛云 S3",
                isSelected = currentProvider == SyncProvider.S3,
                onClick = { onSelectProvider(SyncProvider.S3) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProviderOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Cinnabar else Paper,
        animationSpec = tween(SushiAnim.NORMAL),
        label = "providerBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Paper else InkFaint,
        animationSpec = tween(SushiAnim.NORMAL),
        label = "providerText"
    )

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 14.dp)
                .clip(CardShapeSmall)
                .background(Cinnabar)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = InkLight
        )
    }
}

@Composable
private fun WebDavConfigSection(
    config: WebDavConfig,
    onSave: (WebDavConfig) -> Unit
) {
    var serverUrl by remember(config.serverUrl) { mutableStateOf(config.serverUrl) }
    var username by remember(config.username) { mutableStateOf(config.username) }
    var password by remember(config.password) { mutableStateOf(config.password) }
    var remotePath by remember(config.remotePath) { mutableStateOf(config.remotePath) }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(PaperWarm)
            .padding(SushiSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        SectionTitle(title = "坚果云 WebDAV 配置")

        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("服务器地址", color = InkLight) },
            placeholder = { Text("https://dav.jianguoyun.com/dav/", color = InkFaint) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("账号", color = InkLight) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("应用专用密码", color = InkLight) },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .clip(CardShapeSmall)
                        .background(CinnabarFaint)
                        .clickable { showPassword = !showPassword }
                        .padding(horizontal = SushiSpacing.sm, vertical = SushiSpacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showPassword) "隐藏" else "显示",
                        style = MaterialTheme.typography.labelSmall,
                        color = Cinnabar
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = remotePath,
            onValueChange = { remotePath = it },
            label = { Text("远程目录", color = InkLight) },
            placeholder = { Text("/sushi/", color = InkFaint) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSave(WebDavConfig(serverUrl, username, password, remotePath))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Cinnabar,
                contentColor = Paper
            )
        ) {
            Text(text = "保存配置")
        }
    }
}

@Composable
private fun S3ConfigSection(
    config: S3Config,
    onSave: (S3Config) -> Unit
) {
    var endpoint by remember(config.endpoint) { mutableStateOf(config.endpoint) }
    var region by remember(config.region) { mutableStateOf(config.region) }
    var bucket by remember(config.bucket) { mutableStateOf(config.bucket) }
    var accessKey by remember(config.accessKey) { mutableStateOf(config.accessKey) }
    var secretKey by remember(config.secretKey) { mutableStateOf(config.secretKey) }
    var remotePath by remember(config.remotePath) { mutableStateOf(config.remotePath) }
    var showSecret by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, CardShape)
            .clip(CardShape)
            .background(PaperWarm)
            .padding(SushiSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)
    ) {
        SectionTitle(title = "七牛云 S3 配置")

        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            label = { Text("Endpoint", color = InkLight) },
            placeholder = { Text("s3-cn-south-1.qiniucs.com", color = InkFaint) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Region", color = InkLight) },
            placeholder = { Text("cn-south-1", color = InkFaint) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bucket,
            onValueChange = { bucket = it },
            label = { Text("Bucket", color = InkLight) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = accessKey,
            onValueChange = { accessKey = it },
            label = { Text("Access Key", color = InkLight) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = secretKey,
            onValueChange = { secretKey = it },
            label = { Text("Secret Key", color = InkLight) },
            singleLine = true,
            visualTransformation = if (showSecret) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .clip(CardShapeSmall)
                        .background(CinnabarFaint)
                        .clickable { showSecret = !showSecret }
                        .padding(horizontal = SushiSpacing.sm, vertical = SushiSpacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showSecret) "隐藏" else "显示",
                        style = MaterialTheme.typography.labelSmall,
                        color = Cinnabar
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = remotePath,
            onValueChange = { remotePath = it },
            label = { Text("远程路径前缀", color = InkLight) },
            placeholder = { Text("sushi/", color = InkFaint) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cinnabar,
                unfocusedBorderColor = InkFaint,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Ink
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onSave(S3Config(endpoint, region, bucket, accessKey, secretKey, remotePath))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Cinnabar,
                contentColor = Paper
            )
        ) {
            Text(text = "保存配置")
        }
    }
}

@Composable
private fun CloudActions(
    isPushing: Boolean,
    isPulling: Boolean,
    isTesting: Boolean,
    lastSyncTime: String,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onTest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
        SectionTitle(title = "云端操作")

        if (lastSyncTime.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShapeSmall)
                    .background(CinnabarFaint)
                    .padding(horizontal = SushiSpacing.md, vertical = SushiSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
            ) {
                Text(
                    text = "⏱",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "上次同步: $lastSyncTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = Cinnabar
                )
            }
        }

        OutlinedButton(
            onClick = onTest,
            enabled = !isTesting,
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape
        ) {
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = InkLight
                )
                Spacer(modifier = Modifier.width(SushiSpacing.sm))
            }
            Text(
                text = if (isTesting) "测试中…" else "🔗 测试连接",
                color = InkLight
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            Button(
                onClick = onPush,
                enabled = !isPushing,
                modifier = Modifier.weight(1f),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cinnabar,
                    contentColor = Paper,
                    disabledContainerColor = Cinnabar.copy(alpha = 0.4f),
                    disabledContentColor = Paper.copy(alpha = 0.6f)
                )
            ) {
                if (isPushing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Paper
                    )
                    Spacer(modifier = Modifier.width(SushiSpacing.sm))
                }
                Text(text = if (isPushing) "推送中…" else "↑ 推送到云端")
            }

            Button(
                onClick = onPull,
                enabled = !isPulling,
                modifier = Modifier.weight(1f),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = Paper,
                    disabledContainerColor = Ink.copy(alpha = 0.4f),
                    disabledContentColor = Paper.copy(alpha = 0.6f)
                )
            ) {
                if (isPulling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Paper
                    )
                    Spacer(modifier = Modifier.width(SushiSpacing.sm))
                }
                Text(text = if (isPulling) "拉取中…" else "↓ 从云端拉取")
            }
        }
    }
}

@Composable
private fun LocalBackupSection(
    isExporting: Boolean,
    isImporting: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(SushiSpacing.md)) {
        SectionTitle(title = "本地备份")

        Text(
            text = "将数据导出为 JSON 文件保存到本地，或从 JSON 文件恢复数据。",
            style = MaterialTheme.typography.bodySmall,
            color = InkFaint
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm)
        ) {
            OutlinedButton(
                onClick = onExport,
                enabled = !isExporting,
                modifier = Modifier.weight(1f),
                shape = CardShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Cinnabar,
                    disabledContentColor = Cinnabar.copy(alpha = 0.4f)
                )
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Cinnabar.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(SushiSpacing.sm))
                }
                Text(text = if (isExporting) "导出中…" else "📤 导出 JSON")
            }

            OutlinedButton(
                onClick = onImport,
                enabled = !isImporting,
                modifier = Modifier.weight(1f),
                shape = CardShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Ink,
                    disabledContentColor = Ink.copy(alpha = 0.4f)
                )
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Ink.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.width(SushiSpacing.sm))
                }
                Text(text = if (isImporting) "导入中…" else "📥 导入 JSON")
            }
        }
    }
}

@Composable
private fun StatusMessage(
    message: String,
    onDismiss: () -> Unit
) {
    val isError = message.contains("失败") || message.contains("错误")
    val isSuccess = !isError

    val bgColor by animateColorAsState(
        targetValue = if (isError) CinnabarFaint else CinnabarFaint,
        animationSpec = tween(SushiAnim.FAST),
        label = "statusBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(bgColor)
            .padding(horizontal = SushiSpacing.lg, vertical = SushiSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SushiSpacing.sm),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (isSuccess) "✓" else "✗",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) Cinnabar else Cinnabar
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) Cinnabar else Ink
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "×",
                style = MaterialTheme.typography.bodyMedium,
                color = InkFaint
            )
        }
    }
}
