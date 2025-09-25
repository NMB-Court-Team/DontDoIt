package net.astrorbits.dontdoit

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.astrorbits.dontdoit.system.DiamondBehavior
import net.astrorbits.lib.dialog.DialogHelper
import net.astrorbits.lib.range.IntRange
import net.astrorbits.lib.text.LegacyText
import net.astrorbits.lib.text.TextHelper.gold
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback.Options
import org.bukkit.Difficulty

object DynamicSettings {
    var gameAreaSize: Int = Configs.GAME_AREA_SIZE.get()
    var ingameDifficulty: Difficulty = Configs.INGAME_DIFFICULTY.get()
    var lifeCount: Int = Configs.LIFE_COUNT.get()
    var diamondBehavior: DiamondBehavior = Configs.DIAMOND_BEHAVIOR.get()
    var diamondBehaviorEnabled: Boolean = Configs.DIAMOND_BEHAVIOR_ENABLED.get()
    var diamondBehaviorDisabledThreshold: Int = Configs.DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.get()
    var allowUnbalancedTeams: Boolean = Configs.ALLOW_UNBALANCED_TEAMS.get()
    var allowGuessCriteria: Boolean = Configs.ALLOW_GUESS_CRITERIA.get()
    var guessSuccessAddLife: Int = Configs.GUESS_SUCCESS_ADD_LIFE.get()
    var guessFailedReduceLife: Int = Configs.GUESS_FAILED_REDUCE_LIFE.get()

    // 打开界面：执行命令/criteria settings
    @Suppress("UnstableApiUsage")
    fun createDynamicSettingsDialog(): Dialog {
        return Dialog.create { factory ->
            factory.empty().base(DialogBase.builder(LegacyText.toComponent("§b§l修改游戏设置"))
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .canCloseWithEscape(true)
                .inputs(listOf(
                    DialogInput.numberRange(
                        "game_area_size",
                        LegacyText.toComponent("§e游戏区域大小"),
                        GAME_AREA_SIZE_RANGE.min.toFloat(),
                        GAME_AREA_SIZE_RANGE.max.toFloat()
                    ).step(2f)
                        .initial(gameAreaSize.toFloat())
                        .build(),
                    DialogInput.singleOption(
                        "ingame_difficulty",
                        LegacyText.toComponent("§e游戏期间的Minecraft游戏难度"),
                        DialogHelper.createOptionEntries(Difficulty.entries, ingameDifficulty) { Component.translatable("options.difficulty.${it.name.lowercase()}").gold() },
                    ).labelVisible(true)
                        .build(),
                    DialogInput.numberRange(
                        "life_count",
                        LegacyText.toComponent("§e规则数量上限（生命值上限）"),
                        LIFE_COUNT_RANGE.min.toFloat(),
                        LIFE_COUNT_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(lifeCount.toFloat())
                        .build(),
                    DialogInput.singleOption(
                        "diamond_behavior",
                        LegacyText.toComponent("§e钻石行为"),
                        DialogHelper.createOptionEntries(DiamondBehavior.entries, diamondBehavior) {
                            when (it) {
                                DiamondBehavior.ADD_SELF_LIFE -> LegacyText.toComponent("§6获取钻石时自己队伍回复1点规则数量")
                                DiamondBehavior.REDUCE_OTHERS_LIFE -> LegacyText.toComponent("§6获取钻石时其他队伍减少1点规则数量")
                            }
                        }
                    ).labelVisible(true)
                        .build(),
                    DialogInput.bool(
                        "diamond_behavior_enabled",
                        LegacyText.toComponent("§e是否启用钻石行为"),
                        diamondBehaviorEnabled,
                        "是", "否"
                    ),
                    DialogInput.numberRange(
                        "diamond_behavior_disabled_threshold",
                        LegacyText.toComponent("§e钻石行为禁用阈值（其他队伍规则数量小于等于该值时禁用钻石行为）"),
                        DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.min.toFloat(),
                        DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.max.toFloat()
                    ).step(1f)
                        .initial(diamondBehaviorDisabledThreshold.toFloat())
                        .labelFormat("§e钻石行为禁用阈值: %2\$s")
                        .build(),
                    DialogInput.bool(
                        "allow_unbalanced_teams",
                        LegacyText.toComponent("§e是否允许各队伍人数不平衡"),
                        allowUnbalancedTeams,
                        "是", "否"
                    ),
                    DialogInput.bool(
                        "allow_guess_criteria",
                        LegacyText.toComponent("§e是否开启猜词条玩法"),
                        allowGuessCriteria,
                        "是", "否"
                    ),
                    DialogInput.numberRange(
                        "guess_success_add_life",
                        LegacyText.toComponent("§e猜对词条时回复的规则数量"),
                        GUESS_SUCCESS_ADD_LIFE_RANGE.min.toFloat(),
                        GUESS_SUCCESS_ADD_LIFE_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(guessSuccessAddLife.toFloat())
                        .build(),
                    DialogInput.numberRange(
                        "guess_failed_reduce_life",
                        LegacyText.toComponent("§e猜错词条时减少的规则数量"),
                        GUESS_FAILED_REDUCE_LIFE_RANGE.min.toFloat(),
                        GUESS_FAILED_REDUCE_LIFE_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(guessFailedReduceLife.toFloat())
                        .build(),
                ))
                .build()
            ).type(DialogType.confirmation(
                ActionButton.builder(LegacyText.toComponent("保存修改"))
                    .action(DialogAction.customClick(
                        { responseView, audience ->
                            val gameAreaSize = responseView.getFloat("game_area_size")
                            val ingameDifficulty = responseView.getText("ingame_difficulty")
                            val lifeCount = responseView.getFloat("life_count")
                            val diamondBehavior = responseView.getText("diamond_behavior")
                            val diamondBehaviorEnabled = responseView.getBoolean("diamond_behavior_enabled")
                            val diamondBehaviorDisabledThreshold = responseView.getFloat("diamond_behavior_disabled_threshold")
                            val allowUnbalancedTeams = responseView.getBoolean("allow_unbalanced_teams")
                            val allowGuessCriteria = responseView.getBoolean("allow_guess_criteria")
                            val guessSuccessAddLife = responseView.getFloat("guess_success_add_life")
                            val guessFailedReduceLife = responseView.getFloat("guess_failed_reduce_life")
                            if (gameAreaSize != null) this.gameAreaSize = gameAreaSize.toInt()
                            if (ingameDifficulty != null) this.ingameDifficulty = Difficulty.valueOf(ingameDifficulty.uppercase())
                            if (lifeCount != null) this.lifeCount = lifeCount.toInt()
                            if (diamondBehavior != null) this.diamondBehavior = DiamondBehavior.valueOf(diamondBehavior.uppercase())
                            if (diamondBehaviorEnabled != null) this.diamondBehaviorEnabled = diamondBehaviorEnabled
                            if (diamondBehaviorDisabledThreshold != null) this.diamondBehaviorDisabledThreshold = diamondBehaviorDisabledThreshold.toInt()
                            if (allowUnbalancedTeams != null) this.allowUnbalancedTeams = allowUnbalancedTeams
                            if (allowGuessCriteria != null) this.allowGuessCriteria = allowGuessCriteria
                            if (guessSuccessAddLife != null) this.guessSuccessAddLife = guessSuccessAddLife.toInt()
                            if (guessFailedReduceLife != null) this.guessFailedReduceLife = guessFailedReduceLife.toInt()
                            audience.sendMessage(LegacyText.toComponent("§e已保存对游戏设置的修改"))
                        },
                        Options.builder().build()
                    )).build(),
                ActionButton.builder(LegacyText.toComponent("放弃修改")).build()
            ))
        }
    }  //TODO 牛魔的这里有19个文本要写进配置，我受不了了

    //TODO 把这个写进lib.dialog里面写成一个比较系统性的游戏内动态配置
    //  然后这个还缺一个保存到服务器的操作，每次重启服务器都会丢失之前设置的配置

    val GAME_AREA_SIZE_RANGE: IntRange = IntRange(31, 255)
    val LIFE_COUNT_RANGE: IntRange = IntRange(1, 99)
    val DIAMOND_BEHAVIOR_DISABLED_THRESHOLD: IntRange = IntRange(0, 99)
    val GUESS_SUCCESS_ADD_LIFE_RANGE: IntRange = IntRange(0, 99)
    val GUESS_FAILED_REDUCE_LIFE_RANGE: IntRange = IntRange(0, 99)
}