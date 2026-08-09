package com.aurora.ui.focus

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusEngineTest {

    @Test
    fun `first registered node gains focus`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("home_a", "home", 0))
        engine.register(FocusableNode("home_b", "home", 1))
        assertEquals("home_a", engine.focusedId.value)
    }

    @Test
    fun `right and left move within group`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("a", "home", 0))
        engine.register(FocusableNode("b", "home", 1))
        engine.register(FocusableNode("c", "home", 2))

        assertTrue(engine.moveFocus(FocusDirection.RIGHT))
        assertEquals("b", engine.focusedId.value)
        assertTrue(engine.moveFocus(FocusDirection.RIGHT))
        assertEquals("c", engine.focusedId.value)
        assertFalse(engine.moveFocus(FocusDirection.RIGHT))
        assertEquals("c", engine.focusedId.value)

        assertTrue(engine.moveFocus(FocusDirection.LEFT))
        assertEquals("b", engine.focusedId.value)
        assertTrue(engine.moveFocus(FocusDirection.LEFT))
        assertEquals("a", engine.focusedId.value)
        assertFalse(engine.moveFocus(FocusDirection.LEFT))
        assertEquals("a", engine.focusedId.value)
    }

    @Test
    fun `down moves to next group`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("h", "home", 0))
        engine.register(FocusableNode("f", "favorites", 0))

        assertTrue(engine.moveFocus(FocusDirection.DOWN))
        assertEquals("f", engine.focusedId.value)
    }

    @Test
    fun `down from last group wraps to first`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("h", "home", 0))
        engine.register(FocusableNode("f", "favorites", 0))
        engine.requestFocus("f")

        assertTrue(engine.moveFocus(FocusDirection.DOWN))
        assertEquals("h", engine.focusedId.value)
    }

    @Test
    fun `up from first group stays put`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("h", "home", 0))
        engine.register(FocusableNode("f", "favorites", 0))
        engine.requestFocus("h")

        assertFalse(engine.moveFocus(FocusDirection.UP))
        assertEquals("h", engine.focusedId.value)
    }

    @Test
    fun `down remembers last focused node in target group`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("h1", "home", 0))
        engine.register(FocusableNode("f1", "favorites", 0))
        engine.register(FocusableNode("f2", "favorites", 1))

        engine.requestFocus("f2")
        engine.requestFocus("h1")
        assertTrue(engine.moveFocus(FocusDirection.DOWN))
        assertEquals("f2", engine.focusedId.value)
    }

    @Test
    fun `requestFocus with unknown id is ignored`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("a", "home", 0))
        engine.requestFocus("nope")
        assertEquals("a", engine.focusedId.value)
    }

    @Test
    fun `select invokes action and reports true`() {
        val engine = FocusEngine()
        var selected = false
        engine.register(FocusableNode("a", "home", 0)) { selected = true }
        assertTrue(engine.selectFocused())
        assertTrue(selected)
    }

    @Test
    fun `select with no focus reports false`() {
        val engine = FocusEngine()
        assertFalse(engine.selectFocused())
    }

    @Test
    fun `unregister focused node moves focus to another node`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("a", "home", 0))
        engine.register(FocusableNode("b", "favorites", 0))
        engine.requestFocus("a")
        engine.unregister("a")
        assertEquals("b", engine.focusedId.value)
    }

    @Test
    fun `unregister last node clears focus`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("a", "home", 0))
        engine.unregister("a")
        assertNull(engine.focusedId.value)
    }

    @Test
    fun `back actions pop in lifo order`() {
        val engine = FocusEngine()
        val calls = mutableListOf<String>()
        engine.register(FocusableNode("a", "home", 0))
        engine.pushBackAction { calls.add("first") }
        engine.pushBackAction { calls.add("second") }
        engine.popBackAction()
        engine.popBackAction()
        assertEquals(listOf("second", "first"), calls)
    }

    @Test
    fun `empty groups are skipped in down navigation`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("h", "home", 0))
        engine.register(FocusableNode("f", "favorites", 0))
        assertTrue(engine.moveFocus(FocusDirection.DOWN))
        assertEquals("f", engine.focusedId.value)
        assertTrue(engine.moveFocus(FocusDirection.DOWN)) // wraps back to first group
        assertEquals("h", engine.focusedId.value)
    }

    @Test
    fun `clear resets everything`() {
        val engine = FocusEngine()
        engine.register(FocusableNode("a", "home", 0))
        engine.clear()
        assertNull(engine.focusedId.value)
        assertFalse(engine.moveFocus(FocusDirection.RIGHT))
    }
}