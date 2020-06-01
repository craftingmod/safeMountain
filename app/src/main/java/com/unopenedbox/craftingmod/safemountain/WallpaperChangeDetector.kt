package com.unopenedbox.craftingmod.safemountain

import android.app.*
import android.content.*
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperChangeDetector : Service() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("SafeMountain", "WallpaperChanged")
            if (intent.action == Intent.ACTION_WALLPAPER_CHANGED) {
                handler.sendEmptyMessage(0)
            }
        }
    }

    private val channelID = 91734;

    private lateinit var handler:Handler
    private lateinit var context:Context
    private lateinit var channel:NotificationChannel
    private lateinit var sp:SharedPreferences

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!sp.getBoolean("use_service", false)) {
            stopSelf()
        } else if (!Util.isIgnoringBO(this)) {
            startForeground(7341, Notification.Builder(this, channel.id).apply {
                setSmallIcon(R.drawable.ic_help)
                setContentTitle(getString(R.string.servicenoti_title))
                setContentText(getString(R.string.servicenoti_desc))
            }.build())
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("SafeMountain", "onDestory")
        stopForeground(true)
        context.unregisterReceiver(receiver)
        super.onDestroy()
    }
    override fun onCreate() {
        super.onCreate()
        channel = NotificationChannel("SafeMountainService", "SafeMountain", NotificationManager.IMPORTANCE_NONE)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        Log.d("SafeMountain", "onCreate")
        context = this
        sp = getSharedPreferences("setting", Context.MODE_PRIVATE)
        handler = Handler {
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
            true
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
