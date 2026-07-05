package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Maya",
    val skinTone: String = "Medium Olive",
    val bodyShape: String = "Hourglass",
    val stylePreference: String = "Minimal Luxury",
    val clothingSize: String = "M",
    val ageGroup: String = "25-34",
    val gender: String = "Androgynous / Fluid",
    val favoriteColors: String = "Sage, Slate, Cream, Charcoal",
    val location: String = "London",
    val bioAuthSetup: Boolean = false,
    val bioAuthPhotoBase64: String = "",
    val customPaletteName: String = "",
    val customPaletteColors: String = "",
    val customPaletteJustification: String = "",
    val preferredSilhouettes: String = "Relaxed, Fluid",
    val skinConcerns: String = "Dehydration, Dullness",
    val hasCompletedQuiz: Boolean = false
)

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "Fashion" or "Skincare" or "Magazine"
    val readTime: String,
    val summary: String,
    val content: String,
    val imageUrl: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_products")
data class SavedProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val mainPrice: Double,
    val pricesJson: String, // Platform price comparisons (e.g., "[{\"platform\":\"Farfetch\",\"price\":120.0},{\"platform\":\"Net-A-Porter\",\"price\":135.0}]")
    val rating: Double,
    val reviewsCount: Int,
    val imageUrl: String = "",
    val description: String = "",
    val collection: String = "",
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // "user" or "assistant" or "system"
    val text: String,
    val modelUsed: String = "gemini-3.5-flash",
    val timestamp: Long = System.currentTimeMillis(),
    val isAudio: Boolean = false,
    val isPhoto: Boolean = false,
    val attachmentPath: String = ""
)

// --- DAOs ---

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Dao
interface SavedArticleDao {
    @Query("SELECT * FROM saved_articles ORDER BY savedAt DESC")
    fun getSavedArticles(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :id)")
    suspend fun isArticleSaved(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticle(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE id = :id")
    suspend fun deleteArticle(id: String)
}

@Dao
interface SavedProductDao {
    @Query("SELECT * FROM saved_products ORDER BY savedAt DESC")
    fun getSavedProducts(): Flow<List<SavedProductEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_products WHERE id = :id)")
    suspend fun isProductSaved(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProduct(product: SavedProductEntity)

    @Query("DELETE FROM saved_products WHERE id = :id")
    suspend fun deleteProduct(id: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Entity(tableName = "digital_closet")
data class ClosetItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Outerwear", "Tops", "Bottoms", "Shoes", "Accessories"
    val color: String,
    val material: String = "",
    val notes: String = ""
)

@Dao
interface ClosetItemDao {
    @Query("SELECT * FROM digital_closet ORDER BY id DESC")
    fun getAllClosetItems(): Flow<List<ClosetItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClosetItemEntity)

    @Delete
    suspend fun deleteItem(item: ClosetItemEntity)
}

@Entity(tableName = "saved_outfits")
data class SavedOutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val itemIdsJson: String, // JSON array of closet item IDs
    val itemNames: String, // comma separated item names
    val compatibilityScore: Int,
    val stylingVerdict: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Dao
interface SavedOutfitDao {
    @Query("SELECT * FROM saved_outfits ORDER BY savedAt DESC")
    fun getAllSavedOutfits(): Flow<List<SavedOutfitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOutfit(outfit: SavedOutfitEntity)

    @Delete
    suspend fun deleteOutfit(outfit: SavedOutfitEntity)
}

@Entity(tableName = "skincare_logs")
data class SkincareLogEntity(
    @PrimaryKey val date: String, // format: YYYY-MM-DD
    val amCompleted: Boolean,
    val pmCompleted: Boolean,
    val completedStepsJson: String // e.g. "[\"Cleanse\", \"Serum\"]"
)

@Dao
interface SkincareLogDao {
    @Query("SELECT * FROM skincare_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<SkincareLogEntity>>

    @Query("SELECT * FROM skincare_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): SkincareLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: SkincareLogEntity)
}

// --- Database ---

@Database(
    entities = [
        UserProfileEntity::class,
        SavedArticleEntity::class,
        SavedProductEntity::class,
        ChatMessageEntity::class,
        ClosetItemEntity::class,
        SavedOutfitEntity::class,
        SkincareLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MareDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun savedArticleDao(): SavedArticleDao
    abstract fun savedProductDao(): SavedProductDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun closetItemDao(): ClosetItemDao
    abstract fun savedOutfitDao(): SavedOutfitDao
    abstract fun skincareLogDao(): SkincareLogDao

    companion object {
        @Volatile
        private var INSTANCE: MareDatabase? = null

        fun getDatabase(context: android.content.Context): MareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MareDatabase::class.java,
                    "mare_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
