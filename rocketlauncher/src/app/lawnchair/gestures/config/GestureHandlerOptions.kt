package app.lawnchair.gestures.config

import android.app.Activity
import android.content.Context

// Global system actions that require an AccessibilityService are intentionally omitted
// from this build so financial apps do not see an enabled launcher accessibility service.
val gestureHandlerOptions = listOf(
    GestureHandlerOption.NoOp,
    GestureHandlerOption.Sleep,
    GestureHandlerOption.OpenAppDrawer,
    GestureHandlerOption.OpenAppSearch,
    GestureHandlerOption.OpenSearch,
    GestureHandlerOption.OpenApp,
    GestureHandlerOption.OpenAssistant,
)

private val optionsDisabledInDeckLayout = setOf(
    GestureHandlerOption.OpenAppDrawer,
    GestureHandlerOption.OpenAppSearch,
)

fun filterGestureHandlerOptions(deckLayoutEnabled: Boolean): List<GestureHandlerOption> {
    if (!deckLayoutEnabled) return gestureHandlerOptions
    return gestureHandlerOptions.filterNot { it in optionsDisabledInDeckLayout }
}

suspend fun GestureHandlerOption.buildConfigFrom(context: Context): GestureHandlerConfig? {
    val activity = context as? Activity ?: return null
    return buildConfig(activity)
}
