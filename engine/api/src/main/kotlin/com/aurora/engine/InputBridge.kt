package com.aurora.engine

interface InputBridge {
    fun injectHoverMove(x: Float, y: Float)
    fun injectClick(x: Float, y: Float)
    fun injectScroll(deltaX: Float, deltaY: Float)
    var onPointerIconChange: ((PointerIconType) -> Unit)?
}
