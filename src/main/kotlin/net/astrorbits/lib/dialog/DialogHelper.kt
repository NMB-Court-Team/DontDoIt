package net.astrorbits.lib.dialog

import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput.OptionEntry
import net.kyori.adventure.text.Component

@Suppress("UnstableApiUsage")
object DialogHelper {
    fun <E : Enum<E>> createOptionEntries(enums: Collection<E>, initial: E, idGetter: (E) -> String = { it.name.lowercase() }, textGetter: (E) -> Component): List<OptionEntry> {
        return enums.map { OptionEntry.create(idGetter(it), textGetter(it), it == initial) }
    }


}