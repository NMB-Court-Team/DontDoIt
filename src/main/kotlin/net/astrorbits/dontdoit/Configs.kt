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

    //region 游戏文本
    val TEAM_NAME: ParserMapConfigData<NamedTextColor, Component> = CONFIG.defineConfig(ParserMapConfigData(
        "team_name",
        TEAM_COLORS.inverse().mapValues { (color, name) -> Component.empty().append(Component.text(name).color(color).bold().shadowColor(0xa0000000.toInt())) },
        { TEAM_COLORS[it]!! },
        { TextHelper.parseMiniMessage(it) }
    ))

    val CHANGE_TEAM_ITEM_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "change_team_item_name",
        "<italic:false><aqua>[<key:key.use>]<yellow>切换队伍"
    ))
    val MODIFY_CUSTOM_CRITERIA_ITEM_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "modify_custom_criteria_item_name",
        "<italic:false><aqua>[<key:key.use>]<yellow>设置自定义词条"
    ))
    val START_GAME_ITEM_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "start_game_item_name",
        "<italic:false><aqua>[<key:key.use>]<gold>开始游戏！"
    ))

    val CUSTOM_CRITERIA_NAME_PREFIX: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "custom_criteria_name_prefix",
        "[自定义]"
    ))
    val DIAMOND_BEHAVIOR_DESCRIPTION: ParserMapConfigData<DiamondBehavior, String> = CONFIG.defineConfig(ParserMapConfigData(
        "diamond_behavior_description",
        mapOf(
            DiamondBehavior.REDUCE_OTHERS_LIFE to "减少其他队伍1点生命值",
            DiamondBehavior.ADD_SELF_LIFE to "增加自己队伍1点生命值"
        ),
        { DiamondBehavior.valueOf(it.uppercase()) },
        { it }
    ))

    //region 准备阶段的消息
    val JOIN_SPECTATOR_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "join_spectator",
        "<yellow>你现在是<gray><bold>旁观者"
    ))
    val JOIN_TEAM_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "join_team",
        "<yellow>你加入了队伍 {team_name}"
    ))
    val CURRENT_CUSTOM_CRITERIA_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "current_custom_criteria_message",
        "<yellow>你的队伍的自定义词条：{name}"
    ))
    val SELF_SET_CUSTOM_CRITERIA_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "self_set_custom_criteria_message",
        "<yellow>已将自定义词条设置为：{name}"
    ))
    val OTHER_SET_CUSTOM_CRITERIA_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "other_set_custom_criteria_message",
        "<yellow>{player}将自定义词条设置为：{name}"
    ))
    val NOT_SET_CUSTOM_CRITERIA_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "not_set_custom_criteria_message",
        "<red>你的队伍尚未设置自定义词条"
    ))
    val SELF_REMOVE_CUSTOM_CRITERIA_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "self_remove_custom_criteria_message",
        "<yellow>已取消自己队伍定义的自定义词条"
    ))
    val OTHER_REMOVE_CUSTOM_CRITERIA_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "other_remove_custom_criteria_message",
        "<yellow>{player}取消了你的队伍定义的自定义词条"
    ))
    val ENTER_PREPARE_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "enter_prepare_message",
        "<yellow>请使用手上的物品<green><bold>选择队伍</bold><yellow>，然后设置<green><bold>自定义词条"
    ))
    //endregion

    //region 游戏切换阶段的消息
    val START_GAME_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "start_game_message",
        "<green><bold>游戏开始！"
    ))
    val END_GAME_ANNOUNCE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "end_game_message.announce",
        "{team_name} <gold>获得胜利"
    ))
    val END_GAME_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "end_game_message.title",
        "{team_name}"
    ))
    val END_GAME_SUBTITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "end_game_message.subtitle",
        "<gold>获得胜利"
    ))
    val DRAW_GAME_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "draw_game_message",
        "<red>无人获胜"
    ))
    val RESET_GAME_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "reset_game_message",
        "<yellow>游戏已重置"
    ))
    //endregion

    //region 触发次数统计
    val TRIGGER_COUNT_HEAD: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "trigger_count_message.head",
        "<gray>=====<yellow>触发次数<gray>====="
    ))
    val TRIGGER_COUNT_BODY: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "trigger_count_message.body",
        "{player}<white>: <green>{count}"
    ))
    val TRIGGER_COUNT_TAIL: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "trigger_count_message.tail",
        "<gray>================"
    ))
    //endregion

    //region 游戏进行时的消息
    val INGAME_ACTIONBAR: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "ingame_actionbar",
        "<green>距离更换词条还有：<red>{time}秒  <green>剩余规则数量：<red>{life_count}"
    ))
    val CRITERIA_TRIGGERED_ANNOUNCE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "criteria_triggered_message.announce",
        "{who_triggered} <gold>触发了规则 {criteria}"
    ))
    val CRITERIA_TRIGGERED_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "criteria_triggered_message.title",
        "{criteria}"
    ))
    val CRITERIA_TRIGGERED_SUBTITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "criteria_triggered_message.subtitle",
        "{who_triggered} <gold>触发了规则"
    ))
    val AUTO_CHANGE_CRITERIA_ANNOUNCE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "auto_change_criteria_message.announce",
        "{team_name} <yellow>更换了词条，上一个词条是：{criteria}"
    ))
    val AUTO_CHANGE_CRITERIA_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "auto_change_criteria_message.title",
        "词条更换"
    ))
    val AUTO_CHANGE_CRITERIA_SUBTITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "auto_change_criteria_message.subtitle",
        "<yellow>上一个词条：{criteria}"
    ))
    val GET_DIAMOND_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "get_diamond_message",
        "{player} <yellow>获得了 <aqua>钻石"
    ))
    val HOLDING_CUSTOM_CRITERIA_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "holding_custom_criteria_message",
        "{team_name} <yellow>拥有自定义规则 【{criteria}<yellow>】"
    ))
    val HOLDING_CUSTOM_CRITERIA_CLICK_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "holding_custom_criteria_click_message",
        "<dark_aqua>【触发规则点这里】"
    ))
    val GUESS_HINT_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guess_hint_message",
        "<yellow>输入<light_purple><hover:show_text:'<aqua>点击填入聊天框'><click:suggest_command:/criteria guess>" +
            "/criteria guess \\<玩家名> \\<是否猜中></click></hover> <yellow>来标记玩家是否猜中词条"
    ))
    val GUESS_COOLDOWN_FINISHED_MESSAGE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guess_cooldown_finished_message",
        "{team_name} <yellow>现在可以猜词条了"
    ))
    val GUESS_SUCCESS_ANNOUNCE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guessed_message.success.announce",
        "{player} <yellow>猜中了词条，上一个词条是：{criteria}"
    ))
    val GUESS_SUCCESS_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guessed_message.success.title",
        "词条更换"
    ))
    val GUESS_SUCCESS_SUBTITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guessed_message.success.subtitle",
        "<yellow>猜中词条，剩余规则数<green>+{life_count}"
    ))
    val GUESS_FAILED_ANNOUNCE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guessed_message.failed.announce",
        "{player} <red>猜错了词条，上一个词条是：{criteria}"
    ))
    val GUESS_FAILED_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guessed_message.failed.title",
        "词条更换"
    ))
    val GUESS_FAILED_SUBTITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "guessed_message.failed.subtitle",
        "<red>猜错词条，剩余规则数<dark_red>-{life_count}"
    ))
    val ELIMINATED_ANNOUNCE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "eliminated_message.announce",
        "{team_name} <dark_red>已淘汰"
    ))
    val ELIMINATED_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "eliminated_message.title",
        "{team_name}"
    ))
    val ELIMINATED_SUBTITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "eliminated_message.subtitle",
        "<dark_red>已淘汰"
    ))
    //endregion

    //endregion

    //region 侧边栏
    val SIDEBAR_TITLE: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.title",
        "<shadow:#a0000000><gradient:#ff0000:#EF903E>※ <bold>禁止事项</bold> ※"
    ))
    val SIDEBAR_ENTRY_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry.name",
        "{team_name}<white><shadow:#a0aa0000>[<red>❤</red><white>{life_count:%02d}]"
    ))
    val SIDEBAR_ENTRY_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry.number",
        "<white><shadow:#a0000000>{criteria}"
    ))
    val SIDEBAR_ENTRY_NUMBER_WINNER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry.number_winner",
        "<gold><shadow:#a0000000>获胜者"
    ))
    val SIDEBAR_ENTRY_DEAD_NAME: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry_dead.name",
        "<strikethrough>{team_name}<gray><shadow:#a0000000>[\uD83D\uDC9400]"
    ))
    val SIDEBAR_ENTRY_DEAD_NUMBER: TextConfigData = CONFIG.defineConfig(TextConfigData(
        "sidebar.entry_dead.number",
        "<gray><shadow:#a0000000>已淘汰"
    ))
    //endregion

    //region 自定义词条
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
    //endregion

    //region /criteria命令的文本
    val COMMAND_GAME_NOT_START: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "criteria_command.failed.game_not_start",
        "游戏尚未开始"
    ))
    val COMMAND_GUESS_NOT_ENABLED: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "criteria_command.failed.guess_not_enabled",
        "猜词条玩法未开启"
    ))
    val COMMAND_INVALID_PLAYER: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "criteria_command.failed.invalid_player",
        "玩家%s已淘汰或者是旁观者"
    ))
    val COMMAND_NOT_CUSTOM_CRITERIA: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "criteria_command.failed.not_custom_criteria",
        "队伍%s当前的词条不是自定义词条"
    ))
    val COMMAND_INVALID_TEAM_NAME: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "criteria_command.failed.invalid_team_name",
        "无效的队伍名称"
    ))
    val COMMAND_GUESS_SELF_CRITERIA: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "criteria_command.failed.guess_self_criteria",
        "不允许自己标记自己队伍猜中了词条"
    ))
    val COMMAND_GUESS_IN_COOLDOWN: StringConfigData = CONFIG.defineConfig(StringConfigData(
        "criteria_command.failed.guess_in_cooldown",
        "玩家%s所在队伍正在猜词条冷却中（剩余时间：%d）"
    ))
    //endregion

    //region 物品
    private fun parseMaterial(id: String): Material {
        return Material.matchMaterial(id) ?: throw IllegalArgumentException("Unknown item: $id")
    }

    val JOIN_TEAM_ITEM: ParserMapConfigData<NamedTextColor, Material> = CONFIG.defineConfig(ParserMapConfigData(
        "join_team_item",
        TEAM_COLORS.inverse().mapValues { Material.STICK },
        { TEAM_COLORS[it]!! },
        ::parseMaterial
    ))
    val SPECTATOR_ITEM: MaterialConfigData = CONFIG.defineConfig(MaterialConfigData(
        "spectator_item",
        Material.LIGHT_GRAY_WOOL
    ))
    val MODIFY_CUSTOM_CRITERIA_ITEM: MaterialConfigData = CONFIG.defineConfig(MaterialConfigData(
        "modify_custom_criteria_item",
        Material.WRITABLE_BOOK
    ))
    val START_GAME_ITEM: MaterialConfigData = CONFIG.defineConfig(MaterialConfigData(
        "start_game_item",
        Material.NETHER_STAR
    ))
    //endregion

    //region 方块生成配置
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
    //endregion

    //region 游戏玩法配置项
    val Y_LEVEL_CRITERIA_ENABLED: BoolConfigData = CONFIG.defineConfig(BoolConfigData(
        "y_level_criteria_enabled",
        false
    ))
    val AUTO_CHANGE_CRITERIA_TIME: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "auto_change_criteria_time",
        120
    ))
    //endregion

    //region 全局设置默认值
    val GAME_AREA_SIZE: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "game_area_size",
        65
    ))
    val LIFE_COUNT: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "life_count",
        10
    ))
    val DIAMOND_BEHAVIOR: EnumConfigData<DiamondBehavior> = CONFIG.defineConfig(EnumConfigData(
        "diamond_behavior",
        DiamondBehavior.REDUCE_OTHERS_LIFE
    ))
    val DIAMOND_BEHAVIOR_ENABLED: BoolConfigData = CONFIG.defineConfig(BoolConfigData(
        "diamond_behavior_enabled",
        true
    ))
    val DIAMOND_BEHAVIOR_DISABLED_THRESHOLD: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "diamond_behavior_disabled_threshold",
        7
    ))
    val ALLOW_UNBALANCED_TEAMS: BoolConfigData = CONFIG.defineConfig(BoolConfigData(
        "allow_unbalanced_teams",
        false
    ))
    val ALLOW_GUESS_CRITERIA: BoolConfigData = CONFIG.defineConfig(BoolConfigData(
        "allow_guess_criteria",
        true
    ))
    val GUESS_SUCCESS_ADD_LIFE: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "guess_success_add_life",
        1
    ))
    val GUESS_FAILED_REDUCE_LIFE: IntConfigData = CONFIG.defineConfig(IntConfigData(
        "guess_failed_reduce_life",
        2
    ))
    //endregion

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