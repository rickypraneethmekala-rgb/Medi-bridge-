package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CachedPrescriptionDao
import com.example.data.local.dao.FamilyMemberDao
import com.example.data.local.dao.MedicalDocumentDao
import com.example.data.local.dao.MedicineReminderDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.CachedPrescriptionEntity
import com.example.data.local.entity.LocalFamilyMemberEntity
import com.example.data.local.entity.LocalReminderEntity
import com.example.data.local.entity.MedicalDocument
import com.example.data.local.entity.MedicineReminder

@Database(
    entities = [
        LocalReminderEntity::class,
        LocalFamilyMemberEntity::class,
        CachedPrescriptionEntity::class,
        MedicalDocument::class,
        MedicineReminder::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MediBridgeDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun cachedPrescriptionDao(): CachedPrescriptionDao
    abstract fun medicalDocumentDao(): MedicalDocumentDao
    abstract fun medicineReminderDao(): MedicineReminderDao

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
