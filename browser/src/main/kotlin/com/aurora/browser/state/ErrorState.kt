package com.aurora.browser.state

sealed class ErrorState {
    data object None : ErrorState()
    data class HttpError(val code: Int, val description: String) : ErrorState()
    data class NetworkError(val description: String) : ErrorState()
    data class SslError(val description: String) : ErrorState()
    data class FileNotFound(val url: String) : ErrorState()
    data class Unknown(val code: Int, val description: String) : ErrorState()

    val isError: Boolean get() = this !is None

    companion object {
        fun fromEngineError(errorCode: Int, description: String, url: String): ErrorState {
            return when (errorCode) {
                -2  -> NetworkError("Server hostname could not be found")
                -6  -> NetworkError("Connection refused or failed")
                -7  -> NetworkError("Network I/O error")
                -8  -> NetworkError("Connection timed out")
                -9  -> NetworkError("Too many redirects")
                -11 -> SslError(description)
                -14 -> FileNotFound(url)
                -15 -> Unknown(errorCode, "Too many requests — the server is rate-limiting")
                -1, -3, -4, -5, -10, -12, -13 -> Unknown(errorCode, description)
                else -> {
                    if (errorCode > 0) HttpError(errorCode, description)
                    else Unknown(errorCode, description)
                }
            }
        }
    }
}
