package app.lawnchair.smartspace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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
            "একটু থামুন। সবকিছু একসঙ্গে সমাধান করতে হবে না。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Let kindness guide your next step, even when the whole path is unclear.",
            "পুরো পথ স্পষ্ট না হলেও, পরের পদক্ষেপটি সদয়ভাবে নিন。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "A difficult moment is part of your story, not the whole of it.",
            "কঠিন একটি মুহূর্ত আপনার জীবনের অংশ—পুরো জীবন নয়。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Make room for gratitude, patience, and a little quiet.",
            "কৃতজ্ঞতা, ধৈর্য আর একটু নীরবতার জন্য জায়গা রাখুন。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Progress can be quiet. A small, steady step still moves you forward.",
            "অগ্রগতি নীরবও হতে পারে। ছোট কিন্তু নিয়মিত পদক্ষেপও আপনাকে এগিয়ে নেয়。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "You can begin again without denying what yesterday taught you.",
            "গতকাল যা শিখিয়েছে তা অস্বীকার না করেও আপনি নতুন করে শুরু করতে পারেন。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Listen before answering; understanding is also a form of kindness.",
            "উত্তর দেওয়ার আগে শুনুন; বোঝার চেষ্টা করাও এক ধরনের সদয়তা。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Rest is not failure. Give yourself room to recover and continue.",
            "বিশ্রাম ব্যর্থতা নয়। নিজেকে সেরে ওঠার সময় দিন, তারপর এগিয়ে চলুন。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Choose what is honest and helpful, even when it is not the easiest choice.",
            "সবচেয়ে সহজ না হলেও, সত্যনিষ্ঠ ও কল্যাণকর পথটি বেছে নিন。",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "A calmer mind can see choices that fear keeps hidden.",
            "শান্ত মন এমন পথও দেখতে পায়, যা ভয় আড়াল করে রাখে。",
            "Original Soul Space reflection", null),
    )

    private val sourcedWisdom = listOf(
        WisdomItem("soul_space_source_quran", "Qur’an",
            "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "With hardship, there is surely ease. With hardship, there is surely ease.",
            "নিশ্চয়ই কষ্টের সঙ্গে স্বস্তি রয়েছে। নিশ্চয়ই কষ্টের সঙ্গেই স্বস্তি রয়েছে。",
            "Surah Ash-Sharh 94:5–6", "https://quran.com/94/5-6"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            "God does not place on a soul a burden beyond its capacity.",
            "আল্লাহ কাউকে তার সাধ্যের অতিরিক্ত দায়িত্ব দেন না。",
            "Surah Al-Baqarah 2:286", "https://quran.com/2/286"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            "Truly, hearts find calm in the remembrance of God.",
            "জেনে রেখো, আল্লাহর স্মরণেই অন্তর প্রশান্তি পায়。",
            "Surah Ar-Ra’d 13:28", "https://quran.com/13/28"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ",
            "Do not lose hope in the mercy of God.",
            "আল্লাহর রহমত থেকে নিরাশ হয়ো না。",
            "Surah Az-Zumar 39:53", "https://quran.com/39/53"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            "Seek help through patience and prayer.",
            "ধৈর্য ও সালাতের মাধ্যমে সাহায্য প্রার্থনা করো。",
            "Surah Al-Baqarah 2:153", "https://quran.com/2/153"),

        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
            "Your concern is with action, never with its fruits. Do not make the fruits your motive, and do not become attached to inaction.",
            "তোমার অধিকার কর্মে, কখনোই তার ফলে নয়। কর্মফলকে একমাত্র উদ্দেশ্য কোরো না, আবার অকর্মেও আসক্ত হয়ো না。",
            "Bhagavad Gita 2.47",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=2&field_nsutra_value=47"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "मात्रास्पर्शास्तु कौन्तेय शीतोष्णसुखदुःखदाः।\nआगमापायिनोऽनित्यास्तांस्तितिक्षस्व भारत॥",
            "Pleasure and pain come and go, like heat and cold; they are passing experiences, so learn to endure them.",
            "সুখ-দুঃখ, শীত-উষ্ণতা আসে আবার চলে যায়; এগুলো স্থায়ী নয়—ধৈর্য ধরে সহ্য করতে শেখো。",
            "Bhagavad Gita 2.14",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=2&field_nsutra_value=14"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।",
            "Lift yourself through your own effort; do not let yourself fall into discouragement.",
            "নিজের প্রচেষ্টায় নিজেকে উন্নত করো; নিজেকে হতাশার দিকে ঠেলে দিও না。",
            "Bhagavad Gita 6.5 (opening)",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=6&field_nsutra_value=5"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "अद्वेष्टा सर्वभूतानां मैत्रः करुण एव च।",
            "Be free from hatred; meet all beings with friendliness and compassion.",
            "কারও প্রতি বিদ্বেষ নয়; সকল প্রাণীর প্রতি বন্ধুত্ব ও করুণা রাখো。",
            "Bhagavad Gita 12.13 (opening)",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=12&field_nsutra_value=13"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "यदा यदा हि धर्मस्य ग्लानिर्भवति भारत।",
            "Whenever righteousness declines, a restoring force arises.",
            "যখন ধর্ম ও ন্যায়ের অবক্ষয় ঘটে, তখন তাকে পুনঃপ্রতিষ্ঠার শক্তি আবির্ভূত হয়。",
            "Bhagavad Gita 4.7 (opening)",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=4&field_nsutra_value=7"),

        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Manopubbaṅgamā dhammā, manoseṭṭhā manomayā;\nmanasā ce paduṭṭhena, bhāsati vā karoti vā;\ntato naṃ dukkhamanveti, cakkaṃva vahato padaṃ.",
            "Mind leads the way; mind is foremost, and experience is shaped by mind. If one speaks or acts with a troubled mind, suffering follows, as a cart-wheel follows the ox’s foot.",
            "মনই আগে চলে; মনই প্রধান, আর মন থেকেই অভিজ্ঞতা গড়ে ওঠে। কলুষিত মনে কথা বললে বা কাজ করলে দুঃখ পিছু নেয়—যেমন গরুর পিছু চাকার চিহ্ন。",
            "Dhammapada 1.1", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),
        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Na hi verena verāni, sammantīdha kudācanaṃ;\naverena ca sammanti, esa dhammo sanantano.",
            "Hatred is never ended by hatred; it is brought to an end by non-hatred. This is an enduring principle.",
            "বিদ্বেষ দিয়ে কখনো বিদ্বেষের অবসান হয় না; বিদ্বেষহীনতায় তার অবসান ঘটে—এ এক চিরন্তন সত্য。",
            "Dhammapada 1.5", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),
        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Akkodhena jine kodhaṃ, asādhuṃ sādhunā jine;\njine kadariyaṃ dānena, saccena alikavādinaṃ.",
            "Overcome anger with calm, harmfulness with goodness, stinginess with generosity, and falsehood with truth.",
            "শান্তি দিয়ে ক্রোধকে, সৎকর্ম দিয়ে অসৎকে, দান দিয়ে কৃপণতাকে এবং সত্য দিয়ে মিথ্যাকে জয় করো。",
            "Dhammapada 17.3 (verse 223)", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),
        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Sabba pāpassa akaraṇaṃ, kusalassa upasampadā;\nsacittapariyodapanaṃ—etaṃ buddhāna sāsanaṃ.",
            "Avoid harmful actions, cultivate what is wholesome, and purify the mind: this is the teaching of the awakened.",
            "অকল্যাণ থেকে বিরত থাকো, কল্যাণ চর্চা করো এবং মনকে নির্মল করো—এটাই জাগ্রতদের শিক্ষা。",
            "Dhammapada 14.4 (verse 183)", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),

        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ੴ ਸਤਿ ਨਾਮੁ ਕਰਤਾ ਪੁਰਖੁ ਨਿਰਭਉ ਨਿਰਵੈਰੁ ਅਕਾਲ ਮੂਰਤਿ ਅਜੂਨੀ ਸੈਭੰ ਗੁਰ ਪ੍ਰਸਾਦਿ ॥",
            "One Universal Creator; Truth is the Name; the Creator is present. Without fear or enmity, timeless, unborn, self-existent—known by the Guru’s grace.",
            "এক ওংকার; সত্য তাঁর নাম, তিনিই সৃষ্টিকর্তা। তিনি নির্ভয়, নির্বৈর, কালাতীত, অজন্মা ও স্বয়ংপ্রকাশিত—গুরুর কৃপায় উপলব্ধ。",
            "Mool Mantar • Ang 1",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=1&g=2&h=1&k=&p=0&r=0&t=0"),
        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ਸਭਨਾ ਜੀਆ ਕਾ ਇਕੁ ਦਾਤਾ ਸੋ ਮੈ ਵਿਸਰਿ ਨ ਜਾਈ ॥",
            "There is one giver for all beings; may I never forget that source of life.",
            "সব জীবের দাতা এক; সেই পরম দাতাকে যেন কখনো ভুলে না যাই。",
            "Guru Granth Sahib, Ang 2 • Japji Sahib",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=2&g=2&h=1&k=&p=0&r=0&t=0"),
        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ਹੁਕਮਿ ਰਜਾਈ ਚਲਣਾ ਨਾਨਕ ਲਿਖਿਆ ਨਾਲਿ ॥",
            "Walk in harmony with the Divine Order; this is written into the way of life.",
            "পরম বিধানের সঙ্গে সামঞ্জস্য রেখে চলাই জীবনের পথ—এই বোধ অন্তরে ধারণ করো。",
            "Guru Granth Sahib, Ang 1 • Japji Sahib",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=1&g=2&h=1&k=&p=0&r=0&t=0"),
        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ਮਿਠਤੁ ਨੀਵੀ ਨਾਨਕਾ ਗੁਣ ਚੰਗਿਆਈਆ ਤਤੁ ॥",
            "Humility sweetens a person; it is the essence of goodness.",
            "বিনয় মানুষকে মধুর করে; এটি মহৎ গুণের সার。",
            "Guru Granth Sahib, Ang 470",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=470&g=2&h=1&k=&p=0&r=0&t=0"),

        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "بشنو این نی چون شکایت می‌کند\nاز جدایی‌ها حکایت می‌کند",
            "Listen to this reed: it tells its tale of separation, voicing its longing.",
            "এই নলখাগড়ার বাঁশির কথা শোনো—সে বিচ্ছেদের কাহিনি বলে, বিচ্ছেদের বেদনাই জানায়。",
            "Mathnawi, Book I • opening couplet", "https://www.masnavi.net/1/50/eng/1/"),
        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "هر کسی کو دور ماند از اصل خویش\nباز جوید روزگار وصل خویش",
            "Whoever is separated from their origin longs for the time of reunion.",
            "যে নিজের মূল থেকে বিচ্ছিন্ন, সে আবার মিলনের সময় ফিরে পেতে আকুল হয়。",
            "Mathnawi, Book I • opening section", "https://www.masnavi.net/1/50/eng/1/"),
        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "آتش عشق است کاندر نی فتاد\nجوشش عشق است کاندر می فتاد",
            "It is the fire of love that has entered the reed; it is love that gives the wine its fervour.",
            "প্রেমের আগুনই নলখাগড়ার বাঁশিতে প্রবেশ করেছে; প্রেমই মদের উচ্ছ্বাস জাগায়。",
            "Mathnawi, Book I • opening section", "https://www.masnavi.net/1/50/eng/1/"),
        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "خاموش که بس مست و خرابیم ای عشق",
            "Be still, O Love; we are already overcome and undone by you.",
            "নীরব হও, হে প্রেম—তোমার আবেশে আমরা এমনিতেই আপ্লুত ও আত্মবিস্মৃত。",
            "Rumi • Masnavi, rendering for reflection", "https://www.masnavi.net/1/50/eng/1/"),
    )

    private var currentIndex = 0
    private var receiverRegistered = false
    private val quoteView = TextView(context)
    private val sourceView = TextView(context)
    private val cardContainer = LinearLayout(context)
    private val cardBackground = GradientDrawable()

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
        // Reserve a stable Smartspace slot so a long quote cannot collide with the clock below.
        // The visible card itself remains wrap_content and grows only as much as its text needs.
        orientation = VERTICAL
        gravity = Gravity.TOP
        // Avoid clipping long Bengali/English text inside this custom view.
        clipChildren = false
        clipToPadding = false
        setPadding(0, 0, 0, 0)
        minimumHeight = (1200 * resources.displayMetrics.density).toInt()

        cardContainer.clipChildren = false
        cardContainer.clipToPadding = false
        cardContainer.orientation = VERTICAL
        cardContainer.gravity = Gravity.CENTER_VERTICAL
        cardContainer.background = cardBackground
        cardContainer.setPadding(
            (14 * resources.displayMetrics.density).toInt(),
            (12 * resources.displayMetrics.density).toInt(),
            (14 * resources.displayMetrics.density).toInt(),
            (12 * resources.displayMetrics.density).toInt(),
        )
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES

        quoteView.apply {
            textSize = 18f
            setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            maxLines = Int.MAX_VALUE
            ellipsize = null
            setHorizontallyScrolling(false)
            includeFontPadding = true
        }
        sourceView.apply {
            textSize = 12f
            alpha = 1f
            maxLines = Int.MAX_VALUE
            ellipsize = null
            setHorizontallyScrolling(false)
            includeFontPadding = true
            setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, 0)
            isClickable = true
            isFocusable = true
        }

        val savedIndex = PreferenceManager2.getInstance(context).getCachedPreferences()[intPreferencesKey("soul_space_rotation_index")] ?: 0
        currentIndex = savedIndex
        cardContainer.addView(quoteView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        cardContainer.addView(sourceView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        addView(cardContainer, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
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
        val density = resources.displayMetrics.density
        val backgroundColor = prefs[intPreferencesKey("soul_space_card_background")] ?: Color.rgb(23, 37, 84)
        val textColor = prefs[intPreferencesKey("soul_space_card_text_color")] ?: Color.WHITE
        val referenceColor = prefs[intPreferencesKey("soul_space_card_reference_color")] ?: Color.rgb(253, 211, 77)
        val radius = prefs[intPreferencesKey("soul_space_card_radius")] ?: 18
        val fontSize = prefs[intPreferencesKey("soul_space_card_font_size")] ?: 18
        val fontStyle = prefs[stringPreferencesKey("soul_space_card_font_style")] ?: "normal"

        cardBackground.shape = GradientDrawable.RECTANGLE
        cardBackground.setColor(backgroundColor)
        cardBackground.cornerRadius = radius * density
        quoteView.setTextColor(textColor)
        sourceView.setTextColor(referenceColor)
        quoteView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        quoteView.typeface = when (fontStyle) {
            "serif" -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            "medium" -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            "bold" -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            "italic" -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            else -> Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        // wrap_content lets the card grow with the chosen font size and every line of text.
        cardContainer.minimumHeight = 0
        cardContainer.requestLayout()
        requestLayout()
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
        // Re-measure after text/language changes so the card follows the full content.
        quoteView.requestLayout()
        sourceView.requestLayout()
        cardContainer.requestLayout()
        requestLayout()
    }
}
