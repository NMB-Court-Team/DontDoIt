package net.astrorbits.dontdoit.system

import net.astrorbits.dontdoit.Configs

enum class DiamondBehavior {
    REDUCE_OTHERS_LIFE,
    ADD_SELF_LIFE;

    fun getDescription(): String {
        return Configs.DIAMOND_BEHAVIOR_DESCRIPTION.get()[this]!!
    }
}