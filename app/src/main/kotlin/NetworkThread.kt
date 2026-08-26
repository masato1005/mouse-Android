import java.util.concurrent.LinkedBlockingQueue

class NetworkThread: Thread() {
    private var tasks = LinkedBlockingQueue<Runnable>()

    public override fun run(){
        try {
            while (!isInterrupted) {
                tasks.take().run()
            }
        } catch (_: InterruptedException) {
            interrupt()
        }
    }

    fun addTask(task:Runnable){
        println("addTask")
        tasks.put(task)
    }
}