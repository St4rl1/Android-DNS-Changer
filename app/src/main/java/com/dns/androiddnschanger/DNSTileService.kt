package com.dns.androiddnschanger

import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class DNSTileService : TileService() {

    private var currentIndex = 0
    private lateinit var dnsManager: DNSManager

    override fun onCreate() {
        super.onCreate()
        dnsManager = DNSManager(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun setDNS(hostname: String, name: String) {
        try {
            if (hostname == "off") {
                Settings.Global.putString(
                    contentResolver,
                    "private_dns_mode",
                    "off"
                )
            } else {
                Settings.Global.putString(
                    contentResolver,
                    "private_dns_mode",
                    "hostname"
                )
                Settings.Global.putString(
                    contentResolver,
                    "private_dns_specifier",
                    hostname
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Chyba: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onClick() {
        super.onClick()
        if (!hasWriteSecureSettingsPermission()) {
            Toast.makeText(
                this,
                "Potřebuješ povolit ADB oprávnění!\n\nadb shell pm grant com.dns.androiddnschanger android.permission.WRITE_SECURE_SETTINGS",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // ⬇️ FILTRUJ JEN AKTIVNÍ DNS
        val dnsServers = dnsManager.getAllDNS().filter { it.isActive }

        if (dnsServers.size <= 1) {
            Toast.makeText(this, "Přidej a aktivuj DNS servery v aplikaci!", Toast.LENGTH_LONG).show()
            return
        }

        currentIndex = (currentIndex + 1) % dnsServers.size
        val dns = dnsServers[currentIndex]

        setDNS(dns.hostname, dns.name)
        updateTile()

        Toast.makeText(this, "DNS: ${dns.name}", Toast.LENGTH_SHORT).show()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        // ⬇️ FILTRUJ JEN AKTIVNÍ DNS
        val dnsServers = dnsManager.getAllDNS().filter { it.isActive }

        if (currentIndex >= dnsServers.size) {
            currentIndex = 0
        }

        val dns = dnsServers[currentIndex]

        tile.label = dns.name
        tile.state = if (dns.hostname == "off") Tile.STATE_INACTIVE else Tile.STATE_ACTIVE

        val iconRes = dnsManager.getIconForDNS(dns)
        tile.icon = Icon.createWithResource(this, iconRes)

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
}