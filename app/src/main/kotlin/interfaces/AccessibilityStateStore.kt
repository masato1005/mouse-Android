package interfaces

import android.graphics.Point

object AccessibilityStateStore {
    private var screenSize: Point? = null

    fun setScreenSize(screenSize: Point) {
        this.screenSize = screenSize
    }

    fun getScreenSize(): Point? {
        return screenSize

    }
}