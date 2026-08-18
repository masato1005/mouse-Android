import EventType.DataType
import EventType.MouseEventType
import EventType.WallType
import Json.InputConvertedData
import android.graphics.Point
import com.fasterxml.jackson.databind.ObjectMapper
import com.momos.mouseandroid.MouseAccessibilityService
import data.MouseData
import interfaces.AccessibilityStateStore

class MouseCommandProcessor {
    private var service: MouseAccessibilityService? = null
    private val mapper = ObjectMapper()
    private var dataType: DataType? = null
    private lateinit var mouseData: MouseData
    private var screenWidthSize: Int = 0
    private var screenHeightSize: Int = 0
    private var x: Int = 0
    private var y: Int = 0
    private var haveMouse: Boolean = false
    private var justGetMouse = false
    private var wallType = WallType.EAST
    private val wallRange: Int = 5


    fun serviceConnect() {
        val screenSize = AccessibilityStateStore.getScreenSize()
        if (screenSize != null) {
            updateScreenSize(screenSize)
        }

    }

    fun updateScreenSize(screenSize: Point) {
        if (screenSize.x == screenWidthSize &&
            screenSize.y == screenHeightSize
        ) {
            return
        } else {
            screenWidthSize = screenSize.x
            screenHeightSize = screenSize.y
            screenRotation()
        }
    }

    fun receiveData(receiveData: InputConvertedData) {
        dataType = receiveData.dataType
        when (dataType) {
            DataType.MOUSE -> {
                mouseData = mapper.treeToValue(receiveData.data, MouseData::class.java)

                when (mouseData.getMouseEventType()) {
                    MouseEventType.MOVE -> move()
                    MouseEventType.LEFTCLICK -> leftClick()
                    MouseEventType.RIGHTCLICK -> rightClick()
                    MouseEventType.WHEELCLICK -> TODO()
                    MouseEventType.DRAG -> TODO()
                    MouseEventType.WHEELMOVE -> wheelMove()
                    MouseEventType.TOUCHWALL -> TODO()
                    MouseEventType.SENDMOUSE -> TODO()
                    MouseEventType.CLOSEINVISIBLEWINDOW -> TODO()
                    null -> TODO()
                }
            }

            DataType.KEYBOARD -> TODO()
            DataType.WALLTYPE -> wallType =
                mapper.treeToValue(receiveData.data, WallType::class.java)

            DataType.SYSTEMEXIT -> TODO()
            null -> TODO()
        }
    }

    private fun move() {
        val screenSize = AccessibilityStateStore.getScreenSize()
        if (screenSize != null) {
            updateScreenSize(screenSize)
        }
        if (haveMouse) {
            x += mouseData.dx
            if (x >= screenWidthSize) x = screenWidthSize - 1
            if (x < 0) x = 0
            y += mouseData.dy
            if (y >= screenHeightSize) y = screenHeightSize - 1
            if (y < 0) y = 0
        } else {
            haveMouse = true
            justGetMouse = true
            when (wallType) {
                WallType.WEST -> {
                    x = screenWidthSize - 1
                    y = mouseData.mouseY.coerceIn(0, screenHeightSize - 1)
                }

                WallType.NORTH -> {
                    x = mouseData.mouseX.coerceIn(0, screenWidthSize - 1)
                    y = screenHeightSize - 1
                }

                WallType.SOUTH -> {
                    x = mouseData.mouseX.coerceIn(0, screenWidthSize - 1)
                    y = 0
                }

                WallType.EAST -> {
                    x = 0
                    y = mouseData.mouseY.coerceIn(0, screenHeightSize - 1)
                }
            }
        }
        if (checkTouchWall()) {
            MouseAccessibilityService.instance?.hideCursor()
        } else {
            MouseAccessibilityService.instance?.draw(x, y)
        }

    }

    private fun screenRotation() {
        x = screenWidthSize / 2
        y = screenHeightSize / 2
    }


    fun checkTouchWall(): Boolean {
        if (justGetMouse) return false
        when (wallType) {
            WallType.NORTH -> {
                return if (y < wallRange) {
                    haveMouse = false
                    true
                } else {
                    justGetMouse = false
                    false
                }
            }

            WallType.SOUTH -> {
                return if (y > screenHeightSize - wallRange) {
                    haveMouse = false
                    true
                } else {
                    justGetMouse = false
                    false
                }
            }

            WallType.WEST -> {
                return if (x > screenWidthSize - wallRange) {
                    haveMouse = false
                    true
                } else {
                    justGetMouse = false
                    false
                }
            }

            WallType.EAST -> {
                return if (x < wallRange) {
                    haveMouse = false
                    true
                } else {
                    justGetMouse = false
                    false
                }
            }
        }
    }

    private fun leftClick() {
        if (mouseData.isPressed) MouseAccessibilityService.instance?.tap(x, y)
    }

    private fun rightClick() {
        if (mouseData.isPressed) MouseAccessibilityService.instance?.longTap(x, y)
    }

    private fun wheelMove() {
        val service =
            MouseAccessibilityService.instance ?: return

        val density =
            service.resources.displayMetrics.density

        val distance =
            (-mouseData.wheelAmount * 80f * density).toInt()

        service.scroll(x = x, y = y, deltaY = distance, duration = 150L)
    }

}
