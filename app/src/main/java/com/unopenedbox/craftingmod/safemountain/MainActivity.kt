package com.unopenedbox.craftingmod.safemountain

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.android.systemui.glwallpaper.ImageProcessHelper
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var serviceCheck:CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                MaterialDialog(this).show {
                    title(R.string.perm_title)
                    message(R.string.perm_desc)
                    positiveButton(android.R.string.yes) {
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1523);
                    }
                    negativeButton(android.R.string.no) {
                        finish()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1523);
            }
        } else {
            GlobalScope.launch {
                fixWallpaper()
            }
        }
        findViewById<TextView>(R.id.creditText).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/craftingmod/safeMountain")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }
        val sp = getSharedPreferences("setting", Context.MODE_PRIVATE)
        serviceCheck = findViewById<CheckBox>(R.id.service_checkbox)
        val useService = sp.getBoolean("use_service", false)
        serviceCheck.isChecked = useService
        if (useService) {
            startService(Intent(this, WallpaperChangeDetector::class.java))
        }
        serviceCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!sp.getBoolean("advised_BO", false) && !Util.isIgnoringBO(this)) {
                    MaterialDialog(this).show {
                        message(R.string.suggest_battery_optimize)
                        positiveButton(android.R.string.ok) {
                            sp.edit().putBoolean("advised_BO", true).apply()
                            sp.edit().putBoolean("use_service", true).apply()
                            startService(Intent(this@MainActivity, WallpaperChangeDetector::class.java))
                        }
                        negativeButton(R.string.bo_button) {
                            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                            serviceCheck.isChecked = false;
                        }
                        onCancel {
                            serviceCheck.isChecked = false;
                        }
                    }
                } else {
                    sp.edit().putBoolean("use_service", true).apply()
                    startService(Intent(this, WallpaperChangeDetector::class.java))
                }
            } else {
                sp.edit().putBoolean("use_service", false).apply()
                stopService(Intent(this, WallpaperChangeDetector::class.java))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1523 -> GlobalScope.launch {
                fixWallpaper()
            }
            else -> finish()
        }
    }

    suspend fun fixWallpaper() {
        val isSafe = checkWallpaper()
        Log.d("SafeMountain", "Wallpaper is safe: $isSafe")
        if (!isSafe) {
            val wallpaperMgr = WallpaperManager.getInstance(this@MainActivity)
            wallpaperMgr.clear(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
        }
    }

    suspend fun checkWallpaper():Boolean {
        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return true
        }
        val wallpaperMgr = WallpaperManager.getInstance(this)
        val bitmap = withContext(Dispatchers.IO) {
            val pfd = wallpaperMgr.getWallpaperFile(WallpaperManager.FLAG_LOCK) ?: wallpaperMgr.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)
            BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
        }
        val isSafe = withContext(Dispatchers.Default) {
            ImageProcessHelper.Threshold.isSafe(bitmap)
        }
        return isSafe
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> MaterialDialog(this).show {
                title(R.string.help_title)
                message(R.string.help_desc)
                positiveButton(android.R.string.ok)
                negativeButton(R.string.button_where) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/UniverseIce/status/1266943909499826176")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    })
                }
            }
            R.id.action_osl -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/craftingmod/safeMountain/blob/master/OSL.md")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                })
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        finish()
    }

}
