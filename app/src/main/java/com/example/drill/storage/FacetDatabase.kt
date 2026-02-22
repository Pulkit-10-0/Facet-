package com.example.drill.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FacetEntity::class], version = 1, exportSchema = false)
@TypeConverters(FacetTypeConverters::class)
abstract class FacetDatabase : RoomDatabase() {
    abstract fun facetDao(): FacetDao

    companion object {
        private const val DB_NAME = "facet.db"
        @Volatile private var instance: FacetDatabase? = null

        fun get(context: Context): FacetDatabase {
            return instance
                    ?: synchronized(this) {
                        instance
                                ?: Room.databaseBuilder(
                                                context.applicationContext,
                                                FacetDatabase::class.java,
                                                DB_NAME
                                        )
                                        .build()
                                        .also { instance = it }
                    }
        }
    }
}
