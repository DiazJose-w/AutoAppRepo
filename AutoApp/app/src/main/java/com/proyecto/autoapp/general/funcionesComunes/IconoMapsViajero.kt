package com.proyecto.autoapp.general.funcionesComunes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.core.graphics.createBitmap

fun IconoMapsViajero(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

    // Tamaño deseado del marker en dp (tú lo tenías en 38dp)
    val desiredDp = 38f
    val density = context.resources.displayMetrics.density
    val sizePx = (desiredDp * density).toInt()

    // Bitmap cuadrado donde pintaremos la imagen base
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Dibujamos el vector escalado
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(canvas)

    // >> AQUÍ HACEMOS EL CÍRCULO <<
    val output = createBitmap(sizePx, sizePx)
    val canvasCircle = Canvas(output)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val path = Path()

    // Círculo perfecto
    path.addCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, Path.Direction.CCW)

    canvasCircle.clipPath(path)
    canvasCircle.drawBitmap(bitmap, 0f, 0f, paint)

    return BitmapDescriptorFactory.fromBitmap(output)
}


