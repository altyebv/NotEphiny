package com.zeros.notephiny.data.model




data class CategoryCount(
    val category: String,
    val count: Int
)

enum class CategoryType {
    USER,
    APP
}


data class CategoryItem(
    val name: String,
    val count: Int,
    val type: CategoryType
)

data class CategoryGroup(
    val label: String,
    val items: List<CategoryItem>
)


