package mx.edu.utng.arg.recicladoloresfirst.util

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * Extensiones de Kotlin para simplificar código común.
 *
 * Las extensiones nos permiten "agregar métodos" a clases existentes.
 */

// ============ EXTENSIONES PARA STRING ============

/**
 * Valida si un email tiene formato válido.
 * Ejemplo: "juan@gmail.com".isValidEmail() → true
 */
fun String.isValidEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Valida si un teléfono tiene formato válido (10 dígitos).
 */
fun String.isValidPhone(): Boolean {
    return this.matches(Regex("^[0-9]{10}$"))
}

/**
 * Capitaliza la primera letra de cada palabra.
 * Ejemplo: "juan pérez".toTitleCase() → "Juan Pérez"
 */
fun String.toTitleCase(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}

// ============ EXTENSIONES PARA DATE ============

/**
 * Formatea fecha a string legible.
 * Ejemplo: Date().toFormattedString() → "27 Oct 2025"
 */
fun Date.toFormattedString(pattern: String = "dd MMM yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale("es", "MX"))
    return formatter.format(this)
}

/**
 * Formatea fecha y hora.
 * Ejemplo: Date().toFormattedDateTime() → "27 Oct 2025, 10:30 AM"
 */
fun Date.toFormattedDateTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("es", "MX"))
    return formatter.format(this)
}

/**
 * Obtiene tiempo relativo ("hace 2 horas").
 */
fun Date.toRelativeTime(): String {
    val now = Date()
    val diff = now.time - this.time

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7

    return when {
        seconds < 60 -> "Hace un momento"
        minutes < 60 -> "Hace ${minutes}m"
        hours < 24 -> "Hace ${hours}h"
        days < 7 -> "Hace ${days}d"
        weeks < 4 -> "Hace ${weeks} semanas"
        else -> this.toFormattedString()
    }
}

// ============ EXTENSIONES PARA DOUBLE ============

/**
 * Formatea número a moneda mexicana.
 * Ejemplo: 1500.0.toMoney() → "$1,500.00"
 */
fun Double.toMoney(): String {
    return String.format(Locale("es", "MX"), "$%,.2f", this)
}

/**
 * Redondea a 2 decimales.
 */
fun Double.roundTo2Decimals(): Double {
    return String.format(Locale.US, "%.2f", this).toDouble()
}

// ============ EXTENSIONES PARA INT ============

/**
 * Formatea número de puntos.
 * Ejemplo: 1500.formatPoints() → "1,500 pts"
 */
fun Int.formatPoints(): String {
    return String.format(Locale("es", "MX"), "%,d pts", this)
}

// ============ EXTENSIONES PARA CONTEXT ============

/**
 * Muestra Toast de forma más simple.
 * Ejemplo: context.showToast("Guardado exitoso")
 */
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * Muestra Toast largo.
 */
fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// ============ EXTENSIONES PARA COLLECTIONS ============

/**
 * Suma segura de puntos (evita nulls).
 */
fun List<Int?>.sumOfPoints(): Int {
    return this.filterNotNull().sum()
}

/**
 * Agrupa y suma puntos por tipo.
 */
fun <T> List<T>.sumByInt(selector: (T) -> Int): Int {
    return this.map(selector).sum()
}