package com.aurora.ui.focus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FocusDirection { UP, DOWN, LEFT, RIGHT }

data class FocusableNode(
    val id: String,
    val group: String = "default",
    val order: Int = 0
)

class FocusEngine(
    var groupOrder: List<String> = listOf("home", "continue_browsing", "favorites", "downloads", "quick_actions")
) {

    private val _focusedId = MutableStateFlow<String?>(null)
    val focusedId: StateFlow<String?> = _focusedId.asStateFlow()

    private val nodes = mutableMapOf<String, FocusableNode>()
    private val selectActions = mutableMapOf<String, () -> Unit>()
    private val backActions = mutableListOf<() -> Unit>()
    private val groups = mutableMapOf<String, MutableList<String>>()
    private val lastFocusedInGroup = mutableMapOf<String, String>()

    fun register(node: FocusableNode, onSelect: () -> Unit = {}) {
        nodes[node.id] = node
        selectActions[node.id] = onSelect
        groups.getOrPut(node.group) { mutableListOf() }.add(node.id)
        groups[node.group]?.sortBy { nodes[it]?.order ?: 0 }
        if (_focusedId.value == null) {
            _focusedId.value = node.id
        }
        if (!lastFocusedInGroup.containsKey(node.group)) {
            lastFocusedInGroup[node.group] = node.id
        }
    }

    fun unregister(id: String) {
        val node = nodes[id]
        if (node != null) {
            if (lastFocusedInGroup[node.group] == id) {
                lastFocusedInGroup.remove(node.group)
            }
        }
        nodes.remove(id)
        selectActions.remove(id)
        groups.values.forEach { it.remove(id) }
        if (_focusedId.value == id) {
            val fallback = nodes.keys.firstOrNull()
            _focusedId.value = fallback
            if (fallback != null) {
                val fallbackNode = nodes[fallback]
                if (fallbackNode != null) {
                    lastFocusedInGroup[fallbackNode.group] = fallback
                }
            }
        }
    }

    fun requestFocus(id: String) {
        val node = nodes[id] ?: return
        _focusedId.value = id
        lastFocusedInGroup[node.group] = id
    }

    fun moveFocus(direction: FocusDirection): Boolean {
        val currentId = _focusedId.value ?: return false
        val current = nodes[currentId] ?: return false
        val currentGroup = current.group

        if (direction == FocusDirection.LEFT || direction == FocusDirection.RIGHT) {
            val groupList = groups[currentGroup] ?: return false
            val currentIndex = groupList.indexOf(currentId)
            if (currentIndex < 0) return false
            val nextIndex = when (direction) {
                FocusDirection.LEFT -> (currentIndex - 1).coerceAtLeast(0)
                FocusDirection.RIGHT -> (currentIndex + 1).coerceAtMost(groupList.lastIndex)
                else -> currentIndex
            }
            if (nextIndex != currentIndex) {
                val nextId = groupList[nextIndex]
                _focusedId.value = nextId
                lastFocusedInGroup[currentGroup] = nextId
                return true
            }
            return false
        } else {
            val activeGroups = groupOrder.filter { groupName ->
                groups[groupName]?.isNotEmpty() == true
            }
            if (activeGroups.isEmpty()) return false
            val currentGroupIndex = activeGroups.indexOf(currentGroup)
            if (currentGroupIndex < 0) return false

            val targetGroupIndex = when (direction) {
                FocusDirection.DOWN -> {
                    if (currentGroupIndex == activeGroups.lastIndex) 0 else currentGroupIndex + 1
                }
                FocusDirection.UP -> {
                    if (currentGroupIndex == 0) currentGroupIndex else currentGroupIndex - 1
                }
                else -> currentGroupIndex
            }

            if (targetGroupIndex != currentGroupIndex) {
                val targetGroup = activeGroups[targetGroupIndex]
                val targetGroupList = groups[targetGroup] ?: return false
                if (targetGroupList.isEmpty()) return false

                val rememberedId = lastFocusedInGroup[targetGroup]
                val nextId = if (rememberedId != null && targetGroupList.contains(rememberedId)) {
                    rememberedId
                } else {
                    targetGroupList[0]
                }
                _focusedId.value = nextId
                lastFocusedInGroup[targetGroup] = nextId
                return true
            }
            return false
        }
    }


    fun selectFocused(): Boolean {
        val id = _focusedId.value ?: return false
        selectActions[id]?.invoke()
        return true
    }

    fun pushBackAction(action: () -> Unit) {
        backActions.add(action)
    }

    fun popBackAction() {
        if (backActions.isNotEmpty()) {
            val action = backActions.removeAt(backActions.lastIndex)
            action()
        }
    }

    fun clear() {
        nodes.clear()
        selectActions.clear()
        groups.clear()
        _focusedId.value = null
    }
}