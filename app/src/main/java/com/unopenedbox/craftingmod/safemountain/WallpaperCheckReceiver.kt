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
import kotlinx.coroutines.withContext

class WallpaperCheckReceiver : BroadcastReceiver() {
    @Suppress("DEPRECATION")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) {
            Log.e("SafeMountain", "Intent: ${intent != null}, Context: ${context != null}")
            return
        }
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_WALLPAPER_CHANGED) {
            Log.e("SafeMountain", "Wrong Intent")
            return
        }
        val readable = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if (!readable) {
            Log.i("SafeMountain", "Impossible to read wallpaper.")
            return
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