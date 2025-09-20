# DoNotDoIt
Minecraft paper 1.21.8 minigame plugin

# Data
CriteriaListener: Listener //抽象准则类
    TYPE: CriteriaType //用于初步判断队伍目前分配的任务是不是一样的
    parms: List<String> //判断触发内容是不是队伍分配的
    displayName: String //

TeamData: color:TeamColor, member:set<UUID>, criteriaData:CriteriaData //队伍颜色 队员 准则数据
CriteriaData: Pair<type:CriteriaType, parm:String> //准则类型，准则参数
TeamManager: 
    -join(player, color) //
    -remove() //

# InGame
## Preparation :Listener //准备阶段
    onPlayerJoin( player -> send message )
    onDropItem(color, player -> join team(color , player.UUID))
    onClickInventory -> cancel()
    register(plugin: JavaPlugin) //注册监听器