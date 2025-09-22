package net.astrorbits.dontdoit.criteria.enums

enum class TriggerDifficulty {
    TRIGGER_DEFINITELY,  // 几乎一定会触发的自爆词条，比如：滞空，蹦跶
    VERY_EASY,  // 被其他玩家干扰而自爆的词条，比如：受到伤害，死亡
    EASY,  // 比较容易触发的词条，比如：头顶无方块遮挡，复活
    NORMAL,  // 常规词条，比如：脚下是安山岩，吃腐肉
    HARD,  // 比较难触发的词条，比如：被僵尸杀死
    VERY_HARD,  // 非常难触发的词条，比如：淹死
    IMPOSSIBLE  // 几乎不可能触发的词条，比如：在逃离河豚时被海豚杀死 (directEntity = dolphin, causeEntity = pufferfish)
}