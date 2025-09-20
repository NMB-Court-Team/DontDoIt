package net.astrorbits.doNotDoIt

import net.astrorbits.doNotDoIt.team.TeamManager.TEAM_COLORS
import net.astrorbits.lib.config.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object Configs {
    const val CONFIG_FILE_NAME = "game_settings.yml"

    val CONFIG: Config = Config(
        "game_settings",
        DoNotDoIt.instance.dataPath.resolve(CONFIG_FILE_NAME),
        CONFIG_FILE_NAME,
        DoNotDoIt.LOGGER
    )

    val TEAM_NAME: ParserMapConfigData<NamedTextColor, String> = CONFIG.defineConfig(ParserMapConfigData(
        "team_name",
        TEAM_COLORS.inverse(),
        { TEAM_COLORS[it]!! },
        { it }
    ))
    val SIDEBAR_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_title",
        "<gradient:#ff0000:#EF903E>※ <bold>禁止事项</bold> ※"
    ))
    val SIDEBAR_ENTRY_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry.name",
        "<bold>{team_name}</bold><red>[❤{life}]"
    ))
    val SIDEBAR_ENTRY_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry.number",
        "<white>{criteria}"
    ))
    val SIDEBAR_ENTRY_DEAD_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry_dead.name",
        "<strikethrough><bold>{team_name}</bold><gray>[\uD83D\uDC9400]</strikethrough>"
    ))
    val SIDEBAR_ENTRY_DEAD_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry_dead.number",
        "<gray>已淘汰"
    ))

    val DEFAULT_LIFE_COUNT: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "default_life_count",
        10
    ))

    fun getTeamName(color: NamedTextColor): Component? {
        val teamNameString = TEAM_NAME.get()[color] ?: return null
        return Component.text(teamNameString).color(color)
    }

    fun init() {
        // static init
    }
}