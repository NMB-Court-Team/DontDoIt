package net.astrorbits.dontdoit.criteria.helper

enum class TriggerDifficulty(val weightMultiplier: Double, val difficulty: Int) {
    /** 几乎一定会触发的自爆词条，比如：滞空，蹦跶，剩余规则>7 */
    TRIGGER_DEFINITELY(0.9, 1),
    /** 被其他玩家干扰而自爆的词条，比如：受到伤害，死亡 */
    VERY_EASY(1.0, 2),
    /** 比较容易触发的词条，比如：头顶无方块遮挡，复活 */
    EASY(1.15, 3),
    /** 常规词条，比如：脚下是安山岩，吃腐肉 */
    NORMAL(1.2, 4),
    /** 比较难触发的词条，比如：被僵尸杀死 */
    HARD(1.1, 5),
    /** 非常难触发的词条，比如：淹死 */
    VERY_HARD(0.5, 6),
    /** 几乎不可能触发的词条，比如：在逃离河豚时被海豚杀死 (directEntity = dolphin, causeEntity = pufferfish) */
    IMPOSSIBLE(0.1, 7)
}