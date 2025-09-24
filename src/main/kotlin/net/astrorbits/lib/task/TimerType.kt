package net.astrorbits.lib.task

enum class TimerType(val direction: Int) {
    TIMING(1),
    COUNTDOWN(-1);
}