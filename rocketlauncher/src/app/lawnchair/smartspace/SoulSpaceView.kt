package app.lawnchair.smartspace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Typeface
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.lawnchair.preferences2.PreferenceManager2
import com.patrykmichalik.opto.core.setBlocking
import com.android.launcher3.R

/**
 * Offline Soul Space surface. Sourced entries show their source and reference in the visible UI.
 * Bengali and English renderings are authored by Soul Space and are labelled as such.
 */
class SoulSpaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private data class WisdomItem(
        val sourceKey: String,
        val sourceLabel: String,
        val original: String?,
        val english: String,
        val bengali: String,
        val reference: String,
        val sourceUrl: String?,
    )

    private val generalReflections = listOf(
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Pause for a moment. You do not have to solve everything at once.",
            "একটু থামুন। সবকিছু একসঙ্গে সমাধান করতে হবে না।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Let kindness guide your next step, even when the whole path is unclear.",
            "পুরো পথ স্পষ্ট না হলেও, পরের পদক্ষেপটি সদয়ভাবে নিন।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "A difficult moment is part of your story, not the whole of it.",
            "কঠিন একটি মুহূর্ত আপনার জীবনের অংশ—পুরো জীবন নয়।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Make room for gratitude, patience, and a little quiet.",
            "কৃতজ্ঞতা, ধৈর্য আর একটু নীরবতার জন্য জায়গা রাখুন।",
            "Original Soul Space reflection", null),
    )

    private val sourcedWisdom = listOf(
        WisdomItem("soul_space_source_quran", "Qur’an",
            "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "With hardship, there is surely ease. With hardship, there is surely ease.",
            "নিশ্চয়ই কষ্টের সঙ্গে স্বস্তি রয়েছে। নিশ্চয়ই কষ্টের সঙ্গেই স্বস্তি রয়েছে।",
            "Surah Ash-Sharh 94:5–6", "https://quran.com/94/5-6"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
            "Your concern is with action, never with its fruits. Do not make the fruits your motive, and do not become attached to inaction.",
            "তোমার অধিকার কর্মে, কখনোই তার ফলে নয়। কর্মফলকে একমাত্র উদ্দেশ্য কোরো না, আবার অকর্মেও আসক্ত হয়ো না।",
            "Bhagavad Gita 2.47",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=2&field_nsutra_value=47"),
        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Manopubbaṅgamā dhammā, manoseṭṭhā manomayā;\nmanasā ce paduṭṭhena, bhāsati vā karoti vā;\ntato naṃ dukkhamanveti, cakkaṃva vahato padaṃ.",
            "Mind leads the way; mind is foremost, and experience is shaped by mind. If one speaks or acts with a troubled mind, suffering follows, as a cart-wheel follows the ox’s foot.",
            "মনই আগে চলে; মনই প্রধান, আর মন থেকেই অভিজ্ঞতা গড়ে ওঠে। কলুষিত মনে কথা বললে বা কাজ করলে দুঃখ পিছু নেয়—যেমন গরুর পিছু চাকার চিহ্ন।",
            "Dhammapada 1.1", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),
        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ੴ ਸਤਿ ਨਾਮੁ ਕਰਤਾ ਪੁਰਖੁ ਨਿਰਭਉ ਨਿਰਵੈਰੁ ਅਕਾਲ ਮੂਰਤਿ ਅਜੂਨੀ ਸੈਭੰ ਗੁਰ ਪ੍ਰਸਾਦਿ ॥",
            "One Universal Creator; Truth is the Name; the Creator is present. Without fear or enmity, timeless, unborn, self-existent—known by the Guru’s grace.",
            "এক ওংকার; সত্য তাঁর নাম, তিনিই সৃষ্টিকর্তা। তিনি নির্ভয়, নির্বৈর, কালাতীত, অজন্মা ও স্বয়ংপ্রকাশিত—গুরুর কৃপায় উপলব্ধ।",
            "Mool Mantar • Ang 1",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=1&g=2&h=1&k=&p=0&r=0&t=0"),
        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "بشنو این نی چون شکایت می‌کند\nاز جدایی‌ها حکایت می‌کند",
            "Listen to this reed: it tells its tale of separation, voicing its longing.",
            "এই নলখাগড়ার বাঁশির কথা শোনো—সে বিচ্ছেদের কাহিনি বলে, বিচ্ছেদের বেদনাই জানায়।",
            "Mathnawi, Book I • opening couplet", "https://www.masnavi.net/1/50/eng/1/"),
    )

    private var currentIndex = 0
    private var receiverRegistered = false
    private val quoteView = TextView(context)
    private val sourceView = TextView(context)

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_USER_PRESENT) return
            val manager = PreferenceManager2.getInstance(this@SoulSpaceView.context)
            val prefs = manager.getCachedPreferences()
            val schedule = prefs[stringPreferencesKey("soul_space_schedule")] ?: "each_unlock"
            if (schedule == "each_unlock") {
                val items = selectedItems()
                if (items.isNotEmpty()) {
                    currentIndex = ((prefs[intPreferencesKey("soul_space_rotation_index")] ?: currentIndex) + 1) % items.size
                    manager.soulSpaceRotationIndex.setBlocking(currentIndex)
                    updateReflection()
                }
            }
        }
    }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(
            resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_padding_start),
            (5 * resources.displayMetrics.density).toInt(),
            resources.getDimensionPixelSize(R.dimen.enhanced_smartspace_padding_end),
            (5 * resources.displayMetrics.density).toInt(),
        )
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES

        quoteView.apply {
            textSize = 15f
            setTypeface(typeface, Typeface.NORMAL)
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            maxLines = 2
        }
        sourceView.apply {
            textSize = 11f
            alpha = 1f
            maxLines = 2
            setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, 0)
            isClickable = true
            isFocusable = true
        }

        val savedIndex = PreferenceManager2.getInstance(context).getCachedPreferences()[intPreferencesKey("soul_space_rotation_index")] ?: 0
        currentIndex = savedIndex
        addView(quoteView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(sourceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        setOnClickListener { showNextReflection() }
        sourceView.setOnClickListener {
            currentItem()?.sourceUrl?.let { url ->
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            }
        }
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
                ContextCompat.RECEIVER_EXPORTED,
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

    private fun selectedItems(): List<WisdomItem> {
        val prefs = PreferenceManager2.getInstance(context).getCachedPreferences()
        val selected = mutableListOf<WisdomItem>()
        if (prefs[booleanPreferencesKey("soul_space_source_general")] ?: true) {
            selected.addAll(generalReflections)
        }
        selected.addAll(sourcedWisdom.filter { item -> prefs[booleanPreferencesKey(item.sourceKey)] == true })
        return selected
    }

    private fun currentItem(): WisdomItem? {
        val items = selectedItems()
        if (items.isEmpty()) return null
        currentIndex = Math.floorMod(currentIndex, items.size)
        return items[currentIndex]
    }

    private fun showNextReflection() {
        val items = selectedItems()
        if (items.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % items.size
            PreferenceManager2.getInstance(context).soulSpaceRotationIndex.setBlocking(currentIndex)
        }
        updateReflection()
    }

    private fun applyQuoteColor() {
        val prefs = PreferenceManager2.getInstance(context).getCachedPreferences()
        val mode = prefs[stringPreferencesKey("soul_space_text_color_mode")] ?: "adaptive"
        if (mode == "custom") {
            quoteView.setTextColor(prefs[intPreferencesKey("soul_space_custom_text_color")] ?: 0xFFFFFFFF.toInt())
            return
        }
        val value = TypedValue()
        val resolved = context.theme.resolveAttribute(android.R.attr.textColorPrimary, value, true)
        if (resolved) {
            val color = if (value.resourceId != 0) {
                runCatching { context.getColor(value.resourceId) }.getOrDefault(value.data)
            } else value.data
            quoteView.setTextColor(color)
        } else {
            val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            quoteView.setTextColor(if (night) 0xFFFFFFFF.toInt() else 0xFF202124.toInt())
        }
    }

    private fun updateReflection() {
        val prefs = PreferenceManager2.getInstance(context).getCachedPreferences()
        val language = prefs[stringPreferencesKey("soul_space_language")] ?: "bengali_english"
        val item = currentItem()
        if (item == null) {
            quoteView.text = context.getString(R.string.soul_space_no_source_selected)
            sourceView.text = context.getString(R.string.soul_space_source_pack_status)
            sourceView.setOnClickListener(null)
        } else {
            quoteView.text = when (language) {
                "bengali" -> item.bengali
                "english" -> item.english
                "original_bengali" -> listOfNotNull(item.original, item.bengali).joinToString("\n")
                else -> item.bengali + "\n" + item.english
            }
            sourceView.text = if (item.sourceUrl == null) {
                "Source: ${item.sourceLabel}\nReference: ${item.reference}"
            } else {
                "Source: ${item.sourceLabel}\nReference: ${item.reference} • Open ↗"
            }
            sourceView.setOnClickListener {
                item.sourceUrl?.let { url ->
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                }
            }
            contentDescription = "${quoteView.text}. Source: ${item.sourceLabel}. Reference: ${item.reference}. Tap for next."
        }
        applyQuoteColor()
    }
}
