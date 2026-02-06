package com.dns.androiddnschanger

import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class DNSTileService : TileService() {

    // DNS servery pro přepínání
    private val dnsServers = listOf(
        DNSOption("Off", "off", null),
        DNSOption("Cloudflare", "hostname", "1.1.1.1"),
        DNSOption("Google", "hostname", "dns.google"),
        DNSOption("AdGuard", "hostname", "dns.adguard.com")
    )

    private var currentIndex = 0

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        // Zkontroluj oprávnění
        if (!hasWriteSecureSettingsPermission()) {
            Toast.makeText(this, "Potřebuješ povolit ADB oprávnění!", Toast.LENGTH_LONG).show()
            return
        }

        // Přepni na další DNS
        currentIndex = (currentIndex + 1) % dnsServers.size
        val dns = dnsServers[currentIndex]

        setDNS(dns)
        updateTile()

        Toast.makeText(this, "DNS: ${dns.name}", Toast.LENGTH_SHORT).show()
    }

    private fun setDNS(dns: DNSOption) {
        try {
            Settings.Global.putString(
                contentResolver,
                "private_dns_mode",
                dns.mode
            )

            if (dns.hostname != null) {
                Settings.Global.putString(
                    contentResolver,
                    "private_dns_specifier",
                    dns.hostname
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Chyba: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val dns = dnsServers[currentIndex]

        tile.label = dns.name
        tile.state = if (dns.mode == "off") Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.updateTile()
    }

    private fun hasWriteSecureSettingsPermission(): Boolean {
        return try {
            Settings.Global.putString(contentResolver, "test_permission", "test")
            true
        } catch (e: SecurityException) {
            false
        }
    }

    data class DNSOption(
        val name: String,
        val mode: String,
        val hostname: String?
    )
}