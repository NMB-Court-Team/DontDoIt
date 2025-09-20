package net.astrorbits.doNotDoIt.team

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

enum class TeamColor(val material: Material, val color: NamedTextColor) {
    LIGHT_GRAY(Material.LIGHT_GRAY_WOOL, NamedTextColor.GRAY),
    RED(Material.RED_WOOL, NamedTextColor.RED),
    ORANGE(Material.ORANGE_WOOL, NamedTextColor.GOLD),
    YELLOW(Material.YELLOW_WOOL, NamedTextColor.YELLOW),
    LIME(Material.LIME_WOOL, NamedTextColor.GREEN),
    GREEN(Material.GREEN_WOOL, NamedTextColor.DARK_GREEN),
    CYAN(Material.CYAN_WOOL, NamedTextColor.DARK_AQUA),
    LIGHT_BLUE(Material.LIGHT_BLUE_WOOL, NamedTextColor.AQUA),
    PURPLE(Material.PURPLE_WOOL, NamedTextColor.DARK_PURPLE);

    fun next(): TeamColor {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }

    companion object {
        fun contains(material: Material): Boolean =
            entries.any { it.material == material }
    }
}