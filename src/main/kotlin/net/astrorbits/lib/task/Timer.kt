package net.astrorbits.lib.task

import net.astrorbits.lib.math.Duration
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

abstract class Timer {
    private val plugin: JavaPlugin
    private var _currentTime: Int
    private var timerTask: BukkitTask? = null
    var timerType: TimerType
    private var _startTime: Int

    var currentTime: Duration
        get() = Duration.ticks(_currentTime.toDouble())
        set(value) {
            _currentTime = value.ticks.toInt()
        }
    var startTime: Duration
        get() = Duration.ticks(_startTime.toDouble())
        set(value) {
            _startTime = value.ticks.toInt()
        }

    constructor(plugin: JavaPlugin) {
        this.plugin = plugin
        _currentTime = 0
        _startTime = 0
        timerType = TimerType.TIMING
    }

    constructor(plugin: JavaPlugin, startTime: Duration, timerType: TimerType) {
        this.plugin = plugin
        _currentTime = startTime.ticks.toInt()
        this._startTime = _currentTime
        this.timerType = timerType
    }

    fun tick() {
        onTick()
        _currentTime += timerType.direction
        if (timerType == TimerType.COUNTDOWN && _currentTime == 0) {
            stop()
        }
    }

    fun isTicking(): Boolean {
        return timerTask != null && !timerTask!!.isCancelled
    }

    abstract fun onStart()

    abstract fun onTick()

    abstract fun onStop()

    fun start() {
        if (timerTask != null || (_currentTime == 0 && timerType === TimerType.COUNTDOWN)) {
            return
        }
        onStart()
        timerTask = object : BukkitRunnable() {
            override fun run() {
                tick()
            }
        }.runTaskTimer(plugin, 0, 1)
    }

    fun stop() {
        if (timerTask != null) {
            onStop()
            if (!(timerTask == null || timerTask!!.isCancelled)) {
                timerTask!!.cancel()
                timerTask = null
            }
        }
    }

    /**
     * 不执行[onStop]
     */
    fun stopDirectly() {
        if (timerTask != null) {
            timerTask!!.cancel()
            timerTask = null
        }
    }

    /**
     * 不执行[onStart]
     */
    fun startDirectly() {
        if (timerTask == null || (_currentTime == 0 && timerType === TimerType.COUNTDOWN)) {
            return
        }
        timerTask = object : BukkitRunnable() {
            override fun run() {
                tick()
            }
        }.runTaskTimer(plugin, 0, 1)
    }

    /**
     * 重置计时器，并且会停止计时器（不会执行[onStop]方法）
     */
    fun reset() {
        if (timerTask != null) {
            timerTask!!.cancel()
            timerTask = null
        }
        _currentTime = _startTime
    }

    fun resetAndStart() {
        reset()
        start()
    }
}