package interfaces

interface MouseActionExecutor {
    fun draw(x: Int, y: Int): Boolean
    fun tap(x: Int, y: Int): Boolean
    fun longTap(x: Int, y: Int): Boolean
    fun scroll(x: Int, y: Int, deltaY: Int, duration: Long): Boolean

}