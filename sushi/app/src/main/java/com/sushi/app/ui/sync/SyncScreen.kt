package com.sushi.app.ui.sync

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sushi.app.sync.S3Config
import com.sushi.app.sync.SyncProvider
import com.sushi.app.sync.WebDavConfig
import com.sushi.app.ui.theme.Cinnabar
import com.sushi.app.ui.theme.Ink
import com.sushi.app.ui.theme.InkFaint
import com.sushi.app.ui.theme.InkLight
import com.sushi.app.ui.theme.Linen
import com.sushi.app.ui.theme.Paper
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
                .clickable(onClick = onBack)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.headlineMedium,
                color = InkLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "返回",
                style = MaterialTheme.typography.bodyMedium,
                color = InkLight
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
            CloudActions(
                isPushing = uiState.isPushing,
                isPulling = uiState.isPulling,
                isTesting = uiState.isTesting,
                onPush = viewModel::pushToCloud,
                onPull = viewModel::pullFromCloud,
                onTest = viewModel::testConnection
            )
        }

        // 本地备份
        LocalBackupSection(
            isExporting = uiState.isExporting,
            isImporting = uiState.isImporting,
            onExport = { viewModel.exportToFile() },
            onImport = { filePickerLauncher.launch(arrayOf("application/json")) }
        )

        // 状态消息
        if (uiState.statusMessage.isNotBlank()) {
            StatusMessage(
                message = uiState.statusMessage,
                onDismiss = viewModel::clearStatusMessage
            )
        }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProviderSelection(
    currentProvider: SyncProvider,
    onSelectProvider: (SyncProvider) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "同步方式",
            style = MaterialTheme.typography.labelLarge,
            color = InkLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
    Box(
        modifier = modifier
            .background(if (isSelected) Linen else Paper)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Ink else InkFaint
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
            .background(Linen)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "坚果云 WebDAV 配置",
            style = MaterialTheme.typography.labelLarge,
            color = Ink
        )

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
            visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (showPassword) "隐藏" else "显示",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cinnabar,
                    modifier = Modifier.clickable { showPassword = !showPassword }
                )
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

        OutlinedButton(
            onClick = {
                onSave(WebDavConfig(serverUrl, username, password, remotePath))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "保存配置", color = Cinnabar)
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
            .background(Linen)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "七牛云 S3 配置",
            style = MaterialTheme.typography.labelLarge,
            color = Ink
        )

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
            visualTransformation = if (showSecret) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Text(
                    text = if (showSecret) "隐藏" else "显示",
                    style = MaterialTheme.typography.labelSmall,
                    color = Cinnabar,
                    modifier = Modifier.clickable { showSecret = !showSecret }
                )
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

        OutlinedButton(
            onClick = {
                onSave(S3Config(endpoint, region, bucket, accessKey, secretKey, remotePath))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "保存配置", color = Cinnabar)
        }
    }
}

@Composable
private fun CloudActions(
    isPushing: Boolean,
    isPulling: Boolean,
    isTesting: Boolean,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onTest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "云端操作",
            style = MaterialTheme.typography.labelLarge,
            color = InkLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onTest,
                enabled = !isTesting,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isTesting) "测试中…" else "测试连接",
                    color = InkLight
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPush,
                enabled = !isPushing,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isPushing) "推送中…" else "推送到云端",
                    color = Cinnabar
                )
            }

            OutlinedButton(
                onClick = onPull,
                enabled = !isPulling,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isPulling) "拉取中…" else "从云端拉取",
                    color = Ink
                )
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "本地备份",
            style = MaterialTheme.typography.labelLarge,
            color = InkLight
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onExport,
                enabled = !isExporting,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isExporting) "导出中…" else "导出 JSON",
                    color = Cinnabar
                )
            }

            OutlinedButton(
                onClick = onImport,
                enabled = !isImporting,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isImporting) "导入中…" else "导入 JSON",
                    color = Ink
                )
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Linen)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) Cinnabar else Ink
        )
        Text(
            text = "×",
            style = MaterialTheme.typography.bodyMedium,
            color = InkFaint,
            modifier = Modifier.clickable(onClick = onDismiss)
        )
    }
}
