package app.lawnchair.smartspace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.lawnchair.preferences2.PreferenceManager2
import com.android.launcher3.R

/**
 * Offline Soul Space surface.
 *
 * The bundled starter content is original reflection copy, not scripture or an attributed quotation.
 * Source-specific scripture/writing packs must be verified and licensed before being displayed.
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
    private var receiverRegistered = false
    private val quoteView = TextView(context)
    private val sourceView = TextView(context)
    private val hintView = TextView(context)

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_USER_PRESENT) return
            val prefs = PreferenceManager2.getInstance(this@SoulSpaceView.context).getCachedPreferences()
            val schedule = prefs[stringPreferencesKey("soul_space_schedule")] ?: "each_unlock"
            if (schedule == "each_unlock") showNextReflection()
        }
    }

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
            maxLines = 4
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(
                context,
                unlockReceiver,
                IntentFilter(Intent.ACTION_USER_PRESENT),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            receiverRegistered = true
        }
    }

    override fun onDetachedFromWindow() {
        if (receiverRegistered) {
            runCatching { context.unregisterReceiver(unlockReceiver) }
            receiverRegistered = false
        }
        super.onDetachedFromWindow()
    }

    private fun showNextReflection() {
        currentIndex = (currentIndex + 1) % reflections.size
        updateReflection()
    }

    private fun updateReflection() {
        val prefs = PreferenceManager2.getInstance(context).getCachedPreferences()
        val language = prefs[stringPreferencesKey("soul_space_language")] ?: "bengali_english"
        val schedule = prefs[stringPreferencesKey("soul_space_schedule")] ?: "each_unlock"
        val generalEnabled = prefs[booleanPreferencesKey("soul_space_source_general")] ?: true
        val scriptureSourcesSelected = listOf(
            "soul_space_source_quran",
            "soul_space_source_hindu",
            "soul_space_source_buddhist",
            "soul_space_source_sikh",
            "soul_space_source_sufi",
        ).any { key -> prefs[booleanPreferencesKey(key)] == true }

        if (!generalEnabled) {
            quoteView.text = if (scriptureSourcesSelected) {
                context.getString(R.string.soul_space_sources_unavailable)
            } else {
                context.getString(R.string.soul_space_no_source_selected)
            }
            sourceView.text = context.getString(R.string.soul_space_source_pack_status)
        } else {
            val reflection = reflections[currentIndex]
            quoteView.text = when (language) {
                "bengali" -> reflection.bengali
                "english" -> reflection.english
                "original_bengali" -> reflection.bengali
                else -> reflection.bengali + "\n" + reflection.english
            }
            sourceView.text = if (scriptureSourcesSelected) {
                context.getString(R.string.soul_space_general_with_unavailable_sources)
            } else {
                context.getString(R.string.soul_space_source_general_status)
            }
        }

        hintView.text = if (schedule == "manual") {
            context.getString(R.string.soul_space_next_hint)
        } else {
            context.getString(R.string.soul_space_unlock_hint)
        }
        contentDescription = context.getString(
            R.string.soul_space_accessibility_current,
            quoteView.text,
            sourceView.text,
        )
    }
}
