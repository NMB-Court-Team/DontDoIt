package net.astrorbits.dontdoit.criteria.helper

import net.astrorbits.dontdoit.Configs

enum class YLevelType(val groundOffsetGetter: () -> Int, val belowBorder: Boolean, val typeNameGetter: () -> String) {
    ABOVE_ON_AIR({ Configs.Y_LEVEL_OFFSET_ON_AIR.get() }, false, { Configs.Y_LEVEL_CRITERIA_ON_AIR.get() }),
    BELOW_ON_AIR({ Configs.Y_LEVEL_OFFSET_ON_AIR.get() }, true, { Configs.Y_LEVEL_CRITERIA_ON_AIR.get() }),
    ABOVE_GROUND({ Configs.Y_LEVEL_OFFSET_GROUND.get() }, false, { Configs.Y_LEVEL_CRITERIA_GROUND.get() }),
    BELOW_GROUND({ Configs.Y_LEVEL_OFFSET_GROUND.get() }, true, { Configs.Y_LEVEL_CRITERIA_GROUND.get() });
}