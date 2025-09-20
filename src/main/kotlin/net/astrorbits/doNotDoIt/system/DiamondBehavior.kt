package net.astrorbits.doNotDoIt.system

import net.kyori.adventure.text.Component

enum class DiamondBehavior(val displayName: Component) {
    REDUCE_OTHERS_LIFE(Component.text("减少其他队伍1点生命值")),
    ADD_SELF_LIFE(Component.text("增加自己队伍1点生命值"))
}