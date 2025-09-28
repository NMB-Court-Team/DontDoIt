package net.astrorbits.dontdoit

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
import net.astrorbits.lib.text.TextHelper.white
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback.Options
import org.bukkit.Difficulty
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

object DynamicSettings {
    private val LOGGER = DontDoIt.LOGGER
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
        LOGGER.info("Loading dynamic settings...")
        try {
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
                LOGGER.info("Dynamic settings file not found, using default value")
                saveSettings()
            }
        } catch (e: Exception) {
            LOGGER.error("Error when loading dynamic settings: ", e)
            e.printStackTrace()
        }
    }

    fun saveSettings() {
        LOGGER.info("Saving dynamic settings...")
        try {
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
        } catch (e: Exception) {
            LOGGER.error("Error when saving dynamic settings: ", e)
            e.printStackTrace()
        }
    }

    fun resetSettings() {
        gameAreaSize = DEFAULT_GAME_AREA_SIZE
        ingameDifficulty = DEFAULT_INGAME_DIFFICULTY
        lifeCount = DEFAULT_LIFE_COUNT
        diamondBehavior = DEFAULT_DIAMOND_BEHAVIOR
        diamondBehaviorEnabled = DEFAULT_DIAMOND_BEHAVIOR_ENABLED
        diamondBehaviorDisabledThreshold = DEFAULT_DIAMOND_BEHAVIOR_DISABLED_THRESHOLD
        allowUnbalancedTeams = DEFAULT_ALLOW_UNBALANCED_TEAMS
        allowGuessCriteria = DEFAULT_ALLOW_GUESS_CRITERIA
        guessSuccessAddLife = DEFAULT_GUESS_SUCCESS_ADD_LIFE
        guessFailedReduceLife = DEFAULT_GUESS_FAILED_REDUCE_LIFE
    }

    private fun getPath(): Path {
        return DontDoIt.instance.dataFolder.toPath().resolve(SETTINGS_FILE_NAME)
    }

    // 打开界面：执行命令/criteria settings
    @Suppress("UnstableApiUsage")
    fun createDynamicSettingsDialog(): Dialog {
        return Dialog.create { factory ->
            factory.empty().base(DialogBase.builder(Configs.DYN_SETTINGS_TITLE.get())
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .canCloseWithEscape(true)
                .inputs(listOf(
                    DialogInput.numberRange(
                        "game_area_size",
                        Configs.DYN_SETTINGS_GAME_AREA_SIZE.get(),
                        GAME_AREA_SIZE_RANGE.min.toFloat(),
                        GAME_AREA_SIZE_RANGE.max.toFloat()
                    ).step(2f)
                        .initial(gameAreaSize.toFloat())
                        .width(250)
                        .build(),
                    DialogInput.singleOption(
                        "ingame_difficulty",
                        Configs.DYN_SETTINGS_INGAME_DIFFICULTY.get(),
                        DialogHelper.createOptionEntries(Difficulty.entries, ingameDifficulty) { Component.translatable("options.difficulty.${it.name.lowercase()}").white() },
                    ).labelVisible(true)
                        .width(250)
                        .build(),
                    DialogInput.numberRange(
                        "life_count",
                        Configs.DYN_SETTINGS_LIFE_COUNT.get(),
                        LIFE_COUNT_RANGE.min.toFloat(),
                        LIFE_COUNT_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(lifeCount.toFloat())
                        .width(250)
                        .build(),
                    DialogInput.singleOption(
                        "diamond_behavior",
                        Configs.DYN_SETTINGS_DIAMOND_BEHAVIOR.get(),
                        DialogHelper.createOptionEntries(DiamondBehavior.entries, diamondBehavior) {
                            when (it) {
                                DiamondBehavior.ADD_SELF_LIFE -> Configs.DYN_SETTINGS_DIAMOND_BEHAVIOR_ADD_SELF_LIFE.get()
                                DiamondBehavior.REDUCE_OTHERS_LIFE -> Configs.DYN_SETTINGS_DIAMOND_BEHAVIOR_REDUCE_OTHERS_LIFE.get()
                            }
                        }
                    ).labelVisible(true)
                        .width(250)
                        .build(),
                    DialogInput.bool(
                        "diamond_behavior_enabled",
                        Configs.DYN_SETTINGS_DIAMOND_BEHAVIOR_ENABLED.get(),
                        diamondBehaviorEnabled,
                        Configs.DYN_SETTINGS_YES.get(),
                        Configs.DYN_SETTINGS_NO.get()
                    ),
                    DialogInput.numberRange(
                        "diamond_behavior_disabled_threshold",
                        Configs.DYN_SETTINGS_DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.get(),
                        DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.min.toFloat(),
                        DIAMOND_BEHAVIOR_DISABLED_THRESHOLD.max.toFloat()
                    ).step(1f)
                        .initial(diamondBehaviorDisabledThreshold.toFloat())
                        .width(250)
                        .build(),
                    DialogInput.bool(
                        "allow_unbalanced_teams",
                        Configs.DYN_SETTINGS_ALLOW_UNBALANCED_TEAMS.get(),
                        allowUnbalancedTeams,
                        Configs.DYN_SETTINGS_YES.get(),
                        Configs.DYN_SETTINGS_NO.get()
                    ),
                    DialogInput.bool(
                        "allow_guess_criteria",
                        Configs.DYN_SETTINGS_ALLOW_GUESS_CRITERIA.get(),
                        allowGuessCriteria,
                        Configs.DYN_SETTINGS_YES.get(),
                        Configs.DYN_SETTINGS_NO.get()
                    ),
                    DialogInput.numberRange(
                        "guess_success_add_life",
                        Configs.DYN_SETTINGS_GUESS_SUCCESS_ADD_LIFE.get(),
                        GUESS_SUCCESS_ADD_LIFE_RANGE.min.toFloat(),
                        GUESS_SUCCESS_ADD_LIFE_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(guessSuccessAddLife.toFloat())
                        .width(250)
                        .build(),
                    DialogInput.numberRange(
                        "guess_failed_reduce_life",
                        Configs.DYN_SETTINGS_GUESS_FAILED_REDUCE_LIFE.get(),
                        GUESS_FAILED_REDUCE_LIFE_RANGE.min.toFloat(),
                        GUESS_FAILED_REDUCE_LIFE_RANGE.max.toFloat()
                    ).step(1f)
                        .initial(guessFailedReduceLife.toFloat())
                        .width(250)
                        .build(),
                ))
                .build()
            ).type(DialogType.confirmation(
                ActionButton.builder(Configs.DYN_SETTINGS_SAVE.get())
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
                            audience.sendMessage(Configs.DYN_SETTINGS_SAVE_START.get())
                            TaskBuilder(DontDoIt.instance)
                                .setAsync()
                                .setTask {
                                    saveSettings()
                                    audience.sendMessage(Configs.DYN_SETTINGS_SAVE_SUCCESS.get())
                                }.runTask()
                        },
                        Options.builder().build()
                    )).build(),
                ActionButton.builder(Configs.DYN_SETTINGS_DISCARD.get()).build()
            ))
        }
    }

    val GAME_AREA_SIZE_RANGE: IntRange = IntRange(31, 255)
    val LIFE_COUNT_RANGE: IntRange = IntRange(1, 99)
    val DIAMOND_BEHAVIOR_DISABLED_THRESHOLD: IntRange = IntRange(0, 99)
    val GUESS_SUCCESS_ADD_LIFE_RANGE: IntRange = IntRange(0, 99)
    val GUESS_FAILED_REDUCE_LIFE_RANGE: IntRange = IntRange(0, 99)
}