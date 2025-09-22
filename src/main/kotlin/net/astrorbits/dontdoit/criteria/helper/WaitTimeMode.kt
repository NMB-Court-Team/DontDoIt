package net.astrorbits.dontdoit.criteria.helper

enum class WaitTimeMode {
    /**
     * 在事件结束时检查事件持续时间
     */
    DELAY,
    /**
     * 在事件存在的时候持续检查事件持续时间
     */
    STAY
}