package net.astrorbits.dontdoit

import net.astrorbits.dontdoit.team.TeamManager.TEAM_COLORS
import net.astrorbits.lib.config.*
import net.astrorbits.lib.text.TextHelper
import net.astrorbits.lib.text.TextHelper.bold
import net.astrorbits.lib.text.TextHelper.shadowColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object Configs {
    const val CONFIG_FILE_NAME = "game_settings.yml"

    val CONFIG: Config = Config(
        "game_settings",
        DontDoIt.instance.dataPath.resolve(CONFIG_FILE_NAME),
        CONFIG_FILE_NAME,
        DontDoIt.LOGGER
    )

    val TEAM_NAME: ParserMapConfigData<NamedTextColor, Component> = CONFIG.defineConfig(ParserMapConfigData(
        "team_name",
        TEAM_COLORS.inverse().mapValues { (color, name) -> Component.empty().append(Component.text(name).color(color).bold().shadowColor(0xa0000000.toInt())) },
        { TEAM_COLORS[it]!! },
        { TextHelper.parseMiniMessage(it) }
    ))
    val SIDEBAR_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_title",
        "<shadow:#a0000000><gradient:#ff0000:#EF903E>※ <bold>禁止事项</bold> ※"
    ))
    val SIDEBAR_ENTRY_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry.name",
        "{team_name}<white><shadow:#a0aa0000>[<red>❤</red><white>{life:%02d}]"
    ))
    val SIDEBAR_ENTRY_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry.number",
        "<white><shadow:#a0000000>{criteria}"
    ))
    val SIDEBAR_ENTRY_DEAD_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry_dead.name",
        "<strikethrough>{team_name}<gray><shadow:#a0000000>[\uD83D\uDC9400]"
    ))
    val SIDEBAR_ENTRY_DEAD_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar_entry_dead.number",
        "<gray><shadow:#a0000000>已淘汰"
    ))

    val DEFAULT_LIFE_COUNT: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "default_life_count",
        10
    ))

    fun getTeamName(color: NamedTextColor): Component? {
        return TEAM_NAME.get()[color]
    }

    fun init() {
        // static init
    }
}