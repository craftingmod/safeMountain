package com.unopenedbox.craftingmod.safemountain

import android.Manifest
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WallpaperCheckReceiver : BroadcastReceiver() {
    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sp = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
            if (sp.getBoolean("use_service", false)) {
                context.startService(Intent(context, WallpaperChangeDetector::class.java))
            }
            GlobalScope.launch {
                if (!Util.checkWallpaper(context)) {
                    val wallpaperMgr = WallpaperManager.getInstance(context)
                    launch(Dispatchers.Default) {
                        wallpaperMgr.clear(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    }
                } else {
                    Log.i("SafeMountain", "Wallpaper is safe.")
                }
            }
        }
    }
}