package net.astrorbits.lib.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import org.bukkit.Bukkit
import java.time.Duration

object TitleHelper {
    fun broadcastTitle(title: Title) {
        Bukkit.getOnlinePlayers().forEach { it.showTitle(title) }
    }

    var defaultTimes: Times = times(10, 70, 20)

    fun title(title: Component, subtitle: Component = Component.empty(), times: Times = defaultTimes): Title {
        return Title.title(title, subtitle, defaultTimes)
    }

    fun times(fadeIn: Int, stay: Int, fadeOut: Int): Times {
        return Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))
    }
}