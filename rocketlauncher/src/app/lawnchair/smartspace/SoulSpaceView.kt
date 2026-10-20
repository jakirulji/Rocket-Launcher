package app.lawnchair.smartspace

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.datastore.preferences.core.stringPreferencesKey
import app.lawnchair.preferences2.PreferenceManager2
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

    private data class Reflection(val english: String, val bengali: String)

    private val reflections = listOf(
        Reflection(
            "Pause for a moment. You do not have to solve everything at once.",
            "একটু থামুন। সবকিছু একসঙ্গে সমাধান করতে হবে না।",
        ),
        Reflection(
            "Let kindness guide your next step, even when the whole path is unclear.",
            "পুরো পথ স্পষ্ট না হলেও, পরের পদক্ষেপটি সদয়ভাবে নিন।",
        ),
        Reflection(
            "A difficult moment is part of your story, not the whole of it.",
            "কঠিন একটি মুহূর্ত আপনার জীবনের অংশ—পুরো জীবন নয়।",
        ),
        Reflection(
            "Make room for gratitude, patience, and a little quiet.",
            "কৃতজ্ঞতা, ধৈর্য আর একটু নীরবতার জন্য জায়গা রাখুন।",
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
        val preferences = PreferenceManager2.getInstance(context).getCachedPreferences()
        val language = preferences[stringPreferencesKey("soul_space_language")] ?: "bengali_english"
        val reflection = reflections[currentIndex]
        val displayedText = when (language) {
            "bengali" -> reflection.bengali
            "english" -> reflection.english
            "original_bengali" -> reflection.bengali
            else -> reflection.bengali + "\n" + reflection.english
        }
        val source = context.getString(R.string.soul_space_original_reflection)
        quoteView.text = displayedText
        sourceView.text = source
        contentDescription = context.getString(
            R.string.soul_space_accessibility_current,
            displayedText,
            source,
        )
    }
}
