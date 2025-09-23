package net.astrorbits.dontdoit.criteria.helper

enum class YLevelType(val groundOffset: Int, val belowBorder: Boolean) {
    ABOVE_HIGH_ALTITUDE(10, false),
    BELOW_HIGH_ALTITUDE(10, true),
    ABOVE_GROUND(0, false),
    BELOW_GROUND(0, true)
}