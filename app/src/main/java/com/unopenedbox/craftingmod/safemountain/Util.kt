package com.unopenedbox.craftingmod.safemountain

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.android.systemui.glwallpaper.ImageProcessHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Util {
    companion object {
        suspend fun checkWallpaper(context:Context):Boolean {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return true
            }
            val wallpaperMgr = WallpaperManager.getInstance(context)
            val bitmap = withContext(Dispatchers.IO) {
                val pfd = wallpaperMgr.getWallpaperFile(WallpaperManager.FLAG_LOCK) ?: wallpaperMgr.getWallpaperFile(
                    WallpaperManager.FLAG_SYSTEM)
                BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
            }
            return withContext(Dispatchers.Default) {
                ImageProcessHelper.Threshold.isSafe(bitmap)
            }
        }
        fun isIgnoringBO(context:Context):Boolean {
            return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)
        }
    }
}