package com.example.drill.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FacetDao {
    @Query("SELECT * FROM facets ORDER BY lastOpened DESC")
    suspend fun getAll(): List<FacetEntity>

    @Query("SELECT * FROM facets WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FacetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FacetEntity)

    @Query("DELETE FROM facets WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE facets SET lastOpened = :lastOpened WHERE id = :id")
    suspend fun updateLastOpened(id: String, lastOpened: Long)
}

