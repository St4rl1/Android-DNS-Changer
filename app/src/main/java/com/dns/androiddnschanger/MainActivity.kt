package com.dns.androiddnschanger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.dns.androiddnschanger.ui.theme.AndroidDNSChangerTheme
import android.widget.Toast
import androidx.compose.material.icons.filled.Menu
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidDNSChangerTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dnsManager = remember { DNSManager(context) }
    val hapticFeedback = LocalHapticFeedback.current

    var dnsList by remember { mutableStateOf(dnsManager.getAllDNS()) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // Získej filtrovaný seznam (bez "off")
        val filteredList = dnsList.filter { it.hostname != "off" }

        // Přeuspořádej pouze filtrovaný seznam
        val reorderedList = filteredList.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }

        // Aktualizuj celý seznam (zachovej "Off" na začátku)
        dnsList = listOf(dnsList.first { it.hostname == "off" }) + reorderedList

        // Ulož nové pořadí
        dnsManager.saveOrder(reorderedList)

        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "DNS Servers",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Drag icon to reorder • Tap + to add",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                if (dnsList.size == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No DNS servers yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add servers",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(
                            items = dnsList.filter { it.hostname != "off" },
                            key = { it.hostname }
                        ) { dns ->
                            ReorderableItem(reorderableLazyListState, key = dns.hostname) { isDragging ->
                                val elevation by animateDpAsState(
                                    targetValue = if (isDragging) 8.dp else 0.dp,
                                    label = "elevation"
                                )

                                DNSCard(
                                    dns = dns,
                                    elevation = elevation,
                                    dragModifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    ),
                                    onDelete = if (dns.isCustom) {
                                        {
                                            dnsManager.removeDNS(dns)
                                            dnsList = dnsManager.getAllDNS()
                                        }
                                    } else null,
                                    onEdit = if (dns.isCustom) {
                                        { newName, newHostname ->
                                            dnsManager.removeDNS(dns)
                                            dnsManager.addCustomDNS(newName, newHostname)
                                            dnsList = dnsManager.getAllDNS()
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // FAB Add vpravo dole
            FloatingActionButton(
                onClick = { showCustomDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add DNS",
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }

            // FAB Settings vlevo dole
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    // Dialogy zůstávají stejné...
    if (showTemplateDialog) {
        TemplateSelectionDialog(
            onDismiss = { showTemplateDialog = false },
            onTemplateSelected = { template ->
                val exists = dnsList.any { it.hostname == template.hostname }
                if (exists) {
                    Toast.makeText(context, "This DNS already exists", Toast.LENGTH_SHORT).show()
                } else {
                    dnsManager.addCustomDNS(template.name, template.hostname)
                    dnsList = dnsManager.getAllDNS()
                    showTemplateDialog = false
                }
            },
            onCustomSelected = {
                showTemplateDialog = false
                showCustomDialog = true
            },
            existingDNS = dnsList
        )
    }

    if (showCustomDialog) {
        CustomDNSDialog(
            onDismiss = { showCustomDialog = false },
            onAdd = { name, hostname ->
                val exists = dnsList.any { it.hostname == hostname }
                if (exists) {
                    Toast.makeText(context, "This DNS already exists", Toast.LENGTH_SHORT).show()
                } else {
                    dnsManager.addCustomDNS(name, hostname)
                    dnsList = dnsManager.getAllDNS()
                    showCustomDialog = false
                }
            },
            onTemplates = {
                showCustomDialog = false
                showTemplateDialog = true
            }
        )
    }
}

@Composable
fun DNSCard(
    dns: DNSInfo,
    elevation: Dp = 0.dp,
    dragModifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    onEdit: ((String, String) -> Unit)? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dns.isCustom)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DRAG HANDLE - ikona vlevo
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Reorder",
                modifier = dragModifier
                    .padding(end = 8.dp)
                    .size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Image(
                painter = painterResource(id = dns.iconRes),
                contentDescription = dns.name,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dns.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dns.hostname,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onEdit != null) {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (onDelete != null) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Delete DNS Server",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${dns.name}\"?",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        EditDNSDialog(
            dns = dns,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newHostname ->
                onEdit?.invoke(newName, newHostname)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDNSDialog(
    dns: DNSInfo,
    onDismiss: () -> Unit,
    onSave: (name: String, hostname: String) -> Unit
) {
    var name by remember { mutableStateOf(dns.name) }
    var hostname by remember { mutableStateOf(dns.hostname) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Edit DNS Server",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Name") },
                    isError = showError && name.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hostname,
                    onValueChange = {
                        hostname = it
                        showError = false
                    },
                    label = { Text("Hostname") },
                    isError = showError && hostname.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text(
                        text = "Both fields are required",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || hostname.isBlank()) {
                        showError = true
                    } else {
                        onSave(name, hostname)
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelectionDialog(
    onDismiss: () -> Unit,
    onTemplateSelected: (DNSTemplate) -> Unit,
    onCustomSelected: () -> Unit,
    existingDNS: List<DNSInfo> = emptyList()
) {
    val templates = listOf(
        DNSTemplate("Cloudflare DNS", "one.one.one.one", R.drawable.ic_cloudflare),
        DNSTemplate("Google DNS", "dns.google", R.drawable.ic_google),
        DNSTemplate("Mullvad DNS", "base.dns.mullvad.net", R.drawable.ic_mullvad),
        DNSTemplate("Quad9 DNS", "dns.quad9.net", R.drawable.ic_quad9),
        DNSTemplate("AdGuard DNS", "dns.adguard-dns.com", R.drawable.ic_adguard),
        DNSTemplate("OpenDNS", "dns.opendns.com", R.drawable.ic_opendns),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Add DNS Server",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Choose a template:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    templates.chunked(2).forEach { rowTemplates ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTemplates.forEach { template ->
                                val isDisabled = existingDNS.any { it.hostname == template.hostname }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = !isDisabled) {
                                            onTemplateSelected(template)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDisabled)
                                            Color(0xFF1A1D23)
                                        else
                                            MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isDisabled)
                                            Color(0xFF2A2D33)
                                        else
                                            MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = template.iconRes),
                                            contentDescription = template.name,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .alpha(if (isDisabled) 0.4f else 1f)
                                        )
                                        Text(
                                            template.name,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            color = if (isDisabled)
                                                Color(0xFF575E68)
                                            else
                                                MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            if (rowTemplates.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onCustomSelected,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Dismiss")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDNSDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, hostname: String) -> Unit,
    onTemplates: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hostname by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Custom DNS Server",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showError = false
                    },
                    label = { Text("Name") },
                    placeholder = { Text("My DNS") },
                    isError = showError && name.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hostname,
                    onValueChange = {
                        hostname = it
                        showError = false
                    },
                    label = { Text("Hostname") },
                    placeholder = { Text("dns.example.com") },
                    isError = showError && hostname.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (showError) {
                    Text(
                        text = "Both fields are required",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "Example: dns.adguard.com or 1.1.1.1",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                TextButton(
                    onClick = onTemplates,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use templates instead")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || hostname.isBlank()) {
                        showError = true
                    } else {
                        onAdd(name, hostname)
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

data class DNSTemplate(
    val name: String,
    val hostname: String,
    val iconRes: Int
)

data class DNSInfo(
    val name: String,
    val hostname: String,
    val iconRes: Int,
    val isCustom: Boolean = false
)

class DNSManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("dns_prefs", Context.MODE_PRIVATE)
    private val CUSTOM_DNS_KEY = "custom_dns_list"

    private val defaultDNS = DNSInfo("Off", "off", R.drawable.ic_cloud, isCustom = false)

    fun getAllDNS(): List<DNSInfo> {
        val customDNS = getCustomDNS()
        return listOf(defaultDNS) + customDNS
    }

    fun getIconForDNS(dns: DNSInfo): Int {
        return when {
            dns.hostname == "off" -> R.drawable.ic_cloud
            dns.hostname.contains("cloudflare", ignoreCase = true) ||
                    dns.hostname.contains("one.one.one.one", ignoreCase = true) -> R.drawable.ic_cloudflare
            dns.hostname.contains("google", ignoreCase = true) -> R.drawable.ic_google
            dns.hostname.contains("mullvad", ignoreCase = true) -> R.drawable.ic_mullvad
            dns.hostname.contains("quad9", ignoreCase = true) -> R.drawable.ic_quad9
            dns.hostname.contains("adguard", ignoreCase = true) -> R.drawable.ic_adguard
            dns.hostname.contains("opendns", ignoreCase = true) -> R.drawable.ic_opendns
            else -> dns.iconRes // Použij ikonu z DNSInfo
        }
    }

    fun addCustomDNS(name: String, hostname: String) {
        val current = getCustomDNS().toMutableList()

        val iconRes = getIconForName(name)
        val newDNS = DNSInfo(name, hostname, iconRes, isCustom = true)

        current.add(newDNS)
        saveCustomDNS(current)
    }

    fun removeDNS(dns: DNSInfo) {
        val current = getCustomDNS().toMutableList()
        current.remove(dns)
        saveCustomDNS(current)
    }

    fun saveOrder(orderedList: List<DNSInfo>) {
        saveCustomDNS(orderedList)
    }

    private fun getIconForName(name: String): Int {
        return when {
            name.contains("Cloudflare", ignoreCase = true) || name.contains("CF", ignoreCase = true) -> R.drawable.ic_cloudflare
            name.contains("Google", ignoreCase = true) -> R.drawable.ic_google
            name.contains("Mullvad", ignoreCase = true) -> R.drawable.ic_mullvad
            name.contains("AdGuard", ignoreCase = true) || name.contains("AG", ignoreCase = true) -> R.drawable.ic_adguard
            name.contains("Quad9", ignoreCase = true) -> R.drawable.ic_quad9
            name.contains("OpenDNS", ignoreCase = true) -> R.drawable.ic_opendns
            name.contains("NextDNS", ignoreCase = true) -> R.drawable.ic_nextdns
            else -> R.drawable.ic_cloud
        }
    }

    private fun getCustomDNS(): List<DNSInfo> {
        val json = prefs.getString(CUSTOM_DNS_KEY, "[]") ?: "[]"
        return try {
            val items = json.removeSurrounding("[", "]").split("},{")
            items.mapNotNull { item ->
                if (item.isBlank()) return@mapNotNull null
                val parts = item.replace("{", "").replace("}", "").split(",")
                if (parts.size >= 2) {
                    val name = parts[0].split(":")[1].trim('"')
                    val hostname = parts[1].split(":")[1].trim('"')
                    val iconRes = getIconForName(name)

                    DNSInfo(name, hostname, iconRes, isCustom = true)
                } else null
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomDNS(list: List<DNSInfo>) {
        val json = list.joinToString(",", "[", "]") { dns ->
            """{"name":"${dns.name}","hostname":"${dns.hostname}"}"""
        }
        prefs.edit().putString(CUSTOM_DNS_KEY, json).apply()
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AndroidDNSChangerTheme {
        MainScreen()
    }
}