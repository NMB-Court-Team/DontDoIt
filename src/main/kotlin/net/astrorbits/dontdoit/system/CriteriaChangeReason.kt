package net.astrorbits.dontdoit.system

enum class CriteriaChangeReason {
    /**
     * 自动切换词条
     */
    AUTO,
    /**
     * 猜中词条导致的切换词条
     */
    GUESSED,
    /**
     * 触发词条导致的切换词条
     */
    TRIGGERED,
    /**
     * 游戏生命周期引发的切换词条，包括：
     * - 游戏开始时分配初始词条
     * - 游戏结束时取消词条
     */
    LIFECYCLE;
}