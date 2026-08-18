package data

import EventType.MouseEventType

class MouseData {
    private var mouseEventType: MouseEventType? = null

    var mouseX: Int = 0
    var mouseY: Int = 0
    var dx: Int = 0
    var dy: Int = 0
    var wheelAmount: Int = 0

    var isPressed: Boolean = false

    private var modifiers: Modifiers? = null

    constructor()

    constructor(
        mouseEventType: MouseEventType?,
        mouseX: Int,
        mouseY: Int,
        dx: Int,
        dy: Int,
        wheelAmount: Int,
        pressed: Boolean,
        modifiers: Modifiers?
    ) {
        this.mouseEventType = mouseEventType
        this.mouseX = mouseX
        this.mouseY = mouseY
        this.dx = dx
        this.dy = dy
        this.wheelAmount = wheelAmount
        this.isPressed = pressed
        this.modifiers = modifiers
    }

    fun getMouseEventType(): MouseEventType? {
        return mouseEventType
    }

    fun setMouseEventType(mouseEventType: MouseEventType?) {
        this.mouseEventType = mouseEventType
    }

    fun getModifiers(): Modifiers? {
        return modifiers
    }

    fun setModifiers(modifiers: Modifiers?) {
        this.modifiers = modifiers
    }
}
