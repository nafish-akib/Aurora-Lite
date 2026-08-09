package com.aurora.engine

data class FilePickerRequest(
    val id: Long,
    val mimeTypes: List<String> = emptyList(),
    val isMultiple: Boolean = false,
    val isCapture: Boolean = false,
    val onComplete: ((List<String>) -> Unit) = {},
    val onCancel: (() -> Unit) = {}
) {
    fun complete(uris: List<String>) = onComplete(uris)
    fun cancel() = onCancel()
}

data class PermissionRequest(
    val id: Long,
    val url: String,
    val domain: String,
    val permission: String,
    val description: String,
    val onGrant: (() -> Unit) = {},
    val onDeny: (() -> Unit) = {}
) {
    fun grant() = onGrant()
    fun deny() = onDeny()
}

data class SitePermissionInfo(
    val domain: String,
    val permissions: Map<String, Boolean> = emptyMap()
)

class SitePermissionsService {
    private val sitePermissions = mutableMapOf<String, MutableMap<String, Boolean>>()

    fun getPermission(domain: String, permission: String): Boolean? {
        return sitePermissions[domain]?.get(permission)
    }

    fun setPermission(domain: String, permission: String, granted: Boolean) {
        sitePermissions.getOrPut(domain) { mutableMapOf() }[permission] = granted
    }

    fun clearSite(domain: String) {
        sitePermissions.remove(domain)
    }

    fun clearAll() {
        sitePermissions.clear()
    }

    fun getSitePermissions(domain: String): Map<String, Boolean> {
        return sitePermissions[domain]?.toMap() ?: emptyMap()
    }

    companion object {
        fun permissionLabel(permission: String): String = when (permission) {
            PERMISSION_CAMERA -> "Camera"
            PERMISSION_MICROPHONE -> "Microphone"
            PERMISSION_LOCATION -> "Location"
            PERMISSION_NOTIFICATIONS -> "Notifications"
            PERMISSION_STORAGE -> "Persistent Storage"
            PERMISSION_AUTOPLAY -> "Autoplay"
            else -> permission
        }

        fun permissionIcon(permission: String): String = when (permission) {
            PERMISSION_CAMERA -> "\uD83D\uDCF7"
            PERMISSION_MICROPHONE -> "\uD83C\uDFA4"
            PERMISSION_LOCATION -> "\uD83D\uDCCD"
            PERMISSION_NOTIFICATIONS -> "\uD83D\uDD14"
            PERMISSION_STORAGE -> "\uD83D\uDCBE"
            PERMISSION_AUTOPLAY -> "\u25B6"
            else -> "\uD83D\uDD12"
        }

        const val PERMISSION_CAMERA = "camera"
        const val PERMISSION_MICROPHONE = "microphone"
        const val PERMISSION_LOCATION = "geolocation"
        const val PERMISSION_NOTIFICATIONS = "desktop-notification"
        const val PERMISSION_STORAGE = "persistent-storage"
        const val PERMISSION_AUTOPLAY = "autoplay-media"
    }
}
