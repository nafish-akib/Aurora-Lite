package com.aurora.ui

object KeyBridge {
    var onDpad: ((String) -> Boolean)? = null
    var onSelect: (() -> Boolean)? = null
    var onBack: (() -> Unit)? = null
    var onVoice: (() -> Unit)? = null
    var isKeyboardOpen: Boolean = false
}