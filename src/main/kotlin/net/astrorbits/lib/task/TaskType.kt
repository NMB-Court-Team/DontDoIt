package net.astrorbits.lib.task

import net.astrorbits.lib.math.Duration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * 任务类型
 */
sealed class TaskType {
    abstract fun runTask(plugin: JavaPlugin, runnable: BukkitRunnable, isAsync: Boolean): BukkitTask

    /**
     * 普通任务，会无延迟立刻执行一次
     */
    data object Normal : TaskType() {
        override fun runTask(plugin: JavaPlugin, runnable: BukkitRunnable, isAsync: Boolean): BukkitTask {
            return if (!isAsync) runnable.runTask(plugin)
            else runnable.runTaskAsynchronously(plugin)
        }
    }

    /**
     * 延迟任务，会延迟[delay]时间后执行一次
     */
    class Delayed(val delay: Duration) : TaskType() {
        override fun runTask(plugin: JavaPlugin, runnable: BukkitRunnable, isAsync: Boolean): BukkitTask {
            return if (!isAsync) runnable.runTaskLater(plugin, delay.ticks.toLong())
                else runnable.runTaskLaterAsynchronously(plugin, delay.ticks.toLong())
        }
    }

    /**
     * 重复执行的延迟任务，会先延迟[delay]时间，然后每隔[interval]时间执行一次
     */
    open class RepeatDelayed(val delay: Duration, val interval: Duration) : TaskType() {
        override fun runTask(plugin: JavaPlugin, runnable: BukkitRunnable, isAsync: Boolean): BukkitTask {
            return if (!isAsync) runnable.runTaskTimer(plugin, delay.ticks.toLong(), interval.ticks.toLong())
                else runnable.runTaskTimerAsynchronously(plugin, delay.ticks.toLong(), interval.ticks.toLong())
        }
    }

    /**
     * 每tick执行一次的延迟任务，会先延迟[delay]时间，然后每tick执行一次
     */
    class TickDelayed(delay: Duration) : RepeatDelayed(delay, Duration.ONE_TICK)

    /**
     * 每秒执行一次的延迟任务，会先延迟[delay]时间，然后每秒执行一次
     */
    class SecondDelayed(delay: Duration): RepeatDelayed(delay, Duration.ONE_SECOND)

    /**
     * 重复执行的任务，每隔[interval]执行一次
     */
    open class Repeat(interval: Duration) : RepeatDelayed(Duration.ZERO, interval)

    /**
     * 每tick执行一次的任务
     */
    object Tick : Repeat(Duration.ONE_TICK)

    /**
     * 每秒执行一次的任务
     */
    object Second : Repeat(Duration.ONE_SECOND)
}
