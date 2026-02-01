package com.playoutedge.contracts.pagination

import kotlinx.serialization.Serializable

enum class SortOrder {
    ASC, DESC
}

data class PageRequest(
    val pageSize: Int = 20,
    val pageToken: String? = null,
    val sortBy: String = "createdAt",
    val sortOrder: SortOrder = SortOrder.DESC
) {
    init {
        require(pageSize in 1..100) { "pageSize must be between 1 and 100" }
    }
}

@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val nextPageToken: String? = null,
    val totalCount: Long
)

@Serializable
data class ListResponse<T>(
    val items: List<T>,
    val total: Int
)
