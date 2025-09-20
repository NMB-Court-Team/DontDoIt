package net.astrorbits.doNotDoIt.criteria

enum class CriteriaType {
    BREAK_BLOCK,
    PLACE_BLOCK,
    STEP_ON,
    USED_ITEM,
    DROP_ITEM,
    PICK_UP,
    CRAFT,
    DAMAGE,
    DEATH,
    RECEIVE_DAMAGE,
    WALK,
    KILL,
    FALL,
    JUMP,
    ENTITY_DISTANCE,
    ANGLE_PITCH,
    MAINHAND,
    OFFHAND,
    HEALTH,
    HUNGER,
    SURROUND_BY,
    INVENTORY_ITEM,
    POSITIONED_ON,
    MOVE_TIME_IDLE,
    SPRINT_TIME_IDLE,
    SNEAK_TIME_DURATION,
    REVIVE_TIME_IDLE,
    JUMP_TIME_IDLE,
    ANGLE_CHANGE_TIME_IDLE
}

data class CriteriaData(
    val type: CriteriaType,
    val parms: List<String>
)
