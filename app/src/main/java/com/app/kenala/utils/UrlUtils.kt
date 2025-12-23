package com.app.kenala.utils

import com.app.kenala.api.RetrofitClient

object UrlUtils {
    fun getFullImageUrl(path: String?): String? {
        if (path.isNullOrEmpty()) return null

        if (path.startsWith("http")) return path

        val normalizedPath = path.replace("\\", "/")

        val cleanPath = if (normalizedPath.startsWith("/")) {
            normalizedPath.substring(1)
        } else {
            normalizedPath
        }

        val baseUrl = RetrofitClient.IMAGE_BASE_URL

        val fixedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return "$fixedBaseUrl$cleanPath"
    }
}