package net.astrorbits.dontdoit.system

enum class CriteriaChangeReason {
    /**
     * 自动切换词条
     */
    AUTO,
    /**
     * 猜中词条导致的切换词条
     */
    GUESS_SUCCESS,
    /**
     * 猜错词条导致的切换词条
     */
    GUESS_FAILED,
    /**
     * 触发词条导致的切换词条
     */
    TRIGGERED,
    /**
     * 由游戏阶段切换引发的切换词条，包括：
     * - 游戏开始时分配初始词条
     * - 游戏结束时取消词条
     */
    GAME_STAGE_CHANGE,
    /**
     * 手动切换词条
     */
    MANUAL;

    fun isGuess(): Boolean = this == GUESS_SUCCESS || this == GUESS_FAILED
}