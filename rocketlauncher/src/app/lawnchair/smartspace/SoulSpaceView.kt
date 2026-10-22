package app.lawnchair.smartspace

import android.app.AlertDialog
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
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import app.lawnchair.preferences2.PreferenceManager2
import com.patrykmichalik.opto.core.setBlocking
import kotlinx.coroutines.runBlocking
import com.android.launcher3.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Progress can be quiet. A small, steady step still moves you forward.",
            "অগ্রগতি নীরবও হতে পারে। ছোট কিন্তু নিয়মিত পদক্ষেপও আপনাকে এগিয়ে নেয়।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "You can begin again without denying what yesterday taught you.",
            "গতকাল যা শিখিয়েছে তা অস্বীকার না করেও আপনি নতুন করে শুরু করতে পারেন।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Listen before answering; understanding is also a form of kindness.",
            "উত্তর দেওয়ার আগে শুনুন; বোঝার চেষ্টা করাও এক ধরনের সদয়তা।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Rest is not failure. Give yourself room to recover and continue.",
            "বিশ্রাম ব্যর্থতা নয়। নিজেকে সেরে ওঠার সময় দিন, তারপর এগিয়ে চলুন।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "Choose what is honest and helpful, even when it is not the easiest choice.",
            "সবচেয়ে সহজ না হলেও, সত্যনিষ্ঠ ও কল্যাণকর পথটি বেছে নিন।",
            "Original Soul Space reflection", null),
        WisdomItem("soul_space_source_general", "Soul Space original reflection", null,
            "A calmer mind can see choices that fear keeps hidden.",
            "শান্ত মন এমন পথও দেখতে পায়, যা ভয় আড়াল করে রাখে।",
            "Original Soul Space reflection", null),
    )


    // Additional carefully labelled thematic paraphrases. These are not presented as
    // verbatim translations; the linked source and precise passage are shown to users.
    private fun paraphraseItems(
        key: String,
        label: String,
        url: String,
        entries: List<Triple<String, String, String>>,
    ) = entries.map { (reference, english, bengali) ->
        WisdomItem(key, label, null, english, bengali,
            "$reference • Soul Space paraphrase", url)
    }

    private val supplementalWisdom = listOf(
        paraphraseItems("soul_space_source_quran", "Qur’an", "https://quran.com/2/152-153", listOf(
            Triple("Al-Baqarah 2:152", "Remember God with gratitude, and do not respond to blessings with denial.", "কৃতজ্ঞতার সঙ্গে আল্লাহকে স্মরণ করো; প্রাপ্ত অনুগ্রহ অস্বীকার কোরো না।"),
            Triple("Al-Baqarah 2:153", "Seek strength through patience and prayer; perseverance is not the same as being alone.", "ধৈর্য ও সালাতের মাধ্যমে শক্তি চাও; ধৈর্য ধরা মানে একা হয়ে থাকা নয়।"),
            Triple("Al-Hujurat 49:13", "Human differences are a reason to learn about one another, not to boast over one another.", "মানুষের বৈচিত্র্য পরস্পরকে জানার সুযোগ, একে অপরের ওপর শ্রেষ্ঠত্ব দেখানোর কারণ নয়।"),
            Triple("An-Nahl 16:90", "Justice, generosity, and care for kin are joined with the call to avoid wrongdoing.", "ন্যায়, উদারতা ও আত্মীয়ের প্রতি যত্নের সঙ্গে অন্যায় থেকে বিরত থাকার নির্দেশও রয়েছে।"),
            Triple("Al-Isra 17:37", "Do not walk the earth with arrogance; human power has limits.", "পৃথিবীতে অহংকার নিয়ে চলো না; মানুষের ক্ষমতার সীমা আছে।"),
            Triple("Ash-Shura 42:40", "Forgiveness and reconciliation can be greater than simply returning harm for harm.", "অন্যায়ের বদলা নেওয়ার চেয়ে ক্ষমা ও মীমাংসা অনেক সময় মহত্তর পথ।"),
            Triple("Al-Ankabut 29:69", "Those who strive sincerely for what is right are promised guidance along their way.", "যারা আন্তরিকভাবে সঠিক পথে চেষ্টা করে, তাদের পথনির্দেশের প্রতিশ্রুতি দেওয়া হয়েছে।"),
            Triple("Al-Imran 3:134", "Restraint in anger and forgiveness are marks of generosity of spirit.", "ক্রোধ সংবরণ ও ক্ষমা উদার হৃদয়ের পরিচয়।"),
            Triple("Az-Zumar 39:10", "Patience has a reward beyond easy calculation.", "ধৈর্যের প্রতিদান অপরিমেয়।"),
            Triple("Al-Ma’idah 5:8", "Do not let hostility make you abandon justice.", "কারও প্রতি বিরাগ যেন তোমাকে ন্যায়বিচার থেকে সরিয়ে না দেয়।")
        )),
        paraphraseItems("soul_space_source_hindu", "Bhagavad Gita", "https://www.gitasupersite.iitk.ac.in/", listOf(
            Triple("Bhagavad Gita 2.ang", "The wise seek steadiness amid changing circumstances.", "পরিস্থিতি বদলালেও জ্ঞানী ব্যক্তি অন্তরের স্থিরতা অনুশীলন করেন।"),
            Triple("Bhagavad Gita 2.48", "Act with balance, letting go of fixation on success and failure.", "সাফল্য ও ব্যর্থতার প্রতি অতিরিক্ত আসক্তি ছেড়ে সমভাব নিয়ে কাজ করো।"),
            Triple("Bhagavad Gita 3.19", "Do the work that should be done without clinging to its reward.", "প্রাপ্য কর্তব্য সম্পাদন করো, কিন্তু পুরস্কারের প্রতি আঁকড়ে থেকো না।"),
            Triple("Bhagavad Gita 4.38", "Wisdom is a purifier; it grows through sincere practice and understanding.", "জ্ঞান অন্তরকে শুদ্ধ করে; আন্তরিক অনুশীলন ও উপলব্ধির মাধ্যমে তা বিকশিত হয়।"),
            Triple("Bhagavad Gita 6.5", "Let the self become a support for growth rather than an obstacle.", "নিজের মনকে উন্নতির সহায় করো, বাধা নয়।"),
            Triple("Bhagavad Gita 6.26", "Whenever the restless mind wanders, gently guide it back.", "চঞ্চল মন যখনই সরে যায়, ধীরে ধীরে তাকে ফিরিয়ে আনো।"),
            Triple("Bhagavad Gita 12.13–14", "Friendliness, compassion, and freedom from possessiveness are signs of devotion.", "বন্ধুভাব, করুণা ও অধিকারবোধের আসক্তি কমানো ভক্তির লক্ষণ।"),
            Triple("Bhagavad Gita 16.2", "Nonviolence, truthfulness, and freedom from anger are qualities worth cultivating.", "অহিংসা, সত্যবাদিতা ও ক্রোধমুক্তি চর্চা করার মতো মহৎ গুণ।"),
            Triple("Bhagavad Gita 17.15", "Speech is disciplined when it is truthful, kind, beneficial, and not needlessly hurtful.", "সত্য, মঙ্গল ও কোমলতায় ভরা এবং অকারণে কষ্ট না দেওয়া কথাই সংযত বাক্য।"),
            Triple("Bhagavad Gita 18.63", "Reflect carefully, then choose with understanding and responsibility.", "ভেবে দেখো, তারপর উপলব্ধি ও দায়িত্ববোধ নিয়ে সিদ্ধান্ত নাও।")
        )),
        paraphraseItems("soul_space_source_buddhist", "Dhammapada", "https://www.gutenberg.org/ebooks/2017", listOf(
            Triple("Dhammapada 1.2", "A wholesome mind brings happiness that follows like a faithful shadow.", "কল্যাণময় মন এমন সুখ আনে, যা বিশ্বস্ত ছায়ার মতো সঙ্গ দেয়।"),
            Triple("Dhammapada 1.3–4", "Rehearsing injury keeps hostility alive; releasing it opens another path.", "অপমান আঁকড়ে থাকলে বিদ্বেষ বেঁচে থাকে; তা ছেড়ে দিলে অন্য পথ খুলে যায়।"),
            Triple("Dhammapada 2.21", "Mindful attention is a path toward the deathless; heedlessness wastes life.", "সচেতনতা অমৃতের পথে নিয়ে যায়; অসতর্কতা জীবনকে অপচয় করে।"),
            Triple("Dhammapada 5.63", "Recognizing one's lack of wisdom is the beginning of becoming wiser.", "নিজের অজ্ঞতা চিনতে পারাই প্রজ্ঞাবান হওয়ার সূচনা।"),
            Triple("Dhammapada 6.76", "Value a wise guide who points out faults as well as strengths.", "যিনি গুণের পাশাপাশি ভুলও দেখান, সেই জ্ঞানী পথপ্রদর্শককে মূল্য দাও।"),
            Triple("Dhammapada 10.129–130", "All beings fear harm and value life; remembering this supports compassion.", "সব প্রাণী আঘাতকে ভয় পায় এবং জীবনকে ভালোবাসে; এই বোধ করুণাকে শক্ত করে।"),
            Triple("Dhammapada 12.160", "You must make your own effort; no one can walk the path in your place.", "নিজের প্রচেষ্টা তোমাকেই করতে হবে; তোমার হয়ে কেউ পথ চলতে পারে না।"),
            Triple("Dhammapada 15.197–200", "Peace is possible even among those who live in hostility, if hatred is not fed.", "বিদ্বেষকে পুষ্ট না করলে বিরোধের মধ্যেও শান্তি সম্ভব।"),
            Triple("Dhammapada 20.276", "Awakened teachers show the way; practice is each person's work.", "জাগ্রত শিক্ষক পথ দেখান; অনুশীলনের দায়িত্ব প্রত্যেকের নিজের।"),
            Triple("Dhammapada 25.368", "Cultivate goodwill and the heart can become quiet and clear.", "মৈত্রী চর্চা করো; হৃদয় ধীরে ধীরে শান্ত ও স্বচ্ছ হতে পারে।")
        )),
        paraphraseItems("soul_space_source_sikh", "Guru Granth Sahib", "https://www.srigranth.org/", listOf(
            Triple("Japji Sahib, Pauri 1 • Ang 1", "Truth is not merely spoken; living truthfully is the deeper practice.", "সত্য শুধু মুখে বলা নয়; সত্যনিষ্ঠভাবে বাঁচাই গভীরতর সাধনা।"),
            Triple("Japji Sahib, Pauri 4 • Ang 2", "Love and humility shape the seeker’s approach to the Divine.", "প্রেম ও বিনয় সাধকের পরমের দিকে এগোনোর পথ গড়ে দেয়।"),
            Triple("Japji Sahib, Pauri 8–11 • Ang 3–4", "Listening deeply can transform understanding, conduct, and inner life.", "গভীরভাবে শ্রবণ উপলব্ধি, আচরণ ও অন্তর্জীবনকে বদলে দিতে পারে।"),
            Triple("Japji Sahib, Pauri 21 • Ang 4", "No single human calculation can fully measure the vastness of creation.", "মানুষের কোনো হিসাবই সৃষ্টির বিশালতাকে পুরোপুরি মাপতে পারে না।"),
            Triple("Sukhmani Sahib • Ang 262", "Remembering the Divine is presented as a source of inner steadiness.", "পরমকে স্মরণ করাকে অন্তরের স্থিরতার একটি উৎস হিসেবে তুলে ধরা হয়েছে।"),
            Triple("Ang 286", "The Divine is understood as present through all beings and places.", "সব প্রাণী ও স্থানের মধ্যে পরমের উপস্থিতি উপলব্ধির শিক্ষা রয়েছে।"),
            Triple("Ang 595", "Ego loosens when a person turns from self-importance toward the Name.", "আত্মম্ভরিতা থেকে নাম-স্মরণের দিকে ফিরলে অহংবোধ শিথিল হয়।"),
            Triple("Ang 1349", "Compassion and remembrance belong in daily life, not only in ceremony.", "করুণা ও স্মরণ শুধু আনুষ্ঠানিকতায় নয়, দৈনন্দিন জীবনেও থাকা উচিত।")
        )),
        paraphraseItems("soul_space_source_sufi", "Sufi wisdom • thematic reflections", "https://www.masnavi.net/1/50/eng/1/", listOf(
            Triple("Rumi, Mathnawi I • opening passage", "The reed's lament becomes an image of longing for reunion with one's origin.", "নলখাগড়ার আর্তি নিজের উৎসের সঙ্গে পুনর্মিলনের আকাঙ্ক্ষার প্রতীক হয়ে ওঠে।"),
            Triple("Rumi, Mathnawi I • separation motif", "Longing can reveal what the heart believes it has lost.", "আকুলতা হৃদয় কী হারিয়েছে বলে মনে করে, তা প্রকাশ করতে পারে।"),
            Triple("Rumi, Mathnawi I • love motif", "Love is portrayed as a force that changes the listener, not merely a passing feeling.", "প্রেমকে ক্ষণিক অনুভূতি নয়, মানুষকে বদলে দেওয়া শক্তি হিসেবে দেখানো হয়েছে।"),
            Triple("Attar, The Conference of the Birds • journey motif", "The search for truth also asks the seeker to examine the self.", "সত্যের অনুসন্ধান সাধককে নিজের অন্তরও পরীক্ষা করতে বলে।"),
            Triple("Saadi, Gulistan • ethical counsel", "Wisdom without kindness can fail the people it claims to guide.", "দয়াহীন প্রজ্ঞা যাদের পথ দেখানোর দাবি করে, তাদেরই ব্যর্থ করতে পারে।"),
            Triple("Al-Ghazali, ethical writings • self-examination", "Self-knowledge requires honest attention to motives, not only outward conduct.", "আত্মজ্ঞান শুধু বাহ্যিক আচরণ নয়, নিজের উদ্দেশ্যকেও সৎভাবে দেখার দাবি রাখে।"),
            Triple("Rabia al-Basri • devotional tradition", "Love of the Divine is described as deeper than bargaining for reward.", "পরমের প্রতি প্রেমকে পুরস্কারের বিনিময়-প্রত্যাশার চেয়ে গভীর বলে বর্ণনা করা হয়।"),
            Triple("Ibn Arabi • unity and perception", "Humility begins by recognizing that one's own perspective is not the whole of reality.", "নিজের দৃষ্টিভঙ্গিই সমগ্র বাস্তবতা নয়—এ কথা মানা বিনয়ের সূচনা।")
        )),
        paraphraseItems("soul_space_source_philosophy", "Philosophy & Literature • thematic paraphrase", "https://www.gutenberg.org/", listOf(
            Triple("Plato, Apology 38a", "A life without self-examination leaves important questions unasked.", "আত্মপর্যালোচনা ছাড়া জীবন গুরুত্বপূর্ণ প্রশ্নগুলোকে অনুচ্চারিত রেখে দেয়।"),
            Triple("Aristotle, Nicomachean Ethics I.7", "A good life is shaped by sustained activity and character, not a single pleasant moment.", "একটি ভালো জীবন গড়ে ওঠে ধারাবাহিক কাজ ও চরিত্রে, একটি সুখের মুহূর্তে নয়।"),
            Triple("Epictetus, Enchiridion 1", "Separate what depends on your choices from what does not.", "যা তোমার সিদ্ধান্তের ওপর নির্ভর করে, তা যা করে না—সেগুলো আলাদা করে দেখো।"),
            Triple("Marcus Aurelius, Meditations II.1", "Meet difficult people by remembering that people act from their own understanding.", "মানুষ নিজের উপলব্ধি অনুযায়ী কাজ করে—এ কথা মনে রেখে কঠিন মানুষের মুখোমুখি হও।"),
            Triple("Kierkegaard, The Sickness unto Death", "Despair can involve refusing to face who one is or who one may become.", "হতাশা কখনো নিজের বর্তমান সত্তা বা সম্ভাব্য সত্তার মুখোমুখি হতে অস্বীকার করার মধ্যেও থাকে।"),
            Triple("Dostoevsky, The Brothers Karamazov", "Responsibility toward others can be understood as a demanding form of love.", "অন্যের প্রতি দায়িত্বকে ভালোবাসার এক কঠিন অথচ গভীর রূপ হিসেবে বোঝা যায়।"),
            Triple("Tolstoy, A Confession", "Questions of meaning often return when outward success no longer satisfies.", "বাহ্যিক সাফল্য আর তৃপ্তি না দিলে জীবনের অর্থ নিয়ে প্রশ্ন ফিরে আসে।"),
            Triple("Camus, The Myth of Sisyphus", "Facing life's absurdity honestly need not mean surrendering to it.", "জীবনের অসংগতির মুখোমুখি হওয়া মানেই তার কাছে আত্মসমর্পণ করা নয়।"),
            Triple("Kafka, The Diaries • self-examination", "Inner conflict can make ordinary life feel strange, demanding careful attention.", "অন্তর্দ্বন্দ্ব সাধারণ জীবনকেও অচেনা করে তুলতে পারে; তাই মনোযোগ দিয়ে দেখা দরকার।"),
            Triple("Hermann Hesse, Siddhartha", "Another person's path may inspire you, but it cannot replace your own experience.", "অন্যের পথ অনুপ্রেরণা দিতে পারে, কিন্তু নিজের অভিজ্ঞতার বিকল্প হতে পারে না।"),
            Triple("Schopenhauer, Essays and Aphorisms", "Desire can renew dissatisfaction unless we learn to examine what we pursue.", "কী চাইছি তা বিচার না করলে আকাঙ্ক্ষা বারবার অসন্তোষ ফিরিয়ে আনতে পারে।"),
            Triple("Rilke, Letters to a Young Poet", "Some questions need time to be lived with before they can be answered.", "কিছু প্রশ্নের উত্তর পাওয়ার আগে সেগুলোর সঙ্গে সময় কাটিয়ে বাঁচতে হয়।")
        ))
    )

    private val sourcedWisdom = listOf(
        WisdomItem("soul_space_source_quran", "Qur’an",
            "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "With hardship, there is surely ease. With hardship, there is surely ease.",
            "নিশ্চয়ই কষ্টের সঙ্গে স্বস্তি রয়েছে। নিশ্চয়ই কষ্টের সঙ্গেই স্বস্তি রয়েছে।",
            "Surah Ash-Sharh 94:5–6", "https://quran.com/94/5-6"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
            "God does not place on a soul a burden beyond its capacity.",
            "আল্লাহ কাউকে তার সাধ্যের অতিরিক্ত দায়িত্ব দেন না।",
            "Surah Al-Baqarah 2:286", "https://quran.com/2/286"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            "Truly, hearts find calm in the remembrance of God.",
            "জেনে রেখো, আল্লাহর স্মরণেই অন্তর প্রশান্তি পায়।",
            "Surah Ar-Ra’d 13:28", "https://quran.com/13/28"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ",
            "Do not lose hope in the mercy of God.",
            "আল্লাহর রহমত থেকে নিরাশ হয়ো না।",
            "Surah Az-Zumar 39:53", "https://quran.com/39/53"),
        WisdomItem("soul_space_source_quran", "Qur’an",
            "يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ",
            "Seek help through patience and prayer.",
            "ধৈর্য ও সালাতের মাধ্যমে সাহায্য প্রার্থনা করো।",
            "Surah Al-Baqarah 2:153", "https://quran.com/2/153"),

        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन।\nमा कर्मफलहेतुर्भूर्मा ते सङ्गोऽस्त्वकर्मणि॥",
            "Your concern is with action, never with its fruits. Do not make the fruits your motive, and do not become attached to inaction.",
            "তোমার অধিকার কর্মে, কখনোই তার ফলে নয়। কর্মফলকে একমাত্র উদ্দেশ্য কোরো না, আবার অকর্মেও আসক্ত হয়ো না।",
            "Bhagavad Gita 2.47",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=2&field_nsutra_value=47"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "मात्रास्पर्शास्तु कौन्तेय शीतोष्णसुखदुःखदाः।\nआगमापायिनोऽनित्यास्तांस्तितिक्षस्व भारत॥",
            "Pleasure and pain come and go, like heat and cold; they are passing experiences, so learn to endure them.",
            "সুখ-দুঃখ, শীত-উষ্ণতা আসে আবার চলে যায়; এগুলো স্থায়ী নয়—ধৈর্য ধরে সহ্য করতে শেখো।",
            "Bhagavad Gita 2.14",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=2&field_nsutra_value=14"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "उद्धरेदात्मनात्मानं नात्मानमवसादयेत्।",
            "Lift yourself through your own effort; do not let yourself fall into discouragement.",
            "নিজের প্রচেষ্টায় নিজেকে উন্নত করো; নিজেকে হতাশার দিকে ঠেলে দিও না।",
            "Bhagavad Gita 6.5 (opening)",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=6&field_nsutra_value=5"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "अद्वेष्टा सर्वभूतानां मैत्रः करुण एव च।",
            "Be free from hatred; meet all beings with friendliness and compassion.",
            "কারও প্রতি বিদ্বেষ নয়; সকল প্রাণীর প্রতি বন্ধুত্ব ও করুণা রাখো।",
            "Bhagavad Gita 12.13 (opening)",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=12&field_nsutra_value=13"),
        WisdomItem("soul_space_source_hindu", "Bhagavad Gita",
            "यदा यदा हि धर्मस्य ग्लानिर्भवति भारत।",
            "Whenever righteousness declines, a restoring force arises.",
            "যখন ধর্ম ও ন্যায়ের অবক্ষয় ঘটে, তখন তাকে পুনঃপ্রতিষ্ঠার শক্তি আবির্ভূত হয়।",
            "Bhagavad Gita 4.7 (opening)",
            "https://www.gitasupersite.iitk.ac.in/srimad?choose=1&field_chapter_value=4&field_nsutra_value=7"),

        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Manopubbaṅgamā dhammā, manoseṭṭhā manomayā;\nmanasā ce paduṭṭhena, bhāsati vā karoti vā;\ntato naṃ dukkhamanveti, cakkaṃva vahato padaṃ.",
            "Mind leads the way; mind is foremost, and experience is shaped by mind. If one speaks or acts with a troubled mind, suffering follows, as a cart-wheel follows the ox’s foot.",
            "মনই আগে চলে; মনই প্রধান, আর মন থেকেই অভিজ্ঞতা গড়ে ওঠে। কলুষিত মনে কথা বললে বা কাজ করলে দুঃখ পিছু নেয়—যেমন গরুর পিছু চাকার চিহ্ন।",
            "Dhammapada 1.1", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),
        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Na hi verena verāni, sammantīdha kudācanaṃ;\naverena ca sammanti, esa dhammo sanantano.",
            "Hatred is never ended by hatred; it is brought to an end by non-hatred. This is an enduring principle.",
            "বিদ্বেষ দিয়ে কখনো বিদ্বেষের অবসান হয় না; বিদ্বেষহীনতায় তার অবসান ঘটে—এ এক চিরন্তন সত্য।",
            "Dhammapada 1.5", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),
        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Akkodhena jine kodhaṃ, asādhuṃ sādhunā jine;\njine kadariyaṃ dānena, saccena alikavādinaṃ.",
            "Overcome anger with calm, harmfulness with goodness, stinginess with generosity, and falsehood with truth.",
            "শান্তি দিয়ে ক্রোধকে, সৎকর্ম দিয়ে অসৎকে, দান দিয়ে কৃপণতাকে এবং সত্য দিয়ে মিথ্যাকে জয় করো।",
            "Dhammapada 17.3 (verse 223)", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),
        WisdomItem("soul_space_source_buddhist", "Dhammapada",
            "Sabba pāpassa akaraṇaṃ, kusalassa upasampadā;\nsacittapariyodapanaṃ—etaṃ buddhāna sāsanaṃ.",
            "Avoid harmful actions, cultivate what is wholesome, and purify the mind: this is the teaching of the awakened.",
            "অকল্যাণ থেকে বিরত থাকো, কল্যাণ চর্চা করো এবং মনকে নির্মল করো—এটাই জাগ্রতদের শিক্ষা।",
            "Dhammapada 14.4 (verse 183)", "https://www.tipitaka.org/hindi/Dhammapada.pdf"),

        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ੴ ਸਤਿ ਨਾਮੁ ਕਰਤਾ ਪੁਰਖੁ ਨਿਰਭਉ ਨਿਰਵੈਰੁ ਅਕਾਲ ਮੂਰਤਿ ਅਜੂਨੀ ਸੈਭੰ ਗੁਰ ਪ੍ਰਸਾਦਿ ॥",
            "One Universal Creator; Truth is the Name; the Creator is present. Without fear or enmity, timeless, unborn, self-existent—known by the Guru’s grace.",
            "এক ওংকার; সত্য তাঁর নাম, তিনিই সৃষ্টিকর্তা। তিনি নির্ভয়, নির্বৈর, কালাতীত, অজন্মা ও স্বয়ংপ্রকাশিত—গুরুর কৃপায় উপলব্ধ।",
            "Mool Mantar • Ang 1",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=1&g=2&h=1&k=&p=0&r=0&t=0"),
        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ਸਭਨਾ ਜੀਆ ਕਾ ਇਕੁ ਦਾਤਾ ਸੋ ਮੈ ਵਿਸਰਿ ਨ ਜਾਈ ॥",
            "There is one giver for all beings; may I never forget that source of life.",
            "সব জীবের দাতা এক; সেই পরম দাতাকে যেন কখনো ভুলে না যাই।",
            "Guru Granth Sahib, Ang 2 • Japji Sahib",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=2&g=2&h=1&k=&p=0&r=0&t=0"),
        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ਹੁਕਮਿ ਰਜਾਈ ਚਲਣਾ ਨਾਨਕ ਲਿਖਿਆ ਨਾਲਿ ॥",
            "Walk in harmony with the Divine Order; this is written into the way of life.",
            "পরম বিধানের সঙ্গে সামঞ্জস্য রেখে চলাই জীবনের পথ—এই বোধ অন্তরে ধারণ করো।",
            "Guru Granth Sahib, Ang 1 • Japji Sahib",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=1&g=2&h=1&k=&p=0&r=0&t=0"),
        WisdomItem("soul_space_source_sikh", "Guru Granth Sahib",
            "ਮਿਠਤੁ ਨੀਵੀ ਨਾਨਕਾ ਗੁਣ ਚੰਗਿਆਈਆ ਤਤੁ ॥",
            "Humility sweetens a person; it is the essence of goodness.",
            "বিনয় মানুষকে মধুর করে; এটি মহৎ গুণের সার।",
            "Guru Granth Sahib, Ang 470",
            "https://www.srigranth.org/servlet/gurbani.gurbani?Action=Page&Param=470&g=2&h=1&k=&p=0&r=0&t=0"),

        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "بشنو این نی چون شکایت می‌کند\nاز جدایی‌ها حکایت می‌کند",
            "Listen to this reed: it tells its tale of separation, voicing its longing.",
            "এই নলখাগড়ার বাঁশির কথা শোনো—সে বিচ্ছেদের কাহিনি বলে, বিচ্ছেদের বেদনাই জানায়।",
            "Mathnawi, Book I • opening couplet", "https://www.masnavi.net/1/50/eng/1/"),
        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "هر کسی کو دور ماند از اصل خویش\nباز جوید روزگار وصل خویش",
            "Whoever is separated from their origin longs for the time of reunion.",
            "যে নিজের মূল থেকে বিচ্ছিন্ন, সে আবার মিলনের সময় ফিরে পেতে আকুল হয়।",
            "Mathnawi, Book I • opening section", "https://www.masnavi.net/1/50/eng/1/"),
        WisdomItem("soul_space_source_sufi", "Jalal al-Din Rumi • Masnavi",
            "آتش عشق است کاندر نی فتاد\nجوشش عشق است کاندر می فتاد",
            "It is the fire of love that has entered the reed; it is love that gives the wine its fervour.",
            "প্রেমের আগুনই নলখাগড়ার বাঁশিতে প্রবেশ করেছে; প্রেমই মদের উচ্ছ্বাস জাগায়।",
            "Mathnawi, Book I • opening section", "https://www.masnavi.net/1/50/eng/1/"),

        WisdomItem("soul_space_source_philosophy", "Friedrich Nietzsche • Beyond Good and Evil",
            "Wer mit Ungeheuern kämpft, mag zusehn, dass er nicht dabei zum Ungeheuer wird. Und wenn du lange in einen Abgrund blickst, blickt der Abgrund auch in dich hinein.",
            "Whoever fights monsters should take care not to become a monster; when you gaze long into an abyss, the abyss also gazes into you. (Soul Space rendering from the German original.)",
            "যে দানবের সঙ্গে লড়ে, তার খেয়াল রাখা উচিত যেন সে নিজেই দানব হয়ে না যায়; আর দীর্ঘক্ষণ অতল গহ্বরের দিকে তাকালে, সেই গহ্বরও তোমার দিকে তাকায়। (জার্মান মূল থেকে Soul Space-এর অনুবাদ।)",
            "Beyond Good and Evil, §146 • German original; translation by Soul Space",
            "https://www.gutenberg.org/ebooks/4363"),
        WisdomItem("soul_space_source_philosophy", "Friedrich Nietzsche • Twilight of the Idols",
            "Aus der Kriegsschule des Lebens. — Was mich nicht umbringt, macht mich stärker.",
            "From the school of life: what does not kill me makes me stronger. (Soul Space rendering from the German original.)",
            "জীবনের সংগ্রামশালা থেকে: যা আমাকে মেরে ফেলে না, তা আমাকে আরও শক্তিশালী করে। (জার্মান মূল থেকে Soul Space-এর অনুবাদ।)",
            "Twilight of the Idols, “Maxims and Arrows,” §8 • German original; translation by Soul Space",
            "https://www.gutenberg.org/ebooks/52263"),
    ) + supplementalWisdom.flatten() + loadSufiSelections() + loadScriptureSelections()

    /**
     * Loads source-verified Rumi selections packaged with the launcher.
     * Until an on-device translation engine is available, missing Bengali
     * translations are labelled clearly and the English source is preserved.
     */
    private fun loadSufiSelections(): List<WisdomItem> = runCatching {
        resources.openRawResource(R.raw.sufi_wisdom).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.mapNotNull { line ->
                runCatching {
                    val item = JSONObject(line)
                    WisdomItem(
                        item.getString("sourceKey"),
                        item.getString("sourceLabel"),
                        item.optString("original").takeIf { it.isNotBlank() && it != "null" },
                        item.getString("english"),
                        item.optString("bengali"),
                        item.getString("reference"),
                        item.optString("sourceUrl").takeIf { it.isNotBlank() && it != "null" },
                    )
                }.getOrNull()
            }.toList()
        }
    }.getOrDefault(emptyList())

    /**
     * Loads the normalized offline scripture, philosophy and reflection datasets bundled
     * from datasets/soul-space. Source preferences remain compatible with existing settings.
     */
    private fun loadScriptureSelections(): List<WisdomItem> = runCatching {
        resources.openRawResource(R.raw.soul_space_scriptures).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.mapNotNull { line ->
                runCatching {
                    val item = JSONObject(line)
                    WisdomItem(
                        item.getString("sourceKey"),
                        item.optString("sourceLabel", "Soul Space source"),
                        item.optString("original").takeIf { it.isNotBlank() && it != "null" },
                        item.optString("english"),
                        item.optString("bengali"),
                        item.optString("reference", "Reference not provided"),
                        item.optString("sourceUrl").takeIf { it.isNotBlank() && it != "null" },
                    )
                }.getOrNull()
            }.toList()
        }
    }.getOrDefault(emptyList())

    private var currentIndex = 0
    private var receiverRegistered = false
    private val quoteView = TextView(context)
    private val sourceView = TextView(context)
    private val cardContainer = LinearLayout(context)
    private val actionRow = LinearLayout(context)
    private val favoriteButton = TextView(context)
    private val aiButton = TextView(context)
    private val cardBackground = GradientDrawable()

    private val unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Intent.ACTION_USER_PRESENT) return
            val manager = PreferenceManager2.getInstance(this@SoulSpaceView.context)
            val prefs = manager.getCachedPreferences()
            val schedule = prefs[stringPreferencesKey("soul_space_schedule")] ?: "each_unlock"
            val shouldRotate = when (schedule) {
                "each_unlock" -> true
                "daily" -> {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val lastRotation = prefs[stringPreferencesKey("soul_space_last_daily_rotation")]
                    if (lastRotation != today) {
                        runBlocking {
                            manager.preferencesDataStore.edit {
                                it[stringPreferencesKey("soul_space_last_daily_rotation")] = today
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
                else -> false // Manual mode changes only when the user taps the card.
            }
            if (shouldRotate) {
                val items = selectedItems()
                if (items.isNotEmpty()) {
                    currentIndex = Math.floorMod(
                        prefs[intPreferencesKey("soul_space_rotation_index")] ?: currentIndex,
                        items.size,
                    )
                    currentIndex = nextRandomIndex(items)
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
        actionRow.orientation = HORIZONTAL
        actionRow.gravity = Gravity.END
        actionRow.setPadding(0, (8 * resources.displayMetrics.density).toInt(), 0, 0)
        listOf(favoriteButton, aiButton).forEach { button ->
            button.setTextColor(Color.WHITE)
            button.textSize = 12f
            button.setPadding((10 * resources.displayMetrics.density).toInt(), (6 * resources.displayMetrics.density).toInt(), (10 * resources.displayMetrics.density).toInt(), (6 * resources.displayMetrics.density).toInt())
            button.isClickable = true
            button.isFocusable = true
            actionRow.addView(button, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        }
        favoriteButton.setOnClickListener { toggleFavorite() }
        aiButton.text = "✧ AI ব্যাখ্যা"
        aiButton.contentDescription = "বর্তমান উদ্ধৃতির বাংলা ব্যাখ্যা তৈরি করুন"
        aiButton.setOnClickListener { explainCurrentWisdomWithAi() }
        cardContainer.addView(actionRow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
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
        selected.addAll(sourcedWisdom.filter { item ->
            if (item.sourceKey == "soul_space_source_general") {
                prefs[booleanPreferencesKey("soul_space_source_general")] ?: true
            } else {
                prefs[booleanPreferencesKey(item.sourceKey)] ?: true
            }
        })
        // The bundled datasets overlap older hand-curated cards. Avoid repeated verse references
        // and identical renderings without altering the source files or their stable IDs.
        return selected.distinctBy { item ->
            val normalizedReference = item.reference.lowercase()
                .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            if (item.sourceKey == "soul_space_source_general") {
                "${item.sourceKey}:${item.english.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").replace(Regex("\\s+"), " ").trim()}"
            } else {
                "${item.sourceKey}:$normalizedReference"
            }
        }.distinctBy { item ->
            "${item.sourceKey}:${item.english.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").replace(Regex("\\s+"), " ").trim()}"
        }
    }

    private fun currentItem(): WisdomItem? {
        val items = selectedItems()
        if (items.isEmpty()) return null
        currentIndex = Math.floorMod(currentIndex, items.size)
        return items[currentIndex]
    }

    /**
     * Pick a random item, preferring a different source pack from the currently displayed item.
     * If only one source pack is enabled, still randomize within that pack.
     */
    private fun nextRandomIndex(items: List<WisdomItem>): Int {
        if (items.size <= 1) return 0
        val previousSource = items.getOrNull(Math.floorMod(currentIndex, items.size))?.sourceKey
        val otherSources = items.indices.filter { items[it].sourceKey != previousSource }
        val candidates = if (otherSources.isNotEmpty()) {
            otherSources
        } else {
            items.indices.filter { it != Math.floorMod(currentIndex, items.size) }
        }
        return candidates.random()
    }

    private fun showNextReflection() {
        val items = selectedItems()
        if (items.isNotEmpty()) {
            currentIndex = nextRandomIndex(items)
            PreferenceManager2.getInstance(context).soulSpaceRotationIndex.setBlocking(currentIndex)
        }
        updateReflection()
    }

    /**
     * Runs a short, source-aware explanation with the downloaded local model.
     * This is optional and never sends the quote or prompt to a network service.
     */
    private fun explainCurrentWisdomWithAi() {
        val item = currentItem() ?: return
        val manager = PreferenceManager2.getInstance(context)
        val prefs = manager.getCachedPreferences()
        if (prefs[booleanPreferencesKey("soul_space_ai_enabled")] != true) {
            android.app.AlertDialog.Builder(context)
                .setTitle("Soul Space AI বন্ধ আছে")
                .setMessage("Settings → Soul Space AI থেকে on-device AI চালু করে মডেল ডাউনলোড করুন।")
                .setPositiveButton("ঠিক আছে", null)
                .show()
            return
        }

        val modelFiles = listOf(
            "qwen3_0_6b_q4_0" to "qwen3_0_6b_mixed_int4.litertlm",
            "qwen3_1_7b_q4" to "Qwen3-1.7B_dynamic_wi4b32_afp32.litertlm",
            "qwen3_4b_q4" to "qwen3_4b_mixed_int4.litertlm",
            "gemma_mobile" to "gemma-4-E2B-it.litertlm",
        )
        val selectedModel = prefs[stringPreferencesKey("soul_space_ai_model")] ?: "auto"
        val modelDirectory = File(context.filesDir, "soul-space/models")
        val modelFile = if (selectedModel != "auto") {
            modelFiles.firstOrNull { it.first == selectedModel }
                ?.second?.let { File(modelDirectory, it) }
                ?.takeIf { it.isFile && it.length() > 0L }
        } else {
            modelFiles.firstNotNullOfOrNull { (_, name) ->
                File(modelDirectory, name).takeIf { it.isFile && it.length() > 0L }
            }
        }
        if (modelFile == null) {
            android.app.AlertDialog.Builder(context)
                .setTitle("AI মডেল পাওয়া যায়নি")
                .setMessage("Soul Space AI settings-এ গিয়ে নির্বাচিত মডেলটি ডাউনলোড করুন।")
                .setPositiveButton("ঠিক আছে", null)
                .show()
            return
        }

        val prompt = """
            এই উদ্ধৃতির সহজ, সংক্ষিপ্ত বাংলা ব্যাখ্যা দাও (২–৩টি বাক্য)।
            ব্যাখ্যাটি যেন উদ্ধৃতির অর্থের সঙ্গে সম্পর্কিত হয়। উৎস বা ধর্মগ্রন্থের বক্তব্য
            হিসেবে নতুন কোনো দাবি বানিয়ে লিখবে না। এটি ব্যাখ্যা, মূল উদ্ধৃতি বা অনুমোদিত
            অনুবাদ নয়।
            উৎস: ${item.sourceLabel}
            রেফারেন্স: ${item.reference}
            উদ্ধৃতি/অনুবাদ: ${item.bengali.ifBlank { item.english }}
        """.trimIndent()
        val dialog = android.app.AlertDialog.Builder(context)
            .setTitle("Soul Space · AI ব্যাখ্যা")
            .setMessage("ফোনেই AI মডেল চালু হচ্ছে… প্রথমবার কিছুটা সময় লাগতে পারে।")
            .setPositiveButton("বন্ধ", null)
            .create()
        dialog.setCancelable(false)
        dialog.show()
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).isEnabled = false

        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            try {
                val explanation = withTimeout(120_000) {
                    withContext(Dispatchers.IO) {
                        Engine.setNativeMinLogSeverity(LogSeverity.ERROR)
                        Engine(
                            EngineConfig(
                                modelPath = modelFile.absolutePath,
                                backend = Backend.CPU(),
                                cacheDir = context.cacheDir.absolutePath,
                            ),
                        ).use { engine ->
                            engine.initialize()
                            engine.createConversation().use { conversation ->
                                val answer = StringBuilder()
                                conversation.sendMessageAsync(prompt).collect { message ->
                                    answer.append(message.toString())
                                }
                                answer.toString().trim()
                            }
                        }
                    }
                }
                dialog.setTitle("Soul Space · AI ব্যাখ্যা")
                dialog.setMessage(
                    if (explanation.isNotBlank()) explanation
                    else "AI কোনো পাঠযোগ্য ব্যাখ্যা দেয়নি। মডেল বা runtime পরীক্ষা করুন.",
                )
            } catch (error: Exception) {
                dialog.setTitle("AI ব্যাখ্যা তৈরি হয়নি")
                dialog.setMessage(
                    "এই ফোনে স্থানীয় AI চালাতে সমস্যা হয়েছে.\n\n" +
                        (error.message ?: error.javaClass.simpleName),
                )
            } catch (error: LinkageError) {
                dialog.setTitle("AI runtime সমস্যা")
                dialog.setMessage(error.message ?: "Native AI runtime লোড করা যায়নি।")
            } finally {
                dialog.setCancelable(true)
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).isEnabled = true
            }
        }
    }

    private fun savedEntries(key: String): MutableList<String> {
        val raw = PreferenceManager2.getInstance(context).getCachedPreferences()[stringPreferencesKey(key)] ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            MutableList(array.length()) { index -> array.getString(index) }
        }.getOrDefault(mutableListOf())
    }

    private fun saveEntries(key: String, entries: List<String>) {
        val encoded = JSONArray().apply { entries.forEach { put(it) } }.toString()
        runBlocking {
            PreferenceManager2.getInstance(context).preferencesDataStore.edit { it[stringPreferencesKey(key)] = encoded }
        }
    }

    private fun itemSnapshot(item: WisdomItem): String =
        "${item.sourceLabel} • ${item.reference}\n${item.bengali.ifBlank { item.english }}\n${item.english}"

    private fun toggleFavorite() {
        val item = currentItem() ?: return
        val entries = savedEntries("soul_space_favorites")
        val snapshot = itemSnapshot(item)
        if (snapshot in entries) entries.remove(snapshot) else entries.add(0, snapshot)
        saveEntries("soul_space_favorites", entries.take(200))
        updateActionLabels()
    }

    private fun updateActionLabels() {
        val item = currentItem()
        val saved = item != null && itemSnapshot(item) in savedEntries("soul_space_favorites")
        favoriteButton.text = if (saved) "★ Saved" else "☆ Favorite"
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
            val bengaliText = item.bengali.ifBlank {
                "বাংলা অনুবাদ এখনও প্রস্তুত নয়; আপাতত ইংরেজি মূল পাঠ দেখানো হচ্ছে."
            }
            quoteView.text = when (language) {
                "bengali" -> if (item.bengali.isBlank()) bengaliText + "\n\n" + item.english else item.bengali
                "english" -> item.english
                "original_bengali" -> listOfNotNull(item.original, item.bengali.takeIf { it.isNotBlank() })
                    .ifEmpty { listOf(bengaliText + "\n\n" + item.english) }
                    .joinToString("\n")
                else -> bengaliText + "\n" + item.english
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
        if (item != null) {
            val history = savedEntries("soul_space_history")
            val snapshot = itemSnapshot(item)
            if (history.firstOrNull() != snapshot) saveEntries("soul_space_history", (listOf(snapshot) + history).take(100))
        }
        updateActionLabels()
        applyQuoteColor()
        // Re-measure after text/language changes so the card follows the full content.
        quoteView.requestLayout()
        sourceView.requestLayout()
        cardContainer.requestLayout()
        requestLayout()
    }
}
