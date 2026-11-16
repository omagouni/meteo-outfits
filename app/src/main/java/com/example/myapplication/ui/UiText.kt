package com.example.myapplication.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Texte destiné à l'écran, produit par la couche domaine sans accès au Context.
 * Les ressources sont résolues au moment de l'affichage, donc traduites.
 */
sealed class UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText()
    data class Raw(val value: String) : UiText()
    /** Plusieurs textes joints par un séparateur (ex. "Chaud · imperméable"). */
    data class Joined(val parts: List<UiText>, val separator: String = " · ") : UiText()

    fun resolve(context: Context): String = when (this) {
        is Res -> if (args.isEmpty()) context.getString(id) else context.getString(id, *args.toTypedArray())
        is Raw -> value
        is Joined -> parts.joinToString(separator) { it.resolve(context) }
    }

    companion object {
        fun res(@StringRes id: Int, vararg args: Any) = Res(id, args.toList())
    }
}

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)
