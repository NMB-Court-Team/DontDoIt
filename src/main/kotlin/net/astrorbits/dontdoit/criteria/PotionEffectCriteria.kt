package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffectType

class PotionEffectCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.POTION_EFFECT
    lateinit var effectTypes: Set<PotionEffectType>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setPotionEffectTypes(EFFECT_KEY) { potionEffectTypes, isWildcard ->
            this.effectTypes = potionEffectTypes
            this.isWildcard = isWildcard
        }
    }

    @EventHandler
    fun onPlayerGainEffect(event: EntityPotionEffectEvent) {
        val player = event.entity as? Player ?: return
        if (isWildcard || event.newEffect?.type in effectTypes) {
            trigger(player)
        }
    }

    companion object {
        const val EFFECT_KEY = "effect"
    }
}