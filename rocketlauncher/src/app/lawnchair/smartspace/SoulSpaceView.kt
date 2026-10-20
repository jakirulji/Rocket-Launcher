package app.lawnchair.smartspace

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.android.launcher3.R

/**
 * Initial, fully offline Soul Space surface.
 *
 * The starter reflections are original copy, not attributed scripture quotations.
 * Verified, licensed source-backed quotations will be added in a later stage.
 */
class SoulSpaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private data class Reflection(val text: String, val source: String)

    private val reflections = listOf(
        Reflection(
            "Pause for a moment. You do not have to solve everything at once.",
            "Soul Space • Daily reflection",
        ),
        Reflection(
            "Let kindness guide your next step, even when the whole path is unclear.",
            "Soul Space • Daily reflection",
        ),
        Reflection(
            "A difficult moment is part of your story, not the whole of it.",
            "Soul Space • Daily reflection",
        ),
        Reflection(
            "Make room for gratitude, patience, and a little quiet.",
            "Soul Space • Daily reflection",
        ),
    )

    private var currentIndex = 0
    private val quoteView = TextView(context)
    private val sourceView = TextView(context)
    private val hintView = TextView(context)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(
            resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_padding_start),
            (8 * resources.displayMetrics.density).toInt(),
            resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_padding_end),
            (8 * resources.displayMetrics.density).toInt(),
        )
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        contentDescription = context.getString(R.string.soul_space_accessibility)

        quoteView.apply {
            textSize = 16f
            setTypeface(typeface, Typeface.NORMAL)
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            maxLines = 3
        }
        sourceView.apply {
            textSize = 12f
            alpha = 0.78f
            setPadding(0, (8 * resources.displayMetrics.density).toInt(), 0, 0)
        }
        hintView.apply {
            text = context.getString(R.string.soul_space_next_hint)
            textSize = 11f
            alpha = 0.65f
            setPadding(0, (8 * resources.displayMetrics.density).toInt(), 0, 0)
        }

        addView(quoteView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(sourceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(hintView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        setOnClickListener { showNextReflection() }
        isClickable = true
        isFocusable = true
        updateReflection()
    }

    private fun showNextReflection() {
        currentIndex = (currentIndex + 1) % reflections.size
        updateReflection()
    }

    private fun updateReflection() {
        val reflection = reflections[currentIndex]
        quoteView.text = reflection.text
        sourceView.text = reflection.source
        contentDescription = context.getString(
            R.string.soul_space_accessibility_current,
            reflection.text,
            reflection.source,
        )
    }
}
