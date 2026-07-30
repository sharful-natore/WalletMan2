import android.graphics.Color
fun main() {
    val color = Color.parseColor("#BFFFFFFF")
    val alpha = Color.alpha(color)
    val opaque = color or 0xFF000000.toInt()
    println("alpha: $alpha, opaque: $opaque")
}
