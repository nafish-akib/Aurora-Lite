package com.aurora.engine

/**
 * Engine-agnostic password vault. Implemented by GeckoLoginStorage
 * (Gecko Autofill.Delegate) and WebViewLoginStorage (JS form capture).
 */
interface LoginStorage {

    data class SavedLogin(
        val origin: String,
        val username: String,
        val password: String
    )

    /** When true, login forms are captured automatically. */
    var saveOnCapture: Boolean

    fun saveLogin(origin: String, username: String, password: String)
    fun findLogins(origin: String): List<SavedLogin>
    fun getAllLogins(): List<SavedLogin>
    fun deleteLogin(origin: String, username: String)
    fun clearAll()
}