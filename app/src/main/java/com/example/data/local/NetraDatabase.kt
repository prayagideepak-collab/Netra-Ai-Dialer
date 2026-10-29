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

@Entity(tableName = "local_spam_rules")
data class LocalSpamRuleEntity(
    @PrimaryKey val phoneNumber: String,
    val label: String,
    val category: String, // SPAM, TELEMARKETER, FRAUD, ALLOW_LIST, BLOCK_LIST
    val isBlocked: Boolean,
    val isWhitelisted: Boolean,
    val reportedByUser: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "local_call_recordings")
data class LocalRecordingEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val contactName: String,
    val timestampMillis: Long,
    val durationSeconds: Long,
    val encryptionAlgorithm: String = "AES-256-GCM (Android Keystore)",
    val status: String = "ENCRYPTED_LOCAL", // ENCRYPTED_LOCAL, UNSUPPORTED_OS_RESTRICTION, OPTED_IN_DRIVE
    val retentionDays: Int = 30,
    val transcriptNotes: String = ""
)

@Entity(tableName = "local_editable_profile")
data class LocalEditableProfileEntity(
    @PrimaryKey val id: String = "primary_profile",
    val internalDisplayNotes: String = "Prayagi Store & Services — Main Operations Desk",
    val businessHours: String = "Mon–Sat, 09:00 AM – 08:00 PM IST",
    val faqSummary: String = "Authorized supplier of commercial, technical & bioscience consumables, verified home/office service dispatch.",
    val aiGreetingStyle: String = "Professional Bilingual (Hindi + English)",
    val autoRecordPreference: String = "ALWAYS_WHEN_PERMITTED",
    val ttsEnabled: Boolean = true,
    val ttsQuietHoursEnabled: Boolean = true,
    val driveWifiOnly: Boolean = true,
    val driveIncludeRecordings: Boolean = false,
    val lastDriveBackupMillis: Long = 0L
)

@Dao
interface NetraLocalDao {
    @Query("SELECT * FROM local_spam_rules ORDER BY updatedAtMillis DESC")
    fun observeSpamRules(): Flow<List<LocalSpamRuleEntity>>

    @Query("SELECT * FROM local_spam_rules WHERE phoneNumber = :number LIMIT 1")
    suspend fun findSpamRule(number: String): LocalSpamRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpamRule(rule: LocalSpamRuleEntity)

    @Query("DELETE FROM local_spam_rules WHERE phoneNumber = :number")
    suspend fun deleteSpamRule(number: String)

    @Query("SELECT * FROM local_call_recordings ORDER BY timestampMillis DESC")
    fun observeRecordings(): Flow<List<LocalRecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecording(recording: LocalRecordingEntity)

    @Query("DELETE FROM local_call_recordings WHERE id = :id")
    suspend fun deleteRecording(id: String)

    @Query("SELECT * FROM local_editable_profile WHERE id = 'primary_profile' LIMIT 1")
    fun observeEditableProfile(): Flow<LocalEditableProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEditableProfile(profile: LocalEditableProfileEntity)
}

@Database(
    entities = [
        LocalSpamRuleEntity::class,
        LocalRecordingEntity::class,
        LocalEditableProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NetraLocalDatabase : RoomDatabase() {
    abstract fun dao(): NetraLocalDao

    companion object {
        @Volatile
        private var INSTANCE: NetraLocalDatabase? = null

        fun getInstance(context: Context): NetraLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NetraLocalDatabase::class.java,
                    "netra_dialer_local.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
