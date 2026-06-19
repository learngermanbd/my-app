package com.streamapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_categories")
data class CachedCategory(
    @PrimaryKey val id: Int,
    val name: String,
    val slug: String,
    @ColumnInfo(name = "icon_url") val iconUrl: String?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "channel_count") val channelCount: Int,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cached_channels",
    foreignKeys = [ForeignKey(
        entity = CachedCategory::class,
        parentColumns = ["id"],
        childColumns = ["category_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class CachedChannel(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    val name: String,
    val logo: String?,
    @ColumnInfo(name = "is_live") val isLive: Int,
    val featured: Int,
    @ColumnInfo(name = "category_name") val categoryName: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface StreamDao {

    @Query("SELECT * FROM cached_categories ORDER BY sort_order")
    fun getCategories(): Flow<List<CachedCategory>>

    @Query("SELECT * FROM cached_channels WHERE category_id = :categoryId ORDER BY featured DESC, name")
    fun getChannels(categoryId: Int): Flow<List<CachedChannel>>

    @Query("SELECT c.*, cat.name AS category_name FROM cached_channels c " +
           "LEFT JOIN cached_categories cat ON c.category_id = cat.id " +
           "ORDER BY c.featured DESC, c.is_live DESC, c.name")
    fun getAllChannels(): Flow<List<CachedChannel>>

    @Query("SELECT c.*, cat.name AS category_name FROM cached_channels c " +
           "LEFT JOIN cached_categories cat ON c.category_id = cat.id " +
           "WHERE c.featured = 1 ORDER BY c.name")
    fun getFeaturedChannels(): Flow<List<CachedChannel>>

    @Query("SELECT c.*, cat.name AS category_name FROM cached_channels c " +
           "LEFT JOIN cached_categories cat ON c.category_id = cat.id " +
           "WHERE c.is_live = 1 ORDER BY c.name")
    fun getLiveChannels(): Flow<List<CachedChannel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CachedCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<CachedChannel>)

    @Query("DELETE FROM cached_categories")
    suspend fun clearCategories()

    @Query("DELETE FROM cached_channels")
    suspend fun clearChannels()

    @Query("SELECT * FROM cached_channels WHERE name LIKE '%' || :query || '%' ORDER BY featured DESC")
    fun searchChannels(query: String): Flow<List<CachedChannel>>

    @Query("SELECT COUNT(*) FROM cached_categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT COUNT(*) FROM cached_channels")
    suspend fun getChannelCount(): Int
}
