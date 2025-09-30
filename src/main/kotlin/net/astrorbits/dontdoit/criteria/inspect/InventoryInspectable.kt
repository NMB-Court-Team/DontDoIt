package net.astrorbits.dontdoit.criteria.inspect

import net.astrorbits.dontdoit.system.team.TeamData

/**
 * 实现该接口的准则可以参与仓检
 */
interface InventoryInspectable {
    /**
     * 在即将为队伍绑定下一个词条时调用，用来修改权重
     * @param weight 当前权重
     * @param bindTarget 绑定的目标队伍
     * @param context 上下文，保存了玩家背包物品信息、附近的物品和实体信息、附近的伤害类型信息
     */
    fun modifyWeight(weight: Double, bindTarget: TeamData, context: InventoryInspectContext): Double
}