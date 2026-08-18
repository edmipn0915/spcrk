package com.spcrk.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spcrk.app.ai.McpManager
import com.spcrk.app.data.McpServer
import com.spcrk.app.data.Repository
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpManageScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { Repository(context) }
    val mcpManager = remember { McpManager() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showToolTestDialog by remember { mutableStateOf(false) }
    var selectedServer by remember { mutableStateOf<McpServer?>(null) }
    var testResult by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    val servers by repository.getAllMcpServers().collectAsState(initial = emptyList())

    LaunchedEffect(servers) {
        servers.forEach { server ->
            if (mcpManager.getServer(server.id.toString()) == null) {
                mcpManager.addServer(server)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MCP 管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加服务器")
            }
        }
    ) { padding ->
        if (servers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无 MCP 服务器",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击右下角按钮添加服务器",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(servers) { server ->
                    McpServerItem(
                        server = server,
                        connectionState = mcpManager.getConnectionState(server.id.toString()),
                        onToggle = { enabled ->
                            scope.launch {
                                repository.updateMcpServer(server.copy(isEnabled = enabled))
                            }
                        },
                        onDelete = {
                            scope.launch {
                                repository.deleteMcpServer(server)
                                mcpManager.removeServer(server.id.toString())
                            }
                        },
                        onTest = {
                            selectedServer = server
                            showToolTestDialog = true
                        },
                        onReconnect = {
                            mcpManager.reconnectServer(server.id.toString())
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMcpServerDialog(
            mcpManager = mcpManager,
            onDismiss = { showAddDialog = false },
            onAdd = { server ->
                scope.launch {
                    repository.addMcpServer(server)
                    showAddDialog = false
                }
            }
        )
    }

    if (showToolTestDialog && selectedServer != null) {
        ToolTestDialog(
            server = selectedServer!!,
            mcpManager = mcpManager,
            onDismiss = { showToolTestDialog = false }
        )
    }
}

@Composable
fun McpServerItem(
    server: McpServer,
    connectionState: McpManager.ConnectionState,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    onReconnect: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (server.connectionType) {
                        "sse" -> Icons.Default.Cloud
                        "streamablehttp" -> Icons.Default.CloudQueue
                        else -> Icons.Default.Terminal
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row {
                        Text(
                            text = server.connectionType.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (server.source != "manual") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "[${server.source.uppercase()}]",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
                Switch(
                    checked = server.isEnabled,
                    onCheckedChange = onToggle
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionStateIndicator(connectionState)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onReconnect) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "重连",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onTest) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "测试",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除服务器") },
            text = { Text("确定要删除「${server.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun ConnectionStateIndicator(state: McpManager.ConnectionState) {
    val (color, text) = when (state) {
        McpManager.ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary to "已连接"
        McpManager.ConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiary to "连接中"
        McpManager.ConnectionState.ERROR -> MaterialTheme.colorScheme.error to "错误"
        McpManager.ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) to "未连接"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (state) {
                McpManager.ConnectionState.CONNECTED -> Icons.Default.CheckCircle
                McpManager.ConnectionState.CONNECTING -> Icons.Default.Sync
                McpManager.ConnectionState.ERROR -> Icons.Default.Error
                McpManager.ConnectionState.DISCONNECTED -> Icons.Default.RadioButtonUnchecked
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun AddMcpServerDialog(
    mcpManager: McpManager,
    onDismiss: () -> Unit,
    onAdd: (McpServer) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("快速添加", "JSON 导入", "DXT 包导入", "mcpb 包导入")

    var name by remember { mutableStateOf("") }
    var connectionType by remember { mutableStateOf("stdio") }
    var url by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    var args by remember { mutableStateOf("") }
    var headers by remember { mutableStateOf("") }
    var jsonInput by remember { mutableStateOf("") }
    var dxtFileName by remember { mutableStateOf("") }
    var mcpbFileName by remember { mutableStateOf("") }
    var dxtServer by remember { mutableStateOf<McpServer?>(null) }
    var mcpbServer by remember { mutableStateOf<McpServer?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    val dxtPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File(context.cacheDir, "temp.dxt")
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val server = mcpManager.parseDxtFile(tempFile)
                if (server != null) {
                    dxtServer = server
                    dxtFileName = tempFile.name
                    errorMessage = ""
                } else {
                    errorMessage = "DXT 文件解析失败"
                }
                tempFile.delete()
            } catch (e: Exception) {
                errorMessage = "文件读取失败: ${e.message}"
            }
        }
    }

    val mcpbPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = File(context.cacheDir, "temp.mcpb")
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                val server = mcpManager.parseMcpbFile(tempFile)
                if (server != null) {
                    mcpbServer = server
                    mcpbFileName = tempFile.name
                    errorMessage = ""
                } else {
                    errorMessage = "mcpb 文件解析失败"
                }
                tempFile.delete()
            } catch (e: Exception) {
                errorMessage = "文件读取失败: ${e.message}"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加 MCP 服务器") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                errorMessage = ""
                            },
                            text = { Text(title, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> QuickAddTab(
                        name = name,
                        onNameChange = { name = it },
                        connectionType = connectionType,
                        onConnectionTypeChange = { connectionType = it },
                        url = url,
                        onUrlChange = { url = it },
                        command = command,
                        onCommandChange = { command = it },
                        args = args,
                        onArgsChange = { args = it },
                        headers = headers,
                        onHeadersChange = { headers = it }
                    )
                    1 -> JsonImportTab(
                        jsonInput = jsonInput,
                        onJsonInputChange = { jsonInput = it },
                        onParse = {
                            val server = mcpManager.parseJsonConfig(jsonInput)
                            if (server != null) {
                                onAdd(server)
                                errorMessage = ""
                            } else {
                                errorMessage = "JSON 解析失败"
                            }
                        }
                    )
                    2 -> DxtImportTab(
                        fileName = dxtFileName,
                        server = dxtServer,
                        onSelectFile = {
                            dxtPickerLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                        }
                    )
                    3 -> McpbImportTab(
                        fileName = mcpbFileName,
                        server = mcpbServer,
                        onSelectFile = {
                            mcpbPickerLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                        }
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (selectedTab) {
                        0 -> {
                            if (name.isNotEmpty()) {
                                onAdd(
                                    McpServer(
                                        name = name,
                                        connectionType = connectionType,
                                        url = url,
                                        command = command,
                                        args = args,
                                        headers = headers,
                                        isEnabled = true,
                                        source = "manual"
                                    )
                                )
                            }
                        }
                        2 -> {
                            dxtServer?.let { onAdd(it) }
                        }
                        3 -> {
                            mcpbServer?.let { onAdd(it) }
                        }
                    }
                },
                enabled = when (selectedTab) {
                    0 -> name.isNotEmpty()
                    1 -> false
                    2 -> dxtServer != null
                    3 -> mcpbServer != null
                    else -> false
                }
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun QuickAddTab(
    name: String,
    onNameChange: (String) -> Unit,
    connectionType: String,
    onConnectionTypeChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    command: String,
    onCommandChange: (String) -> Unit,
    args: String,
    onArgsChange: (String) -> Unit,
    headers: String,
    onHeadersChange: (String) -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }
    val connectionTypes = listOf("stdio", "sse", "streamablehttp")

    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("服务器名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            OutlinedTextField(
                value = connectionType.uppercase(),
                onValueChange = {},
                readOnly = true,
                label = { Text("连接方式") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                connectionTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.uppercase()) },
                        onClick = {
                            onConnectionTypeChange(type)
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (connectionType) {
            "stdio" -> {
                OutlinedTextField(
                    value = command,
                    onValueChange = onCommandChange,
                    label = { Text("命令 (uvx/npx)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = args,
                    onValueChange = onArgsChange,
                    label = { Text("参数") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            "sse", "streamablehttp" -> {
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("服务器 URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = headers,
                    onValueChange = onHeadersChange,
                    label = { Text("Headers (JSON 格式)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        }
    }
}

@Composable
fun JsonImportTab(
    jsonInput: String,
    onJsonInputChange: (String) -> Unit,
    onParse: () -> Unit
) {
    Column {
        Text(
            text = "粘贴 MCP 服务器 JSON 配置",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = jsonInput,
            onValueChange = onJsonInputChange,
            label = { Text("JSON 配置") },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            maxLines = 10
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onParse,
            enabled = jsonInput.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("解析并添加")
        }
    }
}

@Composable
fun DxtImportTab(
    fileName: String,
    server: McpServer?,
    onSelectFile: () -> Unit
) {
    Column {
        Text(
            text = "选择 DXT 文件 (ZIP 格式)",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("选择文件")
        }

        if (server != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "解析结果",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("名称: ${server.name}")
                    Text("类型: ${server.connectionType.uppercase()}")
                    if (server.command.isNotEmpty()) {
                        Text("命令: ${server.command}")
                    }
                    if (server.args.isNotEmpty()) {
                        Text("参数: ${server.args}")
                    }
                    if (server.url.isNotEmpty()) {
                        Text("URL: ${server.url}")
                    }
                }
            }
        }
    }
}

@Composable
fun McpbImportTab(
    fileName: String,
    server: McpServer?,
    onSelectFile: () -> Unit
) {
    Column {
        Text(
            text = "选择 mcpb 文件 (二进制包)",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("选择文件")
        }

        if (server != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "解析结果",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("名称: ${server.name}")
                    Text("类型: ${server.connectionType.uppercase()}")
                    if (server.command.isNotEmpty()) {
                        Text("命令: ${server.command}")
                    }
                    if (server.args.isNotEmpty()) {
                        Text("参数: ${server.args}")
                    }
                    if (server.url.isNotEmpty()) {
                        Text("URL: ${server.url}")
                    }
                }
            }
        }
    }
}

@Composable
fun ToolTestDialog(
    server: McpServer,
    mcpManager: McpManager,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTool by remember { mutableStateOf<String?>(null) }
    var toolArgs by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    val tools = mcpManager.getToolsForServer(server.id.toString())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("测试工具 - ${server.name}") },
        text = {
            Column {
                if (tools.isEmpty()) {
                    Text("暂无可用工具")
                } else {
                    Text("选择工具:")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(tools) { tool ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTool = tool.name }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTool == tool.name,
                                    onClick = { selectedTool = tool.name }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = tool.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (tool.description.isNotEmpty()) {
                                        Text(
                                            text = tool.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = toolArgs,
                        onValueChange = { toolArgs = it },
                        label = { Text("参数 (JSON)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            selectedTool?.let { toolName ->
                                scope.launch {
                                    isTesting = true
                                    val args = try {
                                        if (toolArgs.isNotEmpty()) JSONObject(toolArgs) else JSONObject()
                                    } catch (e: Exception) {
                                        JSONObject()
                                    }
                                    testResult = mcpManager.callTool(server.id.toString(), toolName, args)
                                    isTesting = false
                                }
                            }
                        },
                        enabled = selectedTool != null && !isTesting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("执行")
                    }

                    if (testResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("结果:")
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = testResult,
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
