package com.rtiqa.core.domain.model

/**
 * Domain pagination container.
 */
data class PagedData<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Int,
    val hasNextPage: Boolean
)

/**
 * Page request specification.
 */
data class PageRequest(
    val page: Int = 1,
    val pageSize: Int = 20,
    val searchQuery: String? = null,
    val filterCategory: String? = null
)
