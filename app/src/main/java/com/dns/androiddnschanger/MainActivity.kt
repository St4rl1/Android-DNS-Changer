package com.dns.androiddnschanger

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dns.androiddnschanger.ui.theme.AndroidDNSChangerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidDNSChangerTheme {
                MainScreen(
                    onSettingsClick = {
                        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onSettingsClick: () -> Unit = {}) {
    val dnsServers = listOf(
        DNSInfo("Off", "Disabled", "🚫"),
        DNSInfo("Cloudflare", "1.1.1.1", "☁️"),
        DNSInfo("Google", "dns.google", "🔍"),
        DNSInfo("AdGuard", "dns.adguard.com", "🛡️"),
    )

    var selectedDns by remember { mutableStateOf<DNSInfo?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSettingsClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "Available DNS Servers",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Tap to view details • Use Quick Settings to switch",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Grid čtverečků
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(dnsServers) { dns ->
                    DNSTile(
                        dns = dns,
                        onClick = { selectedDns = dns }
                    )
                }
            }
        }
    }

    // Dialog s detaily
    selectedDns?.let { dns ->
        AlertDialog(
            onDismissRequest = { selectedDns = null },
            icon = { Text(text = dns.emoji, fontSize = 48.sp) },
            title = { Text(text = dns.name) },
            text = {
                Column {
                    Text(
                        text = "DNS Address:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dns.address,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDns = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun DNSTile(dns: DNSInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dns.emoji,
                fontSize = 56.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = dns.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class DNSInfo(
    val name: String,
    val address: String,
    val emoji: String
)