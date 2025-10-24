package net.astrorbits.dontdoit.system

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.action.DialogAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.Configs.getJoinTeamItemMaterial
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.dontdoit.system.team.TeamInfoSynchronizer
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.item.ItemHelper.getBoolPdc
import net.astrorbits.lib.item.ItemHelper.getStringPdc
import net.astrorbits.lib.item.ItemHelper.removeIfMatch
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.text.TextHelper.format
import net.astrorbits.lib.text.TextHelper.red
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerGameModeChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object Preparation : Listener {
    private val LOGGER = DontDoIt.LOGGER

    fun onEnterPreparation() {
        for (player in Bukkit.getOnlinePlayers()) {
            setPrepared(player)
            player.level = 0
        }
        Bukkit.getWorlds().forEach { world ->
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.time = 1000
        }
    }

    private fun setPrepared(player: Player) {
        player.inventory.clear()
        putPrepareGameItemsAndSetDisplay(player)
        player.gameMode = GameMode.ADVENTURE
        player.isInvulnerable = true
        player.allowFlight = true
        if (TeamManager.getTeam(player) == null) {
            TeamManager.spectatorTeam.addPlayer(player)
        }
    }

    fun putPrepareGameItemsAndSetDisplay(player: Player) {
        player.sendMessage(Configs.ENTER_PREPARE_MESSAGE.get())
        val team = TeamManager.getTeam(player)
        if (team == null) {
            player.inventory.setItem(TEAM_ITEM_SLOT, createSpectatorTeamItem())
            TeamManager.setSpectatorDisplayName(player)
        } else {
            player.inventory.setItem(TEAM_ITEM_SLOT, createJoinTeamItem(team.color))
            player.inventory.setItem(MODIFY_CUSTOM_CRITERIA_ITEM_SLOT, createModifyCustomCriteriaItem())
            team.setPlayerDisplayName(player)
            team.sidebarDisplay.addPlayer(player)
        }
        putStartGameItem(player)
    }

    fun removePrepareGameItems(player: Player) {
        val inventory = player.inventory
        inventory.removeIfMatch(TEAM_ITEM_SLOT) { it.isPrepareGameItem() }
        inventory.removeIfMatch(MODIFY_CUSTOM_CRITERIA_ITEM_SLOT) { it.isPrepareGameItem() }
        inventory.removeIfMatch(START_GAME_ITEM_SLOT) { it.isPrepareGameItem() }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!GameStateManager.isWaiting()) return
        setPrepared(event.player)
        TeamInfoSynchronizer.syncTeamInfos(TeamManager.teams)
        if (event.player.isOp) {
            event.player.sendMessage(Configs.CHANGE_SETTINGS_HINT_MESSAGE.get())
        }
        event.player.level = 0
        onTeamMembersUpdate()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (!GameStateManager.isWaiting()) return       // 玩家退服：可能让某队人数归零
        // 把离线玩家踢掉，这样 hasMember 就不会再算他
        TeamManager.leaveTeam(event.player)
        onTeamMembersUpdate()
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (!GameStateManager.isWaiting()) return
        if (event.cursor.isPrepareGameItem() || event.currentItem?.isPrepareGameItem() == true ||
            (event.hotbarButton != -1 && event.inventory.getItem(event.hotbarButton)?.isPrepareGameItem() == true)
        ) {
            event.isCancelled = true
            event.view.setCursor(ItemStack.empty())
        }
    }

    @EventHandler
    fun onItemDrop(event: PlayerDropItemEvent) {
        if (!GameStateManager.isWaiting()) return
        if (event.itemDrop.itemStack.isPrepareGameItem()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onItemSwap(event: PlayerSwapHandItemsEvent) {
        if (!GameStateManager.isWaiting()) return
        if (event.mainHandItem.isPrepareGameItem() || event.offHandItem.isPrepareGameItem()) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (!GameStateManager.isWaiting()) return
        val player = event.player
        TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
            .setTask {
                player.allowFlight = true
                player.isInvulnerable = true
            }.runTask()
    }

    const val TEAM_NAME_PLACEHOLDER = "team_name"
    const val INTERACTED_TAG = "${DontDoIt.PLUGIN_NAME}.interacted"

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!GameStateManager.isWaiting() || !event.action.isRightClick || event.player.interacted()) return
        val item = event.item ?: return
        if (!item.isPrepareGameItem()) return

        val player = event.player
        player.markAsInteracted()

        val teamId = item.getTeamId()
        if (teamId != null) {
            val (nextTeamColor, nextTeamItem) = getNextTeam(teamId)
            event.player.inventory.setItem(TEAM_ITEM_SLOT, nextTeamItem)
            if (nextTeamColor == SPECTATOR_COLOR) {
                TeamManager.leaveTeam(player)
                player.sendMessage(Configs.JOIN_SPECTATOR_MESSAGE.get())
                player.inventory.setItem(MODIFY_CUSTOM_CRITERIA_ITEM_SLOT, ItemStack.empty())
            } else {
                TeamManager.joinTeam(player, nextTeamColor)
                player.sendMessage(Configs.JOIN_TEAM_MESSAGE.get()
                    .format(TEAM_NAME_PLACEHOLDER to TeamManager.getTeam(nextTeamColor).teamName)
                )
                player.inventory.setItem(MODIFY_CUSTOM_CRITERIA_ITEM_SLOT, createModifyCustomCriteriaItem())
            }
            onTeamMembersUpdate()
        } else if (item.isModifyCustomCriteriaItem()) {
            val dialog = createModifyCustomCriteriaDialog(player) ?: return
            player.showDialog(dialog)
        } else if (item.isStartGameItem()) {
            if (!player.isOp) {
                player.sendMessage(Component.text("You have not enough permission to do this").red())
                return
            }
            GameStateManager.startGame(player)
            LOGGER.info("Player ${player.name} starts the game")
        }

        event.isCancelled = true
    }

    private fun Player.interacted(): Boolean {
        return this.scoreboardTags.contains(INTERACTED_TAG)
    }

    private fun Player.markAsInteracted() {
        this.scoreboardTags.add(INTERACTED_TAG)
        TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(1.0)))
            .setTask { this.scoreboardTags.remove(INTERACTED_TAG) }
            .runTask()
    }

    private var canStartGame: Boolean = false

    fun onTeamMembersUpdate() {
        // 只数在线的
        val onlineTeams = TeamManager.teams.filter { team ->
            team.members.any { it.isOnline }
        }

        canStartGame = when {
            onlineTeams.size <= 1 -> false
            DynamicSettings.allowUnbalancedTeams -> true
            else -> {
                val counts = onlineTeams.map { it.members.count { p -> p.isOnline } }.toSet()
                counts.size <= 1      // 所有在线队伍人数相同
            }
        }

        // 游戏一旦开始，就不再发 start-item
        if (GameStateManager.isRunning()) return

        Bukkit.getOnlinePlayers().forEach { putStartGameItem(it) }
    }

    fun putStartGameItem(player: Player) {
        // 运行中一律不给
        if (GameStateManager.isRunning() || !GameStateManager.isWaiting() || !player.isOp) {
            if (player.inventory.getItem(START_GAME_ITEM_SLOT)?.isStartGameItem() == true) {
                player.inventory.setItem(START_GAME_ITEM_SLOT, ItemStack.empty())
            }
            return
        }

        if (canStartGame && TeamManager.getTeam(player) != null) {
            if (player.inventory.getItem(START_GAME_ITEM_SLOT)?.isStartGameItem() != true) {
                player.inventory.setItem(START_GAME_ITEM_SLOT, createStartGameItem())
            }
        } else {
            if (player.inventory.getItem(START_GAME_ITEM_SLOT)?.isStartGameItem() == true) {
                player.inventory.setItem(START_GAME_ITEM_SLOT, ItemStack.empty())
            }
        }
    }

    const val CUSTOM_CRITERIA_PLACEHOLDER = "name"

    fun tick() {
        for (player in Bukkit.getOnlinePlayers()) {
            val team = TeamManager.getTeam(player)
            val mainhandStack = player.inventory.itemInMainHand
            if (mainhandStack.isModifyCustomCriteriaItem()) {
                if (team != null) {
                    val name = customCriteriaNames[team.color]
                    if (name == null) {
                        player.sendActionBar(Configs.NOT_SET_CUSTOM_CRITERIA_MESSAGE.get())
                    } else {
                        player.sendActionBar(
                            Configs.CURRENT_CUSTOM_CRITERIA_MESSAGE.get()
                                .format(CUSTOM_CRITERIA_PLACEHOLDER to name)
                        )
                    }
                }
            } else {
                if (team == null) {
                    player.sendActionBar(Configs.JOIN_SPECTATOR_MESSAGE.get())
                } else {
                    player.sendActionBar(Configs.JOIN_TEAM_MESSAGE.get()
                        .format(TEAM_NAME_PLACEHOLDER to TeamManager.getTeam(team.color).teamName)
                    )
                }
            }
        }
    }

    fun register(plugin: JavaPlugin) {
        Bukkit.getServer().pluginManager.registerEvents(Preparation, plugin)
    }

    const val TEAM_ITEM_SLOT = 0
    const val MODIFY_CUSTOM_CRITERIA_ITEM_SLOT = 1
    const val START_GAME_ITEM_SLOT = 8

    val PREPARE_GAME_ITEM_PDC_KEY = DontDoIt.id("prepare_game_item")
    val MODIFY_CUSTOM_CRITERIA_PDC_KEY = DontDoIt.id("modify_custom_criteria")
    val START_GAME_PDC_KEY = DontDoIt.id("start_game")
    val TEAM_ID_PDC_KEY = DontDoIt.id("team_id")
    const val SPECTATOR_ID = "spectator"
    val SPECTATOR_COLOR: NamedTextColor = NamedTextColor.GRAY

    private val COLORS: List<NamedTextColor> = TeamManager.TEAM_COLORS.values.toList() + SPECTATOR_COLOR

    private fun getNextTeam(teamId: String): Pair<NamedTextColor, ItemStack> {
        val color = if (teamId == SPECTATOR_ID) NamedTextColor.GRAY else TeamManager.TEAM_COLORS[teamId] ?: throw IllegalArgumentException("Unknown team id")
        val index = COLORS.indexOf(color)
        if (index == -1) throw IllegalArgumentException("Unknown team id")
        val nextIndex = (index + 1) % COLORS.size
        val nextColor = COLORS[nextIndex]
        return nextColor to if (nextColor == SPECTATOR_COLOR) {
            createSpectatorTeamItem()
        } else {
            createJoinTeamItem(nextColor)
        }
    }

    private fun createStartGameItem(): ItemStack {
        val item = ItemStack.of(Material.FEATHER, 1)
        val itemMeta = item.itemMeta
        itemMeta.displayName(Configs.START_GAME_ITEM_NAME.get())
        itemMeta.setMaxStackSize(1)
        itemMeta.itemModel = Configs.START_GAME_ITEM.get().key
        itemMeta.setEnchantmentGlintOverride(true)
        itemMeta.persistentDataContainer.set(PREPARE_GAME_ITEM_PDC_KEY, PersistentDataType.BOOLEAN, true)
        itemMeta.persistentDataContainer.set(START_GAME_PDC_KEY, PersistentDataType.BOOLEAN, true)
        item.setItemMeta(itemMeta)
        return item
    }

    private fun createModifyCustomCriteriaItem(): ItemStack {
        val item = ItemStack.of(Material.FEATHER, 1)
        val itemMeta = item.itemMeta
        itemMeta.displayName(Configs.MODIFY_CUSTOM_CRITERIA_ITEM_NAME.get())
        itemMeta.setMaxStackSize(1)
        itemMeta.itemModel = Configs.MODIFY_CUSTOM_CRITERIA_ITEM.get().key
        itemMeta.persistentDataContainer.set(PREPARE_GAME_ITEM_PDC_KEY, PersistentDataType.BOOLEAN, true)
        itemMeta.persistentDataContainer.set(MODIFY_CUSTOM_CRITERIA_PDC_KEY, PersistentDataType.BOOLEAN, true)
        item.setItemMeta(itemMeta)
        return item
    }

    private fun createSpectatorTeamItem(): ItemStack {
        val item = ItemStack.of(Material.FEATHER, 1)
        val itemMeta = item.itemMeta
        val itemName = Configs.CHANGE_TEAM_ITEM_NAME.get()
        itemMeta.displayName(itemName)
        itemMeta.setMaxStackSize(1)
        itemMeta.itemModel = Configs.SPECTATOR_ITEM.get().key
        itemMeta.persistentDataContainer.set(PREPARE_GAME_ITEM_PDC_KEY, PersistentDataType.BOOLEAN, true)
        itemMeta.persistentDataContainer.set(TEAM_ID_PDC_KEY, PersistentDataType.STRING, SPECTATOR_ID)
        item.setItemMeta(itemMeta)
        return item
    }

    private fun createJoinTeamItem(color: NamedTextColor): ItemStack {
        //DontDoIt.LOGGER.info(color.toString())
        val material = getJoinTeamItemMaterial(color)
        val item = ItemStack.of(Material.FEATHER, 1)
        val itemMeta = item.itemMeta
        val itemName = Configs.CHANGE_TEAM_ITEM_NAME.get()
        itemMeta.displayName(itemName)
        itemMeta.setMaxStackSize(1)
        itemMeta.itemModel = material.key
        itemMeta.persistentDataContainer.set(PREPARE_GAME_ITEM_PDC_KEY, PersistentDataType.BOOLEAN, true)
        itemMeta.persistentDataContainer.set(TEAM_ID_PDC_KEY, PersistentDataType.STRING, TeamManager.TEAM_COLORS.inverse()[color]!!)
        item.setItemMeta(itemMeta)
        return item
    }

    private fun ItemStack.isPrepareGameItem(): Boolean {
        return this.getBoolPdc(PREPARE_GAME_ITEM_PDC_KEY) ?: false
    }

    private fun ItemStack.isModifyCustomCriteriaItem(): Boolean {
        return this.getBoolPdc(MODIFY_CUSTOM_CRITERIA_PDC_KEY) ?: false
    }

    private fun ItemStack.isStartGameItem(): Boolean {
        return this.getBoolPdc(START_GAME_PDC_KEY) ?: false
    }

    private fun ItemStack.getTeamId(): String? {
        return this.getStringPdc(TEAM_ID_PDC_KEY)
    }

    val customCriteriaNames: MutableMap<NamedTextColor, String> = mutableMapOf()

    private const val CUSTOM_CRITERIA_NAME_DIALOG_KEY = "name"
    const val CUSTOM_CRITERIA_NAME_LENGTH = 20

    @Suppress("UnstableApiUsage")
    private fun createModifyCustomCriteriaDialog(player: Player): Dialog? {
        val teamData = TeamManager.getTeam(player) ?: return null
        val name = customCriteriaNames[teamData.color] ?: ""
        val dialog = Dialog.create { factory ->
            factory.empty().base(DialogBase.builder(Configs.MODIFY_CUSTOM_CRITERIA_TITLE.get())
                .body(Configs.MODIFY_CUSTOM_CRITERIA_BODY.get().map {
                    DialogBody.plainMessage(it, 1024)
                })
                .inputs(listOf(DialogInput.text(CUSTOM_CRITERIA_NAME_DIALOG_KEY, Configs.MODIFY_CUSTOM_CRITERIA_TEXT_BOX_TITLE.get())
                    .initial(name)
                    .maxLength(CUSTOM_CRITERIA_NAME_LENGTH)
                    .width(150)
                    .build()
                ))
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .canCloseWithEscape(true)
                .build()
            ).type(DialogType.confirmation(
                ActionButton.builder(Configs.MODIFY_CUSTOM_CRITERIA_YES_BUTTON_LABEL.get())
                    .action(DialogAction.customClick(
                        { response, audience ->
                            val inputName = response.getText(CUSTOM_CRITERIA_NAME_DIALOG_KEY)
                            if (inputName == null) {
                                DontDoIt.LOGGER.warn("Cannot set custom criteria name for team ${teamData.teamId} for unknown reason")
                                audience.sendMessage(Component.text("Cannot set custom criteria name for team ${teamData.teamId} for unknown reason").red())
                                return@customClick
                            }
                            if (inputName.isEmpty()) {
                                removeCustomCriteriaName(player, teamData)
                            } else {
                                setCustomCriteriaName(player, teamData, inputName)
                            }
                        },
                        ClickCallback.Options.builder().build()
                    )).build(),
                ActionButton.builder(Configs.MODIFY_CUSTOM_CRITERIA_NO_BUTTON_LABEL.get()).build()
            ))
        }
        return dialog
    }

    const val PLAYER_NAME_PLACEHOLDER = "player"

    private fun setCustomCriteriaName(setter: Player, teamData: TeamData, name: String) {
        customCriteriaNames[teamData.color] = name
        setter.sendMessage(Configs.SELF_SET_CUSTOM_CRITERIA_MESSAGE.get()
            .format(CUSTOM_CRITERIA_PLACEHOLDER to name)
        )
        for (player in teamData.members) {
            if (player.uniqueId == setter.uniqueId) continue
            player.sendMessage(Configs.OTHER_SET_CUSTOM_CRITERIA_MESSAGE.get()
                .format(
                    PLAYER_NAME_PLACEHOLDER to player.displayName(),
                    CUSTOM_CRITERIA_PLACEHOLDER to name
                )
            )
        }
        LOGGER.info("Player ${setter.name} sets the custom criteria name of team ${teamData.teamId} to $name")
    }

    private fun removeCustomCriteriaName(remover: Player, teamData: TeamData) {
        customCriteriaNames.remove(teamData.color)
        remover.sendMessage(Configs.SELF_REMOVE_CUSTOM_CRITERIA_MESSAGE.get())
        for (player in teamData.members) {
            if (player.uniqueId == remover.uniqueId) continue
            player.sendMessage(Configs.OTHER_REMOVE_CUSTOM_CRITERIA_MESSAGE.get()
                .format(PLAYER_NAME_PLACEHOLDER to player.displayName())
            )
        }
        LOGGER.info("Player ${remover.name} removes the custom criteria name of team ${teamData.teamId}")
    }
}