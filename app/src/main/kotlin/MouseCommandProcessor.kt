import EventType.DataType
import EventType.MouseEventType
import EventType.WallType
import Json.InputConvertedData
import android.graphics.Point
import com.fasterxml.jackson.databind.ObjectMapper
import com.momos.mouseandroid.MouseAccessibilityService
import data.MouseData
import interfaces.AccessibilityStateStore
import interfaces.CallSendData
import android.os.SystemClock
import kotlin.math.abs

class MouseCommandProcessor(val callSendData: CallSendData) {
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
    private val wallRange: Int = 10
    private val returnIgnoreMillis = 100L
    @Volatile
    private var ignoreMoveUntil = SystemClock.elapsedRealtime()
    private var tapTime = SystemClock.elapsedRealtime()
    private var leftClicked = false
    private var tapX = 0
    private var tapY = 0

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
                val mouseEventType = mouseData.getMouseEventType()

                if (mouseEventType == MouseEventType.MOVE &&
                    SystemClock.elapsedRealtime() < ignoreMoveUntil
                ) {
                    return
                }

                when (mouseEventType) {
                    MouseEventType.MOVE -> {
                        if(leftClicked){
                            draw()
                        }else{
                            move()
                        }
                    }


                    MouseEventType.LEFT_CLICK -> leftClick()
                    MouseEventType.RIGHT_CLICK -> rightClick()
                    MouseEventType.WHEEL_CLICK -> notDefine()
                    MouseEventType.DRAG -> notDefine()
                    MouseEventType.WHEEL_MOVE -> wheelMove()
                    MouseEventType.TOUCH_WALL -> notDefine()
                    MouseEventType.SEND_MOUSE -> notDefine()
                    MouseEventType.CLOSE_INVISIBLE_WINDOW -> notDefine()
                    null -> notDefine()
                }
            }

            DataType.KEYBOARD -> notDefine()
            DataType.WALL_TYPE -> setWallType(mapper.treeToValue(receiveData.data, WallType::class.java))

            DataType.SYSTEM_EXIT -> notDefine()
            null -> notDefine()
        }
    }

    fun notDefine(){
        return
    }

    fun setWallType(wallType:WallType){
    when(wallType){
        WallType.NORTH -> this.wallType = WallType.SOUTH
        WallType.SOUTH -> this.wallType = WallType.NORTH
        WallType.WEST -> this.wallType = WallType.EAST
        WallType.EAST -> this.wallType = WallType.WEST
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
                    y = 0
                }

                WallType.SOUTH -> {
                    x = mouseData.mouseX.coerceIn(0, screenWidthSize - 1)
                    y = screenHeightSize - 1
                }

                WallType.EAST -> {
                    x = 0
                    y = mouseData.mouseY.coerceIn(0, screenHeightSize - 1)
                }
            }
        }
        if (checkTouchWall()) {
            ignoreMoveUntil =
                SystemClock.elapsedRealtime() + returnIgnoreMillis
            MouseAccessibilityService.instance?.hideCursor()
            mouseData.setMouseEventType(MouseEventType.TOUCH_WALL)
            mouseData.mouseX = x
            mouseData.mouseY = y
            callSendData.callSendData(DataType.MOUSE,mouseData)

        } else {
            MouseAccessibilityService.instance?.draw(x, y)
        }

    }

    private fun screenRotation() {
        x = screenWidthSize / 2
        y = screenHeightSize / 2
    }


    fun checkTouchWall(): Boolean {
        when (wallType) {
            WallType.NORTH -> {
                return if (y < wallRange) {
                    if(!justGetMouse) {
                        haveMouse = false
                        true
                    }else{
                        false
                    }
                } else {
                    justGetMouse = false
                    false
                }
            }

            WallType.SOUTH -> {
                return if (y > screenHeightSize - wallRange && !justGetMouse) {
                    haveMouse = false
                    true
                } else {
                    justGetMouse = false
                    false
                }
            }

            WallType.WEST -> {
                return if (x > screenWidthSize - wallRange && !justGetMouse) {
                    haveMouse = false
                    true
                } else {
                    justGetMouse = false
                    false
                }
            }

            WallType.EAST -> {
                return if (x < wallRange && !justGetMouse) {
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
        val CHENGE_EO_SWIPE_RANGE = 5
        if (mouseData.isPressed) {
            tapTime = SystemClock.elapsedRealtime()
            leftClicked = true
            tapX = x
            tapY = y
        }
        if(!mouseData.isPressed) {
            if(abs(tapX - x) <= CHENGE_EO_SWIPE_RANGE && abs(tapY - y) <= CHENGE_EO_SWIPE_RANGE){
                MouseAccessibilityService.instance?.tap(x, y)
                leftClicked = false
            }else{
                MouseAccessibilityService.instance?.swipe(tapX,tapY,x, y)
                leftClicked = false
            }
        }
    }

    fun draw(){
        x += mouseData.dx
        if (x >= screenWidthSize) x = screenWidthSize - 1
        if (x < 0) x = 0
        y += mouseData.dy
        if (y >= screenHeightSize) y = screenHeightSize - 1
        if (y < 0) y = 0
        MouseAccessibilityService.instance?.draw(x, y)
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

    fun errorOccurred(){
        MouseAccessibilityService.instance?.hideCursor()
    }
}
