package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CachedPrescriptionDao
import com.example.data.local.dao.FamilyMemberDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.CachedPrescriptionEntity
import com.example.data.local.entity.LocalFamilyMemberEntity
import com.example.data.local.entity.LocalReminderEntity

@Database(
    entities = [
        LocalReminderEntity::class,
        LocalFamilyMemberEntity::class,
        CachedPrescriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MediBridgeDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun cachedPrescriptionDao(): CachedPrescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: MediBridgeDatabase? = null

        fun getDatabase(context: Context): MediBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediBridgeDatabase::class.java,
                    "medibridge_offline_store.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
