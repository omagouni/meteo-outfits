package com.example.myapplication.domain

import java.util.TimeZone

object LocalDays {
    private const val MILLIS_PER_DAY = 86_400_000L

    /** Jour courant (jours depuis l'epoch) dans le fuseau du téléphone. */
    fun today(nowMillis: Long = System.currentTimeMillis()): Long {
        val offset = TimeZone.getDefault().getOffset(nowMillis)
        return Math.floorDiv(nowMillis + offset, MILLIS_PER_DAY)
    }
}
