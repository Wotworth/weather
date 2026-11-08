package io.github.wotworth.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import java.util.Locale

/**
 * Metro building blocks for classic Views (WX-1.1), after the Tennis app's look: black page,
 * Selawik, small all-caps app title, large lowercase headers, flat square blocks. No ripples,
 * shadows, rounded corners or Material components: presses dim the item instead.
 */
object Ui {
    enum class Font(val res: Int) { Light(R.font.selawik_light), Semilight(R.font.selawik_semilight), Regular(R.font.selawik_regular), Semibold(R.font.selawik_semibold) }

    private val fonts = HashMap<Font, Typeface>()
    fun font(c: Context, f: Font): Typeface = fonts.getOrPut(f) { ResourcesCompat.getFont(c, f.res) ?: Typeface.DEFAULT }

    fun dp(c: Context, v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.resources.displayMetrics).toInt()

    fun text(c: Context, s: CharSequence?, size: Float, f: Font = Font.Regular, color: Int = WeatherTokens.Colors.PRIMARY, spacing: Float = 0f) =
        TextView(c).apply {
            text = s
            textSize = size
            typeface = font(c, f)
            setTextColor(color)
            letterSpacing = spacing
            includeFontPadding = false
        }

    fun appTitle(c: Context) = text(c, "WEATHER", WeatherTokens.Type.APP_TITLE, Font.Semibold, spacing = WeatherTokens.Type.APP_TITLE_SPACING)

    fun section(c: Context, s: String) = text(c, s.lowercase(Locale.ROOT), WeatherTokens.Type.SECTION, Font.Semilight, WeatherTokens.Colors.SECONDARY).apply {
        setPadding(0, dp(c, WeatherTokens.Layout.SECTION_TOP), 0, dp(c, WeatherTokens.Layout.SECTION_BOTTOM))
    }

    fun icon(c: Context, res: Int, size: Float) = ImageView(c).apply {
        setImageResource(res)
        layoutParams = LinearLayout.LayoutParams(dp(c, size), dp(c, size))
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    /** No ripple: the view dims while pressed. */
    @SuppressLint("ClickableViewAccessibility")
    fun pressable(v: View, onClick: () -> Unit): View {
        v.isClickable = true
        v.setOnTouchListener { view, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.alpha = WeatherTokens.Colors.PRESSED_ALPHA
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.alpha = 1f
            }
            false
        }
        v.setOnClickListener { onClick() }
        return v
    }

    /** Windows Phone button: a white 2 dp frame; pressed, filled with the accent. */
    fun button(c: Context, label: String, onClick: () -> Unit): TextView {
        val b = text(c, label.lowercase(Locale.ROOT), WeatherTokens.Type.BUTTON, Font.Semibold)
        b.gravity = Gravity.CENTER
        val stroke = dp(c, WeatherTokens.Layout.BUTTON_BORDER)
        fun box(fill: Int) = GradientDrawable().apply { setColor(fill); setStroke(stroke, if (fill == 0) WeatherTokens.Colors.PRIMARY else fill) }
        b.background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_pressed), box(WeatherTokens.Colors.ACCENT))
            addState(intArrayOf(), box(0))
        }
        b.setPadding(dp(c, WeatherTokens.Layout.BUTTON_PAD), 0, dp(c, WeatherTokens.Layout.BUTTON_PAD), 0)
        b.minHeight = dp(c, WeatherTokens.Layout.BUTTON_HEIGHT)
        b.stateListAnimator = null
        b.isClickable = true
        b.setOnClickListener { onClick() }
        return b
    }

    /** Windows Phone text box: grey, white while typing, black text, square. */
    fun input(c: Context, hint: String, secret: Boolean): EditText = EditText(c).apply {
        this.hint = hint
        textSize = WeatherTokens.Type.INPUT
        typeface = font(c, Font.Regular)
        setTextColor(WeatherTokens.Colors.TEXT_BOX_TEXT)
        setHintTextColor(0x99000000.toInt())
        isSingleLine = true
        inputType = if (secret) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        if (secret) importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), GradientDrawable().apply { setColor(WeatherTokens.Colors.TEXT_BOX_FOCUSED) })
            addState(intArrayOf(), GradientDrawable().apply { setColor(WeatherTokens.Colors.TEXT_BOX) })
        }
        val pad = dp(c, 10f)
        setPadding(pad, 0, pad, 0)
        minHeight = dp(c, WeatherTokens.Layout.INPUT_HEIGHT)
    }

    fun row(c: Context) = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
    fun column(c: Context) = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL }

    fun gap(c: Context, h: Float) = View(c).apply { layoutParams = LinearLayout.LayoutParams(1, dp(c, h)) }

    fun wrap() = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    fun fill() = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    fun weight(w: Float = 1f) = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, w)
}
