package net.astrorbits.lib.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.astrorbits.lib.text.TextHelper.toMessage
import net.kyori.adventure.text.Component
import java.util.concurrent.CompletableFuture

class EnumArgumentType<E : Enum<E>> private constructor(val enums: Map<String, E>) : CustomArgumentType<E, String> {
    override fun parse(reader: StringReader): E {
        val enumName = reader.readUnquotedString()
        return enums[enumName] ?: throw INVALID_ENUM_NAME.create(enumName)
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return CommandHelper.suggestMatching(enums.keys, builder)
    }

    companion object {
        val INVALID_ENUM_NAME = DynamicCommandExceptionType { name -> Component.text("Invalid enum name: $name").toMessage() }

        fun <E : Enum<E>> enum(enumGetter: () -> Collection<E>, stringifier: (E) -> String = { it.name.lowercase() }): EnumArgumentType<E> {
            return EnumArgumentType(enums = enumGetter().associateBy(stringifier))
        }

        fun <E : Enum<E>> enum(enumClass: Class<E>, stringifier: (E) -> String = { it.name.lowercase() }): EnumArgumentType<E> {
            return EnumArgumentType(enumClass.enumConstants.associateBy(stringifier))
        }

        fun <E : Enum<E>> getEnum(ctx: CommandContext<CommandSourceStack>, name: String, enumClass: Class<E>): E {
            return ctx.getArgument(name, enumClass)
        }
    }
}