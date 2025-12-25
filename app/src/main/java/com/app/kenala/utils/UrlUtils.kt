package com.app.kenala.utils

import com.app.kenala.api.RetrofitClient
import java.net.URI

object UrlUtils {
    fun getFullImageUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null

        val normalizedPath = path.replace("\\", "/")

        val apiBaseUrl = RetrofitClient.BASE_URL
        val serverRootUrl = if (apiBaseUrl.endsWith("api/")) {
            apiBaseUrl.removeSuffix("api/")
        } else {
            apiBaseUrl
        }

        if (normalizedPath.startsWith("http")) {
            return try {
                val savedUri = URI(normalizedPath)
                val currentBaseUri = URI(serverRootUrl)

                if ((savedUri.host == "localhost" || savedUri.host == "10.0.2.2") &&
                    currentBaseUri.host != savedUri.host) {

                    normalizedPath
                        .replace("http://${savedUri.host}:${savedUri.port}", "${currentBaseUri.scheme}://${currentBaseUri.host}:${currentBaseUri.port}")
                        .replace("http://${savedUri.host}", "${currentBaseUri.scheme}://${currentBaseUri.host}:${currentBaseUri.port}")
                } else {
                    normalizedPath
                }
            } catch (e: Exception) {
                normalizedPath
            }
        }

        val cleanPath = if (normalizedPath.startsWith("/")) {
            normalizedPath.substring(1)
        } else {
            normalizedPath
        }

        return "$serverRootUrl$cleanPath"
    }
}