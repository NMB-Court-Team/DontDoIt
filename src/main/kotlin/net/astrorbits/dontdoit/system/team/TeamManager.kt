package net.astrorbits.dontdoit.system.team

import com.google.common.collect.BiMap
import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.dontdoit.system.DiamondBehavior
import net.astrorbits.dontdoit.system.GameState
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.team.TeamData.Companion.CRITERIA_DISPLAY_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.LIFE_COUNT_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.PLAYER_NAME_PLACEHOLDER
import net.astrorbits.dontdoit.system.team.TeamData.Companion.TEAM_NAME_PLACEHOLDER
import net.astrorbits.lib.collection.CollectionHelper.toBiMap
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.scoreboard.SidebarDisplay
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.astrorbits.lib.task.Timer
import net.astrorbits.lib.text.TextHelper.format
import net.astrorbits.lib.text.TextHelper.gray
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scoreboard.Team

object TeamManager : Listener {
    val TEAM_COLORS: BiMap<String, NamedTextColor> = setOf(
        NamedTextColor.RED,
        NamedTextColor.GOLD,
        NamedTextColor.YELLOW,
        NamedTextColor.GREEN,
        NamedTextColor.AQUA,
        NamedTextColor.DARK_AQUA,
        NamedTextColor.LIGHT_PURPLE,
        NamedTextColor.DARK_PURPLE
    ).associateBy { NamedTextColor.NAMES.valueToKey()[it]!! }.toBiMap()

    private val _teams: MutableList<TeamData> = mutableListOf()
    val spectatorSidebarDisplay: SidebarDisplay = SidebarDisplay()
    val spectatorTeam: Team = spectatorSidebarDisplay.scoreboard.registerNewTeam("spectator")

    val teams: List<TeamData>
        get() = _teams

    fun init(server: Server) {
        server.pluginManager.registerEvents(this, DontDoIt.instance)

        for ((_, color) in TEAM_COLORS) {
            _teams.add(TeamData(color))
        }
        spectatorSidebarDisplay.title = Configs.SIDEBAR_TITLE.get()
        spectatorTeam.color(NamedTextColor.GRAY)
    }

    fun getInUseTeams(): Map<String, TeamData> {
        return if (!GameStateManager.isWaiting()) {
            teams.filter { it.isInUse }.associateBy { it.teamId }
        } else {
            teams.associateBy { it.teamId }
        }
    }

    fun joinTeam(player: Player, color: NamedTextColor) {
        leaveTeam(player, false)
        getTeam(color).join(player)
        TeamInfoSynchronizer.syncTeamInfos(teams)
    }

    fun leaveTeam(player: Player, joinSpectator: Boolean = true) {
        getTeam(player)?.leave(player)
        spectatorTeam.removePlayer(player)
        if (joinSpectator) {
            joinSpectatorTeam(player)
        } else {
            TeamInfoSynchronizer.syncTeamInfos(teams)
        }
    }

    fun joinSpectatorTeam(player: Player, sync: Boolean = true) {
        spectatorSidebarDisplay.addPlayer(player)
        spectatorTeam.addPlayer(player)
        if (sync) {
            TeamInfoSynchronizer.syncTeamInfos(teams)
        }
    }

    fun setSpectatorDisplayName(player: Player) {
        val displayName = Component.text(player.name).gray()
        player.displayName(displayName)
        player.playerListName(displayName)
    }

    /** 更新计分板显示 */
    fun updateSidebars() {
        val teams = getInUseTeams().values
        for (teamData in teams) {
            val otherTeams = teams.filter { it !== teamData }
            teamData.updateSidebar(otherTeams)
        }
        spectatorSidebarDisplay.content = getInUseTeams().values.map(::formatTeamSidebarInfo)
        for (player in Bukkit.getOnlinePlayers()) {
            if (getTeam(player) == null) {
                joinSpectatorTeam(player, false)
            } else {
                spectatorTeam.removePlayer(player)
            }
        }
        TeamInfoSynchronizer.syncTeamInfos(this.teams)
    }

    fun formatTeamSidebarInfo(teamData: TeamData): SidebarDisplay.ScoreEntry {
        val isWinner = GameStateManager.state == GameState.FINISHED && getWinner() === teamData

        val criteria = teamData.criteria
        val nameFormatConfig = if (teamData.isEliminated) Configs.SIDEBAR_ENTRY_DEAD_NAME else Configs.SIDEBAR_ENTRY_NAME
        val name = nameFormatConfig.get().format(mapOf(
            TEAM_NAME_PLACEHOLDER to teamData.teamName,
            LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
            CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria?.displayName
        ))
        val numberFormatConfig = if (teamData.isEliminated) {
            Configs.SIDEBAR_ENTRY_DEAD_NUMBER
        } else if (isWinner) {
            Configs.SIDEBAR_ENTRY_NUMBER_WINNER
        } else {
            Configs.SIDEBAR_ENTRY_NUMBER
        }
        val number = numberFormatConfig.get().format(mapOf(
            TEAM_NAME_PLACEHOLDER to teamData.teamName,
            LIFE_COUNT_PLACEHOLDER to teamData.lifeCount,
            CRITERIA_DISPLAY_NAME_PLACEHOLDER to criteria?.displayName
        ))
        return SidebarDisplay.ScoreEntry(name, number)
    }

    fun getTeam(player: Player): TeamData? {
        return _teams.firstOrNull { player in it }
    }

    fun getTeam(color: NamedTextColor): TeamData {
        return _teams.first { it.color == color }
    }

    fun getWinner(): TeamData? {
        val aliveTeams = getInUseTeams().values.filter { !it.isEliminated }
        return if (aliveTeams.size == 1) {
            aliveTeams[0]
        } else {
            null
        }
    }

    @EventHandler
    fun onJoinServer(event: PlayerJoinEvent) {
        if (GameStateManager.isWaiting()) return
        val player = event.player
        val team = getTeam(player)
        if (team == null) {
            player.gameMode = GameMode.SPECTATOR
            setSpectatorDisplayName(player)
            joinSpectatorTeam(player, false)
        } else {
            team.setPlayerDisplayName(player)
            team.sidebarDisplay.addPlayer(player)
        }
        TaskBuilder(DontDoIt.instance, TaskType.Delayed(Duration.ticks(2.0)))
            .setTask { TeamInfoSynchronizer.syncTeamInfos(teams) }
            .runTask()
        updateSidebars()
    }

    private val guessHintAnnounceTimer: Timer = object : Timer(DontDoIt.instance) {
        override fun onStart() { }
        override fun onTick() {
            if ((currentTimeTicks - GUESS_ANNOUNCE_DELAY_SEC * 20) % (GUESS_ANNOUNCE_COOLDOWN_SEC * 20) == 0) {
                getInUseTeams().values.forEach { it.broadcast(Configs.GUESS_HINT_MESSAGE.get()) }
            }
        }
        override fun onStop() { }
    }

    const val GUESS_ANNOUNCE_COOLDOWN_SEC = 60
    const val GUESS_ANNOUNCE_DELAY_SEC = 25

    fun onGameStart() {
        for (teamData in teams) {
            if (teamData.hasMember) {
                teamData.onGameStart()
            }
        }
        for (player in Bukkit.getOnlinePlayers()) {
            val team = getTeam(player)
            if (team == null) {
                player.gameMode = GameMode.SPECTATOR
                joinSpectatorTeam(player, false)
            } else {
                player.gameMode = GameMode.SURVIVAL
                spectatorSidebarDisplay.removePlayer(player)
                team.sidebarDisplay.addPlayer(player)
            }
        }
        if (DynamicSettings.allowGuessCriteria) {
            guessHintAnnounceTimer.start()
        }
        spectatorSidebarDisplay.show()
        updateSidebars()
    }

    fun tryEndGame() {
        if (getInUseTeams().values.count { !it.isEliminated } <= 1) {
            GameStateManager.endGame()
        }
    }

    fun onGameEnd() {
        guessHintAnnounceTimer.reset()
        teams.forEach { team -> team.onGameEnd() }
        updateSidebars()
    }

    fun onEnterPreparation() {
        for (teamData in teams) {
            teamData.onEnterPreparation()
        }
        spectatorSidebarDisplay.hide()
        spectatorSidebarDisplay.content = emptyList()
        guessHintAnnounceTimer.reset()
    }

    /**
     * 返回值代表是否允许猜词条
     */
    fun guess(player: Player, teamData: TeamData, guessed: Boolean): Int? {
        return teamData.guess(player, guessed)
    }

    private val UNPICKED_DIAMOND_PDC_KEY = DontDoIt.id("unpicked_diamond")

    /**
     * 挖矿掉落的钻石打上「未拾取」标签。
     */
    @EventHandler
    fun onBlockDropItem(event: BlockDropItemEvent) {
        if (event.isCancelled) return
        for (item in event.items) {
            val stack = item.itemStack
            if (stack.type == Material.DIAMOND && !stack.isUnpickedDiamond()) {
                item.itemStack = stack.apply { setUnpickedDiamond(true) }
            }
        }
    }

    /**
     * 各种 interact 事件触发后,检查 cursor 和背包里带「未拾取」标签的钻石并全部转换:
     * cursor 上的原地清除标签(物品不消失,无缝);背包里的先清掉、再给予同数量普通钻石
     * (被动合并进已有普通堆)。每颗钻石触发一次行为(禁用/阈值时仅转换不触发)。
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        (event.whoClicked as? Player)?.let { convertAllTaggedDiamonds(it) }
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        (event.whoClicked as? Player)?.let { convertAllTaggedDiamonds(it) }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        (event.player as? Player)?.let { convertAllTaggedDiamonds(it) }
    }

    /**
     * 捡起钻石:捡起的是带标签钻石时,原地清除这组的标签(原版随后会把普通钻石放进背包),
     * 并转换身上其他所有带标签钻石。(捡起普通钻石时没有带标签的钻石,自然无事发生。)
     */
    @EventHandler
    fun onPickUpItem(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val stack = event.item.itemStack
        if (stack.type != Material.DIAMOND || !stack.isUnpickedDiamond()) return
        // 原地清除这组的标签 → 原版随后把普通钻石放进背包;计入触发数量
        val pickedUp = stack.amount
        stack.setUnpickedDiamond(false)
        event.item.itemStack = stack
        convertAllTaggedDiamonds(player, pickedUp)
    }

    /**
     * 背包满时:地上带标签的钻石原版完全装不下(remaining == amount),这里接管:
     * 按容量(空格×64 + 普通钻石堆空位)清掉地上的对应数量,触发行为并给予普通钻石;
     * 容量小于地上数量时只处理对应数量,其余留在地上。
     */
    @EventHandler
    fun onAttemptPickupDiamond(event: PlayerAttemptPickupItemEvent) {
        if (!GameStateManager.isRunning()) return
        val stack = event.item.itemStack
        if (stack.type != Material.DIAMOND || !stack.isUnpickedDiamond()) return
        // 原版能装下(至少一部分)→ 交给正常捡起流程(EntityPickupItemEvent 会转换)
        if (event.remaining < stack.amount) return

        val player = event.player
        val team = getTeam(player) ?: return
        if (!team.isInUse || team.isEliminated) return

        var capacity = 0
        val inventory = player.inventory
        for (slot in 0 until inventory.size) {
            val item = inventory.getItem(slot)
            if (item == null || item.type == Material.AIR) {
                capacity += 64
            } else if (item.type == Material.DIAMOND && !item.isUnpickedDiamond()) {
                capacity += item.maxStackSize - item.amount
            }
        }
        if (capacity <= 0) return

        val processCount = minOf(capacity, stack.amount)
        repeat(processCount) { triggerDiamondBehavior(player, team) }
        givePlainDiamonds(player, processCount)

        val remaining = stack.amount - processCount
        if (remaining <= 0) {
            event.item.remove()
        } else {
            stack.amount = remaining
            event.item.itemStack = stack
        }
    }

    /**
     * 转换玩家身上所有带「未拾取」标签的钻石。
     * cursor 上的原地清除标签(物品保留,只计触发);背包(主36 + offhand)里的先清掉,
     * 再给予同数量普通钻石(被动合并);extraCount 是调用方已原地清标签的钻石数(只计触发)。
     */
    private fun convertAllTaggedDiamonds(player: Player, extraCount: Int = 0) {
        var triggerCount = extraCount
        var giveCount = 0
        val inventory = player.inventory

        // cursor:原地清除标签,物品不消失 → 计入触发,不计入给予
        val cursor = player.itemOnCursor
        if (cursor.type == Material.DIAMOND && cursor.isUnpickedDiamond()) {
            triggerCount += cursor.amount
            cursor.setUnpickedDiamond(false)
            player.setItemOnCursor(cursor)
        }

        // 背包(主36 + offhand):先清掉所有带标签钻石 → 计入触发 + 给予
        for (slot in 0 until inventory.size) {
            val item = inventory.getItem(slot) ?: continue
            if (item.type == Material.DIAMOND && item.isUnpickedDiamond()) {
                triggerCount += item.amount
                giveCount += item.amount
                inventory.setItem(slot, null)
            }
        }

        if (triggerCount == 0) return

        // 按数量触发行为(禁用/阈值时内部直接返回,转换照做)
        val team = getTeam(player)
        if (team != null && team.isInUse && !team.isEliminated) {
            repeat(triggerCount) { triggerDiamondBehavior(player, team) }
        }

        // 给予被清掉数量的普通钻石(被动合并进已有普通堆/空格)
        if (giveCount > 0) {
            givePlainDiamonds(player, giveCount)
        }
    }

    private fun givePlainDiamonds(player: Player, count: Int) {
        var remaining = count
        while (remaining > 0) {
            val amount = minOf(64, remaining)
            player.inventory.addItem(ItemStack(Material.DIAMOND, amount))
            remaining -= amount
        }
    }

    private fun ItemStack.isUnpickedDiamond(): Boolean {
        return persistentDataContainer.get(UNPICKED_DIAMOND_PDC_KEY, PersistentDataType.BOOLEAN) == true
    }

    private fun ItemStack.setUnpickedDiamond(value: Boolean) {
        editMeta {
            if (value) {
                it.persistentDataContainer.set(UNPICKED_DIAMOND_PDC_KEY, PersistentDataType.BOOLEAN, true)
            } else {
                it.persistentDataContainer.remove(UNPICKED_DIAMOND_PDC_KEY)
            }
        }
    }

    private fun triggerDiamondBehavior(player: Player, team: TeamData): Boolean {
        if (!GameStateManager.isRunning() || !DynamicSettings.diamondBehaviorEnabled) return false
        if (getTeam(player) !== team || !team.isInUse || team.isEliminated) return false

        when (DynamicSettings.diamondBehavior) {
            DiamondBehavior.REDUCE_OTHERS_LIFE -> {
                val teams = getInUseTeams().values
                if (teams.any { it.lifeCount <= DynamicSettings.diamondBehaviorDisabledThreshold }) {
                    return false
                }
                teams.filter { it !== team }.forEach { it.reduceLife(1) }
            }
            DiamondBehavior.ADD_SELF_LIFE -> {
                team.addLife(1)
            }
        }
        Bukkit.broadcast(Configs.GET_DIAMOND_MESSAGE.get().format(PLAYER_NAME_PLACEHOLDER to player.displayName()))
        player.world.playSound(
            player.location,
            "minecraft:block.note_block.bell",
            SoundCategory.BLOCKS,
            1f, 1f
        )
        Bukkit.getOnlinePlayers().forEach { p ->
            if (p.uniqueId != player.uniqueId && getTeam(p) != null) {
                p.playSound(
                    p.location,
                    "minecraft:block.note_block.bass",
                    1f, 0.5f
                )
            }
        }
        player.world.spawnParticle(
            Particle.WAX_OFF,
            player.location.x, player.location.y + 1, player.location.z,
            30,
            0.5, 0.5, 0.5,
            1.0, null, true
        )
        return true
    }
}
