package net.astrorbits.dontdoit.criteria.helper

import net.astrorbits.dontdoit.system.GameStateManager

interface BuiltinCriteria {
    fun shouldUse(): Boolean {
        return GameStateManager.isRunning()
    }
}