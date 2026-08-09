package com.aurora.data.storage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

class UsbStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("AuroraStorage", "USB broadcast: $action  path=${intent.dataString}")

        when (action) {
            Intent.ACTION_MEDIA_MOUNTED -> {
                val volumes = StorageScanner.getAvailableStorageVolumes(context)
                val usbVolume = volumes.find { it.isRemovable }
                if (usbVolume != null) {
                    Log.i("AuroraStorage", "USB mounted — ${usbVolume.displayName} (${StorageScanner.formatSize(usbVolume.availableBytes)} free)")
                    showToast(context, "USB Detected: ${usbVolume.displayName}")
                }
            }
            Intent.ACTION_MEDIA_EJECT,
            Intent.ACTION_MEDIA_UNMOUNTED,
            Intent.ACTION_MEDIA_REMOVED -> {
                Log.w("AuroraStorage", "USB removed — check download settings")
                showToast(context, "USB Disconnected — open Settings to confirm storage")
            }
        }
    }

    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            try {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        fun createFilter(): IntentFilter {
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED)
            filter.addAction(Intent.ACTION_MEDIA_EJECT)
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            filter.addAction(Intent.ACTION_MEDIA_REMOVED)
            filter.addDataScheme("file")
            return filter
        }
    }
}