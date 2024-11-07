package com.beemer.seoulbike.view.utils

import java.util.Locale

object UnitConversion {
    fun formatDistance(distance: Double): String {
        return if (distance < 1000) {
            String.format(Locale.getDefault(), "%.1fm", distance)
        } else {
            String.format(Locale.getDefault(), "%.1fkm", distance / 1000)
        }
    }
}