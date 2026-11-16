package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_sos_events")
data class CachedSosEntity(
    @PrimaryKey val id: String,
    val status: String,
    val triggerType: String,
    val lat: Double,
    val lon: Double,
    val locationLabel: String,
    val message: String,
    val deliveryNote: String,
    val timestampMs: Long
)

@Entity(tableName = "cached_citizen_reports")
data class CachedReportEntity(
    @PrimaryKey val id: String,
    val category: String,
    val description: String,
    val lat: Double,
    val lon: Double,
    val nearestStation: String,
    val severity: Int,
    val status: String,
    val timestampMs: Long
)

@Entity(tableName = "quiz_badges")
data class QuizBadgeEntity(
    @PrimaryKey val badgeId: String,
    val earnedAtMs: Long
)

@Dao
interface CyberWomenDao {
    @Query("SELECT * FROM cached_sos_events ORDER BY timestampMs DESC")
    fun observeCachedSos(): Flow<List<CachedSosEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSos(item: CachedSosEntity)

    @Query("SELECT * FROM cached_citizen_reports ORDER BY timestampMs DESC")
    fun observeCachedReports(): Flow<List<CachedReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReport(item: CachedReportEntity)

    @Query("SELECT * FROM quiz_badges")
    fun observeBadges(): Flow<List<QuizBadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun earnBadge(badge: QuizBadgeEntity)
}

@Database(
    entities = [CachedSosEntity::class, CachedReportEntity::class, QuizBadgeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CyberWomenDatabase : RoomDatabase() {
    abstract fun dao(): CyberWomenDao

    companion object {
        @Volatile
        private var INSTANCE: CyberWomenDatabase? = null

        fun getInstance(context: Context): CyberWomenDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CyberWomenDatabase::class.java,
                    "cyber_women_local_cache.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { INSTANCE = it }
            }
        }
    }
}
