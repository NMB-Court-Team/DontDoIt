package net.astrorbits.lib.task

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

object TaskHelper {
    fun runForceSync(plugin: JavaPlugin, run: () -> Unit) {
        Bukkit.getScheduler().runTask(plugin, run)
    }
}