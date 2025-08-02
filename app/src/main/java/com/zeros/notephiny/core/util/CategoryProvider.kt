package com.zeros.notephiny.core.util

import com.zeros.notephiny.data.model.CategoryItem
import com.zeros.notephiny.data.model.CategoryType
import com.zeros.notephiny.domain.repository.NoteRepository
import javax.inject.Inject

data class CategoryItem(
    val name: String,
    val count: Int = 0,
    val type: CategoryType
)

class CategoryProvider @Inject constructor(
    private val repository: NoteRepository
) {
    suspend fun getStructuredCategories(): List<CategoryItem> {
        val defaultCategories = repository.getDefaultCategories()
        val appCategories = repository.getAppCategories()
        val categoryCounts = repository.getNoteCountsByCategory().associateBy { it.category }

        val userCategories = categoryCounts.keys
            .filter { it !in appCategories }
            .sorted()

        return buildList {
            // User-defined (includes default categories) treated as USER
            addAll(userCategories.map { name ->
                val type = if (name in defaultCategories) CategoryType.USER else CategoryType.USER
                CategoryItem(name, categoryCounts[name]?.count ?: 0, type)
            })

            // App categories (e.g., Deleted, Locked)
            addAll(appCategories.map { name ->
                CategoryItem(name, categoryCounts[name]?.count ?: 0, CategoryType.APP)
            })
        }
    }

}


