package net.astrorbits.dontdoit

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.astrorbits.dontdoit.system.DiamondBehavior
import net.astrorbits.lib.dialog.DialogHelper
import net.astrorbits.lib.range.IntRange
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.text.LegacyText
import net.astrorbits.lib.text.TextHelper.yellow
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback.Options
import org.bukkit.Difficulty
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

object DynamicSettings {
    const val SETTINGS_FILE_NAME = "settings.json"

    const val DEFAULT_GAME_AREA_SIZE = 63
    val DEFAULT_INGAME_DIFFICULTY = Difficulty.EASY
    const val DEFAULT_LIFE_COUNT = 15
    val DEFAULT_DIAMOND_BEHAVIOR = DiamondBehavior.REDUCE_OTHERS_LIFE
    const val DEFAULT_DIAMOND_BEHAVIOR_ENABLED = true
    const val DEFAULT_DIAMOND_BEHAVIOR_DISABLED_THRESHOLD = 7
    const val DEFAULT_ALLOW_UNBALANCED_TEAMS = false
    const val DEFAULT_ALLOW_GUESS_CRITERIA = true
    const val DEFAULT_GUESS_SUCCESS_ADD_LIFE = 1
    const val DEFAULT_GUESS_FAILED_REDUCE_LIFE = 2

    var gameAreaSize: Int = DEFAULT_GAME_AREA_SIZE
    var ingameDifficulty: Difficulty = DEFAULT_INGAME_DIFFICULTY
    var lifeCount: Int = DEFAULT_LIFE_COUNT
    var diamondBehavior: DiamondBehavior = DEFAULT_DIAMOND_BEHAVIOR
    var diamondBehaviorEnabled: Boolean = DEFAULT_DIAMOND_BEHAVIOR_ENABLED
    var diamondBehaviorDisabledThreshold: Int = DEFAULT_DIAMOND_BEHAVIOR_DISABLED_THRESHOLD
    var allowUnbalancedTeams: Boolean = DEFAULT_ALLOW_UNBALANCED_TEAMS
    var allowGuessCriteria: Boolean = DEFAULT_ALLOW_GUESS_CRITERIA
    var guessSuccessAddLife: Int = DEFAULT_GUESS_SUCCESS_ADD_LIFE
    var guessFailedReduceLife: Int = DEFAULT_GUESS_FAILED_REDUCE_LIFE

    fun loadSettings() {
        val path = getPath()
        if (path.exists()) {
            val jsonElement = JsonParser.parseString(path.toFile().readText())
            if (!jsonElement.isJsonObject) throw JsonParseException("Root element is not a json object")
            val json = jsonElement.asJsonObject
            gameAreaSize = json.getAsJsonPrimitive("game_area_size").asInt
            ingameDifficulty = Difficulty.valueOf(json.getAsJsonPrimitive("ingame_difficulty").asString.uppercase())
            lifeCount = json.getAsJsonPrimitive("life_count").asInt
            diamondBehavior = DiamondBehavior.valueOf(json.getAsJsonPrimitive("diamond_behavior").asString.uppercase())
            diamondBehaviorEnabled = json.getAsJsonPrimitive("diamond_behavior_enabled").asBoolean
            diamondBehaviorDisabledThreshold = json.getAsJsonPrimitive("diamond_behavior_disabled_threshold").asInt
            allowUnbalancedTeams = json.getAsJsonPrimitive("allow_unbalanced_teams").asBoolean
            allowGuessCriteria = json.getAsJsonPrimitive("allow_guess_criteria").asBoolean
            guessSuccessAddLife = json.getAsJsonPrimitive("guess_success_add_life").asInt
            guessFailedReduceLife = json.getAsJsonPrimitive("guess_failed_reduce_life").asInt
        } else {
            saveSettings()
        }
    }

    fun saveSettings() {
        val path = getPath()
        if (!path.parent.exists()) {
            Files.createDirectory(path.parent)
        }
        val json = JsonObject()
        json.addProperty("game_area_size", gameAreaSize)
        json.addProperty("ingame_difficulty", ingameDifficulty.name.lowercase())
        json.addProperty("life_count", lifeCount)
        json.addProperty("diamond_behavior", diamondBehavior.name.lowercase())
        json.addProperty("diamond_behavior_enabled", diamondBehaviorEnabled)
        json.addProperty("diamond_behavior_disabled_threshold", diamondBehaviorDisabledThreshold)
        json.addProperty("allow_unbalanced_teams", allowUnbalancedTeams)
        json.addProperty("allow_guess_criteria", allowGuessCriteria)
        json.addProperty("guess_success_add_life", guessSuccessAddLife)
        json.addProperty("guess_failed_reduce_life", guessFailedReduceLife)
        val jsonString = json.toString()
        path.writeText(jsonString)
    }

    private fun getPath(): Path {
        return DontDoIt.instance.dataFolder.toPath().resolve(SETTINGS_FILE_NAME)
    }

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
                        LegacyText.toComponent("§f游戏区域大小"),
                        GAME_AREA_SIZE_RANGE.min.toFloat(),
                        GAME_AREA_SIZE_RANGE.max.toFloat()
                    ).step(2f)
                        .initial(gameAreaSize.toFloat())
                        .build(),
                    DialogInput.singleOption(
                        "ingame_difficulty",
                        LegacyText.toComponent("§f游戏期间的Minecraft游戏难度"),
                        DialogHelper.createOptionEntries(Difficulty.entries, ingameDifficulty) { Component.translatable("options.difficulty.${it.name.lowercase()}").yellow() },
                    ).labelVisible(true)
                        .build(),
                    DialogInput.numberRange(
                        "life_count",
                        LegacyText.toComponent("§f规则数量上限（生命值上限）"),
                        LIFE_COUNT_RANGE.min.toFloat(),
                        LIFE_COUNT_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(lifeCount.toFloat())
                        .build(),
                    DialogInput.singleOption(
                        "diamond_behavior",
                        LegacyText.toComponent("§f钻石行为"),
                        DialogHelper.createOptionEntries(DiamondBehavior.entries, diamondBehavior) {
                            when (it) {
                                DiamondBehavior.ADD_SELF_LIFE -> LegacyText.toComponent("§e获取钻石时自己队伍回复1点规则数量")
                                DiamondBehavior.REDUCE_OTHERS_LIFE -> LegacyText.toComponent("§e获取钻石时其他队伍减少1点规则数量")
                            }
                        }
                    ).labelVisible(true)
                        .build(),
                    DialogInput.bool(
                        "diamond_behavior_enabled",
                        LegacyText.toComponent("§f是否启用钻石行为"),
                        diamondBehaviorEnabled,
                        "是", "否"
                    ),
                    DialogInput.numberRange(
                        "diamond_behavior_disabled_threshold",
                        LegacyText.toComponent("§f钻石行为禁用阈值（其他队伍规则数量小于等于该值时禁用钻石行为）"),
                        DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.min.toFloat(),
                        DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.max.toFloat()
                    ).step(1f)
                        .initial(diamondBehaviorDisabledThreshold.toFloat())
                        .labelFormat("§f钻石行为禁用阈值: %2\$s")
                        .build(),
                    DialogInput.bool(
                        "allow_unbalanced_teams",
                        LegacyText.toComponent("§f是否允许各队伍人数不平衡"),
                        allowUnbalancedTeams,
                        "是", "否"
                    ),
                    DialogInput.bool(
                        "allow_guess_criteria",
                        LegacyText.toComponent("§f是否开启猜词条玩法"),
                        allowGuessCriteria,
                        "是", "否"
                    ),
                    DialogInput.numberRange(
                        "guess_success_add_life",
                        LegacyText.toComponent("§f猜对词条时回复的规则数量"),
                        GUESS_SUCCESS_ADD_LIFE_RANGE.min.toFloat(),
                        GUESS_SUCCESS_ADD_LIFE_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(guessSuccessAddLife.toFloat())
                        .build(),
                    DialogInput.numberRange(
                        "guess_failed_reduce_life",
                        LegacyText.toComponent("§f猜错词条时减少的规则数量"),
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
                            audience.sendMessage(LegacyText.toComponent("§e正在保存对游戏设置的修改..."))
                            TaskBuilder(DontDoIt.instance)
                                .setAsync()
                                .setTask {
                                    saveSettings()
                                    audience.sendMessage(LegacyText.toComponent("§e游戏设置保存完毕"))
                                }.runTask()
                        },
                        Options.builder().build()
                    )).build(),
                ActionButton.builder(LegacyText.toComponent("放弃修改")).build()
            ))
        }
    }  //TODO 这里有一堆文本要写进配置

    val GAME_AREA_SIZE_RANGE: IntRange = IntRange(31, 255)
    val LIFE_COUNT_RANGE: IntRange = IntRange(1, 99)
    val DIAMOND_BEHAVIOR_DISABLED_THRESHOLD: IntRange = IntRange(0, 99)
    val GUESS_SUCCESS_ADD_LIFE_RANGE: IntRange = IntRange(0, 99)
    val GUESS_FAILED_REDUCE_LIFE_RANGE: IntRange = IntRange(0, 99)
}