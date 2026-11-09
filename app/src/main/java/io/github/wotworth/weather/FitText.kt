package io.github.wotworth.weather

import android.content.Context
import android.text.Layout
import android.util.TypedValue
import android.view.View.MeasureSpec
import android.widget.TextView

/**
 * One line of text that shrinks to fit its width instead of being cut off (Chris, 9 Nov: the
 * temperature and the town were cut on the phone, at its font size). Never larger than
 * [maxSp] (scaled by the phone's font size, as all text is).
 */
class FitText(c: Context, private val maxSp: Float) : TextView(c) {
    init {
        isSingleLine = true
        includeFontPadding = false
        textSize = maxSp
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            val avail = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
            val maxPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maxSp, resources.displayMetrics)
            paint.textSize = maxPx
            val need = Layout.getDesiredWidth(text, paint)
            val px = if (need <= avail || need <= 0f) maxPx else maxPx * avail / need * FIT_MARGIN
            if (px != textSize) setTextSize(TypedValue.COMPLEX_UNIT_PX, px)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private companion object { const val FIT_MARGIN = 0.97f }
}
