package net.astrorbits.dontdoit

import net.astrorbits.dontdoit.system.DiamondBehavior
import net.astrorbits.dontdoit.system.generate.BlockGenerationConfigData
import net.astrorbits.dontdoit.system.team.TeamManager.TEAM_COLORS
import net.astrorbits.lib.config.*
import net.astrorbits.lib.text.TextHelper
import net.astrorbits.lib.text.TextHelper.bold
import net.astrorbits.lib.text.TextHelper.shadowColor
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material

object Configs {
    const val CONFIG_FILE_NAME = "game_settings.yml"

    val CONFIG: Config = Config(
        "game_settings",
        DontDoIt.instance.dataPath.resolve(CONFIG_FILE_NAME),
        CONFIG_FILE_NAME,
        DontDoIt.LOGGER
    )

    // 游戏文本
    val TEAM_NAME: ParserMapConfigData<NamedTextColor, Component> = CONFIG.defineConfig(ParserMapConfigData(
        "team_name",
        TEAM_COLORS.inverse().mapValues { (color, name) -> Component.empty().append(Component.text(name).color(color).bold().shadowColor(0xa0000000.toInt())) },
        { TEAM_COLORS[it]!! },
        { TextHelper.parseMiniMessage(it) }
    ))

    val SIDEBAR_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.title",
        "<shadow:#a0000000><gradient:#ff0000:#EF903E>※ <bold>禁止事项</bold> ※"
    ))
    val SIDEBAR_ENTRY_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry.name",
        "{team_name}<white><shadow:#a0aa0000>[<red>❤</red><white>{life:%02d}]"
    ))
    val SIDEBAR_ENTRY_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry.number",
        "<white><shadow:#a0000000>{criteria}"
    ))
    val SIDEBAR_ENTRY_DEAD_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry_dead.name",
        "<strikethrough>{team_name}<gray><shadow:#a0000000>[\uD83D\uDC9400]"
    ))
    val SIDEBAR_ENTRY_DEAD_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry_dead.number",
        "<gray><shadow:#a0000000>已淘汰"
    ))

    val CHANGE_TEAM_ITEM_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "change_team_item_name",
        "<italic:false><aqua>[<key:key.use>]<yellow>切换队伍"
    ))
    val MODIFY_CUSTOM_CRITERIA_ITEM_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "modify_custom_criteria_item_name",
        "<italic:false><aqua>[<key:key.use>]<yellow>设置自定义词条"
    ))
    val JOIN_SPECTATOR: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "join_spectator",
        "<yellow>你现在是<gray><bold>旁观者"
    ))
    val JOIN_TEAM: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "join_team",
        "<yellow>你加入了队伍 {team_name}"
    ))

    val MODIFY_CUSTOM_CRITERIA_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "modify_custom_criteria.title",
        "<aqua><bold>设置自定义词条"
    ))
    val MODIFY_CUSTOM_CRITERIA_BODY: MultilineTextConfigData = CONFIG.defineConfig(MultilineTextConfigData(
        "modify_custom_criteria.body",
        listOf(
            "<white>在下方的文本框修改自定义词条内容",
            "<white>然后点击“确认”按钮以保存修改",
            ""
        )
    ))
    val MODIFY_CUSTOM_CRITERIA_TEXT_BOX_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "modify_custom_criteria.text_box_title",
        "<yellow>自定义词条内容"
    ))
    val MODIFY_CUSTOM_CRITERIA_NO_BUTTON_LABEL: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "modify_custom_criteria.no_button_label",
        "<white>取消"
    ))
    val MODIFY_CUSTOM_CRITERIA_YES_BUTTON_LABEL: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "modify_custom_criteria.yes_button_label",
        "<white>确认"
    ))

    private fun parseMaterial(id: String): Material {
        return Material.matchMaterial(id) ?: throw IllegalArgumentException("Unknown item: $id")
    }

    // 物品
    val JOIN_TEAM_ITEM: ParserMapConfigData<NamedTextColor, Material> = CONFIG.defineConfig(ParserMapConfigData(
        "join_team_item",
        TEAM_COLORS.inverse().mapValues { Material.STICK },
        { TEAM_COLORS[it]!! },
        ::parseMaterial
    ))
    val SPECTATOR_ITEM: ParserConfigData<Material> = CONFIG.defineConfig(ParserConfigData(
        "spectator_item",
        Material.LIGHT_GRAY_WOOL,
        ::parseMaterial
    ))
    val MODIFY_CUSTOM_CRITERIA_ITEM: ParserConfigData<Material> = CONFIG.defineConfig(ParserConfigData(
        "modify_custom_criteria_item",
        Material.WRITABLE_BOOK,
        ::parseMaterial
    ))

    // 方块生成配置
    val ANDESITE_GENERATION: BlockGenerationConfigData = CONFIG.defineConfig(BlockGenerationConfigData(
        "andesite_generation",
        Material.ANDESITE,
        emptyList()
    ))
    val COAL_ORE_GENERATION: BlockGenerationConfigData = CONFIG.defineConfig(BlockGenerationConfigData(
        "coal_ore_generation",
        Material.COAL_ORE,
        emptyList()
    ))
    val IRON_ORE_GENERATION: BlockGenerationConfigData = CONFIG.defineConfig(BlockGenerationConfigData(
        "iron_ore_generation",
        Material.IRON_ORE,
        emptyList()
    ))
    val DIAMOND_ORE_GENERATION: BlockGenerationConfigData = CONFIG.defineConfig(BlockGenerationConfigData(
        "diamond_ore_generation",
        Material.DIAMOND_ORE,
        emptyList()
    ))
    val BEDROCK_DEPTH: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "bedrock_depth",
        11
    ))

    // 全局设置默认值
    val GAME_AREA_SIZE: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "game_area_size",
        65
    ))
    val LIFE_COUNT: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "life_count",
        10
    ))
    val DIAMOND_BEHAVIOR: ParserConfigData<DiamondBehavior> = CONFIG.defineConfig(ParserConfigData(
        "diamond_behavior",
        DiamondBehavior.REDUCE_OTHERS_LIFE,
        { DiamondBehavior.valueOf(it.uppercase()) }
    ))


    fun getTeamName(color: NamedTextColor): Component {
        return TEAM_NAME.get()[color]!!
    }

    fun getJoinTeamItemMaterial(color: NamedTextColor): Material {
        return JOIN_TEAM_ITEM.get()[color]!!
    }

    fun init() {
        CONFIG.load()
    }
}