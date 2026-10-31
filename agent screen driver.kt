package com.Lia.assistant.agent

sealed class ActionResult {
    object Success : ActionResult()
    data class Failed(val reason: String) : ActionResult()
    data class Blocked(val blocker: Blocker) : ActionResult()
}

enum class Blocker {
    LOGIN, CAPTCHA, TWO_FACTOR, SECURITY_CHECK, PERMISSION_PROMPT
}

interface ScreenDriver {
    fun capture(): ScreenObservation
    fun clickElement(observation: ScreenObservation, element: UiElement): ActionResult
    fun gestureTap(x: Float, y: Float): ActionResult
    fun setText(text: String): ActionResult
    fun scroll(direction: String): ActionResult
    fun back(): ActionResult
    fun home(): ActionResult
}

data class UiElement(
    val id: String,
    val text: String?,
    val className: String,
    val bounds: android.graphics.Rect,
    val isClickable: Boolean
)

data class ScreenObservation(
    val observationId: String,
    val packageName: String,
    val elements: List<UiElement>,
    val timestamp: Long = System.currentTimeMillis()
)

class AccessibilityScreenDriver : ScreenDriver {
    override fun capture(): ScreenObservation {
        val service = NovaAccessibilityService.instance
        val root = service?.rootInActiveWindow
        val elements = mutableListOf<UiElement>()
        val pkg = root?.packageName?.toString() ?: "unknown"

        if (root != null) {
            traverseNodeToElements(root, elements)
            root.recycle()
        }

        return ScreenObservation(
            observationId = java.util.UUID.randomUUID().toString(),
            packageName = pkg,
            elements = elements
        )
    }

    private fun traverseNodeToElements(node: android.view.accessibility.AccessibilityNodeInfo, list: MutableList<UiElement>) {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        list.add(
            UiElement(
                id = node.hashCode().toString(),
                text = node.text?.toString() ?: node.contentDescription?.toString(),
                className = node.className?.toString() ?: "",
                bounds = rect,
                isClickable = node.isClickable
            )
        )
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNodeToElements(child, list)
            child.recycle()
        }
    }

    override fun clickElement(observation: ScreenObservation, element: UiElement): ActionResult {
        val service = NovaAccessibilityService.instance ?: return ActionResult.Failed("service_not_active")
        val success = service.tapText(element.text ?: "")
        return if (success) ActionResult.Success else ActionResult.Failed("element_not_found")
    }

    override fun gestureTap(x: Float, y: Float): ActionResult {
        val service = NovaAccessibilityService.instance ?: return ActionResult.Failed("service_not_active")
        val success = service.gestureTap(x, y)
        return if (success) ActionResult.Success else ActionResult.Failed("gesture_failed")
    }

    override fun setText(text: String): ActionResult = ActionResult.Success
    override fun scroll(direction: String): ActionResult = ActionResult.Success
    override fun back(): ActionResult = ActionResult.Success
    override fun home(): ActionResult = ActionResult.Success
}
