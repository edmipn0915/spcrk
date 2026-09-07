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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.data.McpServer
import com.spcrk.app.ai.api.McpConnectionState
import com.spcrk.app.ui.l10n.appStrings
import com.spcrk.app.ui.navigation.Screen
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpManageScreen(
    onBackClick: () -> Unit,
    viewModel: McpManageViewModel = viewModel(factory = McpManageViewModelFactory(LocalContext.current.applicationContext as android.app.Application))
) {
    val scope = rememberCoroutineScope()
    val s = appStrings()

    var showAddDialog by remember { mutableStateOf(false) }
    var showToolTestDialog by remember { mutableStateOf(false) }
    var selectedServer by remember { mutableStateOf<McpServer?>(null) }
    var testResult by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    val servers by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.mcpManageTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = s.back)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = s.addServer)
            }
        }
    ) { padding ->
        if (servers.servers.isEmpty()) {
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
                        text = s.noMcpServers,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s.addMcpServerHint,
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
                items(servers.servers) { server ->
                    McpServerItem(
                        server = server,
                        connectionState = viewModel.getConnectionState(server.id.toString()),
                        onToggle = { enabled -> viewModel.toggleServer(server, enabled) },
                        onDelete = { viewModel.deleteServer(server) },
                        onTest = {
                            selectedServer = server
                            showToolTestDialog = true
                        },
                        onReconnect = { viewModel.reconnectServer(server) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddMcpServerDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onAdd = { server ->
                viewModel.addServer(server)
                showAddDialog = false
            }
        )
    }

    if (showToolTestDialog && selectedServer != null) {
        ToolTestDialog(
            server = selectedServer!!,
            viewModel = viewModel,
            onDismiss = { showToolTestDialog = false }
        )
    }
}

@Composable
fun McpServerItem(
    server: McpServer,
    connectionState: McpConnectionState,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    onReconnect: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val s = appStrings()

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
                        contentDescription = s.reconnect,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onTest) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = s.test,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = s.delete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.deleteServerTitle) },
            text = { Text(String.format(s.deleteServerConfirm, server.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(s.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(s.cancel)
                }
            }
        )
    }
}

@Composable
fun ConnectionStateIndicator(state: McpConnectionState) {
    val s = appStrings()
    val (color, text) = when (state) {
        McpConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary to s.connected
        McpConnectionState.CONNECTING -> MaterialTheme.colorScheme.tertiary to s.connecting
        McpConnectionState.ERROR -> MaterialTheme.colorScheme.error to s.error
        McpConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) to s.disconnected
        else -> MaterialTheme.colorScheme.onSurfaceVariant to s.unknown
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (state) {
                McpConnectionState.CONNECTED -> Icons.Default.CheckCircle
                McpConnectionState.CONNECTING -> Icons.Default.Sync
                McpConnectionState.ERROR -> Icons.Default.Error
                McpConnectionState.DISCONNECTED -> Icons.Default.RadioButtonUnchecked
                else -> Icons.Default.Help
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
    viewModel: McpManageViewModel,
    onDismiss: () -> Unit,
    onAdd: (McpServer) -> Unit
) {
    val s = appStrings()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(s.quickAdd, s.jsonImport, s.dxtImport, s.mcpbImport)

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
                val server = viewModel.parseDxtFile(tempFile)
                if (server != null) {
                    dxtServer = server
                    dxtFileName = tempFile.name
                    errorMessage = ""
                } else {
                    errorMessage = s.dxtParseFailed
                }
                tempFile.delete()
            } catch (e: Exception) {
                errorMessage = String.format(s.fileReadFailed, e.message)
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
                val server = viewModel.parseMcpbFile(tempFile)
                if (server != null) {
                    mcpbServer = server
                    mcpbFileName = tempFile.name
                    errorMessage = ""
                } else {
                    errorMessage = s.mcpbParseFailed
                }
                tempFile.delete()
            } catch (e: Exception) {
                errorMessage = String.format(s.fileReadFailed, e.message)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.addMcpServer) },
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
                            val server = viewModel.parseJsonConfig(jsonInput, "json")
                            if (server != null) {
                                onAdd(server)
                                errorMessage = ""
                            } else {
                                errorMessage = s.jsonParseFailed
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
                Text(s.add)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
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
    val s = appStrings()
    val connectionTypes = listOf("stdio", "sse", "streamablehttp")

    Column {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(s.serverName) },
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
                label = { Text(s.connectionMethod) },
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
                    label = { Text(s.command) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = args,
                    onValueChange = onArgsChange,
                    label = { Text(s.arguments) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            "sse", "streamablehttp" -> {
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text(s.serverUrl) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = headers,
                    onValueChange = onHeadersChange,
                    label = { Text(s.headersLabel) },
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
    val s = appStrings()
    Column {
        Text(
            text = s.pasteMcpJsonHint,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = jsonInput,
            onValueChange = onJsonInputChange,
            label = { Text(s.jsonConfig) },
            modifier = Modifier.fillMaxWidth().height(200.dp),
            maxLines = 10
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onParse,
            enabled = jsonInput.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(s.parseAndAdd)
        }
    }
}

@Composable
fun DxtImportTab(
    fileName: String,
    server: McpServer?,
    onSelectFile: () -> Unit
) {
    val s = appStrings()
    Column {
        Text(
            text = s.selectDxtFile,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(s.selectFile)
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
                        text = s.parseResult,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${s.name}: ${server.name}")
                    Text("${s.type}: ${server.connectionType.uppercase()}")
                    if (server.command.isNotEmpty()) {
                        Text("${s.command}: ${server.command}")
                    }
                    if (server.args.isNotEmpty()) {
                        Text("${s.arguments}: ${server.args}")
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
    val s = appStrings()
    Column {
        Text(
            text = s.selectMcpbFile,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(s.selectFile)
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
                        text = s.parseResult,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${s.name}: ${server.name}")
                    Text("${s.type}: ${server.connectionType.uppercase()}")
                    if (server.command.isNotEmpty()) {
                        Text("${s.command}: ${server.command}")
                    }
                    if (server.args.isNotEmpty()) {
                        Text("${s.arguments}: ${server.args}")
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
    viewModel: McpManageViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTool by remember { mutableStateOf<String?>(null) }
    var toolArgs by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }
    val s = appStrings()

    val tools = viewModel.getToolsForServer(server.id.toString())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(String.format(s.testToolTitle, server.name)) },
        text = {
            Column {
                if (tools.isEmpty()) {
                    Text(s.noAvailableTools)
                } else {
                    Text(s.selectTool)
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
                        label = { Text(s.argsJsonLabel) },
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
                                    testResult = viewModel.callTool(server.id.toString(), toolName, args)
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
                        Text(s.execute)
                    }

                    if (testResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(s.result)
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
                Text(s.close)
            }
        }
    )
}
