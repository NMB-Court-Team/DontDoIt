package net.astrorbits.lib.task

import net.astrorbits.lib.math.Duration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * 任务执行构筑器，基于Bukkit的[BukkitRunnable]和[BukkitTask]
 *
 * 任务执行逻辑见方法[runTask]
 *
 * 注意：受[BukkitRunnable]的限制，所有[Duration]的最小单位都是tick
 * @param plugin 执行任务的插件
 */
class TaskBuilder(val plugin: JavaPlugin, type: TaskType = TaskType.Normal) {
    var onStart: () -> Unit = EMPTY_TASK
    var task: () -> Unit = EMPTY_TASK
    var isAsync: Boolean = false
    var taskType: TaskType = type
    var stopPredicate: StopPredicate = NEVER_STOP_PREDICATE
    var onStop: () -> Unit = EMPTY_TASK

    /**
     * 设置任务
     */
    fun setTask(task: () -> Unit): TaskBuilder = apply { this.task = task }

    /**
     * 设置为异步任务
     */
    fun setAsync(): TaskBuilder = apply { isAsync = true }

    /**
     * 设置为同步任务
     */
    fun setSync(): TaskBuilder = apply { isAsync = false }

    /**
     * 设置任务类型
     */
    fun setType(type: TaskType): TaskBuilder = apply { this.taskType = type }

    /**
     * 设置任务停止谓词
     */
    fun setStopPredicate(predicate: StopPredicate): TaskBuilder = apply { this.stopPredicate = predicate }

    /**
     * 设置根据重复次数来停止
     * @param count 重复次数，任务重复次数达到[count]时停止执行
     */
    fun setStopByRepeatCount(count: Int): TaskBuilder = setStopPredicate(counterPredicate(count))

    /**
     * 设置根据运行时长来停止
     * @param duration 运行时长，任务运行时间超过[duration]时停止执行
     */
    fun setStopByTime(duration: Duration): TaskBuilder = setStopPredicate(timerPredicate(duration))

    /**
     * 设置当任务开始时执行的内容
     */
    fun setOnStart(onStart: () -> Unit): TaskBuilder = apply { this.onStart = onStart }

    /**
     * 设置任务停止时执行的内容
     */
    fun setOnStop(onStop: () -> Unit): TaskBuilder = apply { this.onStop = onStop }

    /**
     * 开始执行任务
     */
    fun runTask(): BukkitTask {
        val runnable = object : BukkitRunnable() {
            private var initialized: Boolean = false
            override fun run() {
                if (!initialized) {
                    stopPredicate.init()
                    onStart()
                    initialized = true
                }

                if (stopPredicate.shouldStop()) {
                    onStop()
                    cancel()
                    return
                }
                task()
                stopPredicate.repeat()
            }
        }
        return taskType.runTask(plugin, runnable, isAsync)
    }

    companion object {
        val EMPTY_TASK: () -> Unit = {}
        val NEVER_STOP_PREDICATE: StopPredicate = StopPredicate { false }

        fun counterPredicate(count: Int): StopPredicate {
            return object : StopPredicate {
                private var c = 0
                override fun repeat() { c += 1 }
                override fun shouldStop(): Boolean = c >= count
            }
        }

        fun timerPredicate(duration: Duration): StopPredicate {
            return object : StopPredicate {
                private var startTime: Long = 0
                override fun init() { startTime = System.currentTimeMillis() }
                override fun shouldStop(): Boolean = Duration.tillNow(startTime) >= duration
            }
        }
    }

    /**
     * 任务停止谓词
     */
    fun interface StopPredicate {
        /**
         * 是否应该停止任务，在任务执行前判定
         */
        fun shouldStop(): Boolean

        /**
         * 初始化谓词，先于[onStart]调用
         */
        fun init() { }

        /**
         * 此方法会在每次执行完任务后调用一次，可以用来迭代谓词保存的数据
         */
        fun repeat() { }
    }
}