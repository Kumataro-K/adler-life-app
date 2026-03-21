package com.example.adlerlife.data.local

import androidx.room.TypeConverter
import com.example.adlerlife.data.model.ActionCategory

class Converters {
    @TypeConverter
    fun toCategory(value: String): ActionCategory = ActionCategory.valueOf(value)

    @TypeConverter
    fun fromCategory(category: ActionCategory): String = category.name
}
