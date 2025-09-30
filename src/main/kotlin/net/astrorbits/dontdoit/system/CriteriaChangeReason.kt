package net.astrorbits.dontdoit.system

import net.astrorbits.dontdoit.criteria.system.CriteriaManager

enum class CriteriaChangeReason(val weightMultiplier: (historyDistance: Int) -> Double) {
    /**
     * 自动切换词条
     */
    AUTO(CriteriaManager::generalDuplicatedMultiplier),
    /**
     * 猜中词条导致的切换词条
     */
    GUESS_SUCCESS(CriteriaManager::guessedDuplicatedMultiplier),
    /**
     * 猜错词条导致的切换词条
     */
    GUESS_FAILED(CriteriaManager::guessedDuplicatedMultiplier),
    /**
     * 触发词条导致的切换词条
     */
    TRIGGERED(CriteriaManager::generalDuplicatedMultiplier),
    /**
     * 由游戏阶段切换引发的切换词条，包括：
     * - 游戏开始时分配初始词条
     * - 游戏结束时取消词条
     * - 被淘汰时取消词条
     */
    GAME_STAGE_CHANGE(CriteriaManager::constMultiplier),
    /**
     * 手动切换词条
     */
    MANUAL(CriteriaManager::constMultiplier);

    fun isGuess(): Boolean = this == GUESS_SUCCESS || this == GUESS_FAILED
}
