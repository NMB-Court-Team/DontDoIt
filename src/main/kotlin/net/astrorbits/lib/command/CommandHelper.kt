package net.astrorbits.lib.command

import com.google.common.base.CharMatcher
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

object CommandHelper {
    val SUGGESTION_MATCH_PREFIX: CharMatcher = CharMatcher.anyOf("._/")

    fun suggestMatching(candidates: Iterable<String>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val remaining = builder.remaining.lowercase()

        for (candidate in candidates) {
            if (shouldSuggest(remaining, candidate.lowercase())) {
                builder.suggest(candidate)
            }
        }

        return builder.buildFuture()
    }


    fun shouldSuggest(remaining: String, candidate: String): Boolean {
        var i = 0

        while (!candidate.startsWith(remaining, i)) {
            val j = SUGGESTION_MATCH_PREFIX.indexIn(candidate, i)
            if (j < 0) {
                return false
            }

            i = j + 1
        }

        return true
    }
}