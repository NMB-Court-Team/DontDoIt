package net.astrorbits.lib.item

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object ItemHelper {
    fun ItemStack.editMeta(editor: (ItemMeta) -> Unit) {
        val meta = this.itemMeta
        editor(meta)
        this.setItemMeta(meta)
    }

    fun ItemStack.getBoolPdc(key: NamespacedKey): Boolean? {
        return this.persistentDataContainer.get(key, PersistentDataType.BOOLEAN)
    }

    fun ItemStack.getStringPdc(key: NamespacedKey): String? {
        return this.persistentDataContainer.get(key, PersistentDataType.STRING)
    }
}