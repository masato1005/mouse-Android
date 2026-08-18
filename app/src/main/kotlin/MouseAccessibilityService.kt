package com.momos.mouseandroid

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import interfaces.AccessibilityStateStore
import interfaces.MouseActionExecutor

@SuppressLint("AccessibilityPolicy")
class MouseAccessibilityService : AccessibilityService(), MouseActionExecutor {
    lateinit var windowManager: WindowManager
    var cursorView: View? = null
    var cursorParams: WindowManager.LayoutParams? = null
    val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        windowManager =
            getSystemService(WindowManager::class.java)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AccessibilityStateStore.setScreenSize(getScreenSize())
    }

    override fun onConfigurationChanged(
        newConfig: Configuration
    ) {
        super.onConfigurationChanged(newConfig)
        AccessibilityStateStore.setScreenSize(getScreenSize())
    }

    companion object {
        @Volatile
        var instance: MouseAccessibilityService? = null
            private set
    }

    private fun getScreenSize(): Point {
        val windowManager = getSystemService(WindowManager::class.java)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds =
                windowManager.currentWindowMetrics.bounds

            Point(
                bounds.width(),
                bounds.height()
            )
        } else {
            @Suppress("DEPRECATION")
            Point().also { size ->
                windowManager.defaultDisplay.getRealSize(size)
            }
        }
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun draw(x: Int, y: Int): Boolean {
        mainHandler.post {
            if (cursorView == null) {
                val density = resources.displayMetrics.density
                val size = (24f * density).toInt()

                val cursor = View(this).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.argb(180, 30, 144, 255))
                        setStroke(
                            (2f * density).toInt(),
                            Color.WHITE
                        )
                    }
                }

                val params = WindowManager.LayoutParams(
                    size,
                    size,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                }
                windowManager.addView(cursor, params)

                cursorView = cursor
                cursorParams = params
            }

            val view = cursorView ?: return@post
            val params = cursorParams ?: return@post

            params.x = x - view.width / 2
            params.y = y - view.height / 2

            windowManager.updateViewLayout(
                view,
                params
            )
        }

        return true
    }


    fun hideCursor() {
        mainHandler.post {
            cursorView?.let { view ->
                windowManager.removeView(view)
            }
            cursorView = null
            cursorParams = null
        }
    }

    override fun tap(x: Int, y: Int): Boolean {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 50L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        val accepted = dispatchGesture(gesture, null, null)
        return accepted
    }

    override fun longTap(x: Int, y: Int): Boolean {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 2000L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        val accepted = dispatchGesture(gesture, null, null)
        return accepted
    }


    override fun scroll(x: Int, y: Int, deltaY: Int, duration: Long): Boolean {
        val screenSize = AccessibilityStateStore.getScreenSize() ?: return false
        val endY = (y + deltaY)
            .coerceIn(0, screenSize.y - 1)

        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
            lineTo(x.toFloat(), endY.toFloat())
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0,
                    duration
                )
            )
            .build()
        return dispatchGesture(gesture, null, null)
    }
}


fun Context.isAccessibilityServiceEnabled(
    serviceClass: Class<out AccessibilityService>
): Boolean {
    val manager = getSystemService(AccessibilityManager::class.java)
    val targetService = ComponentName(this, serviceClass)

    return manager
        .getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        .any { accessibilityServiceInfo ->
            val serviceInfo =
                accessibilityServiceInfo.resolveInfo.serviceInfo

            val enabledService = ComponentName(
                serviceInfo.packageName,
                serviceInfo.name
            )
            enabledService == targetService
        }
}
