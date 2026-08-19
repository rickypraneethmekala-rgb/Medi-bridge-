package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MediBridgeApp
import com.example.core.alarm.MedicineAlarmPlayer
import com.example.core.alarm.MedicineAlarmScheduler
import com.example.core.security.SecureStorage
import com.example.data.local.entity.LocalFamilyMemberEntity
import com.example.data.local.entity.LocalReminderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderFamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val secureStorage = SecureStorage(application)
    private val db = (application as MediBridgeApp).database

    val reminders = db.reminderDao().getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyMembers = db.familyMemberDao().getAllFamilyMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isAlarmRinging = MedicineAlarmPlayer.isAlarmRinging
    val activeAlarmMedicine = MedicineAlarmPlayer.activeAlarmMedicine

    private val _isSeniorMode = MutableStateFlow(secureStorage.isSeniorMode)
    val isSeniorMode: StateFlow<Boolean> = _isSeniorMode.asStateFlow()

    fun toggleSeniorMode(enabled: Boolean) {
        secureStorage.isSeniorMode = enabled
        _isSeniorMode.value = enabled
    }

    fun addReminder(name: String, dosage: String, timesJson: String, totalDays: Int) {
        viewModelScope.launch {
            val remId = "rem_${System.currentTimeMillis()}"
            val entity = LocalReminderEntity(
                id = remId,
                medicineName = name,
                dosage = dosage,
                reminderTimesJson = timesJson,
                totalDays = totalDays,
                daysRemaining = totalDays,
                isTakenToday = false
            )
            db.reminderDao().insertReminder(entity)

            // Connect and schedule with Android Local AlarmManager
            val context = getApplication<Application>()
            MedicineAlarmScheduler.scheduleAllTimes(
                context = context,
                reminderId = remId,
                medicineName = name,
                dosage = dosage,
                timesString = timesJson
            )
        }
    }

    fun markDoseTaken(reminder: LocalReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(
                isTakenToday = true,
                lastTakenTimestamp = System.currentTimeMillis()
            )
            db.reminderDao().updateReminder(updated)
            // Stop alarm if it was currently ringing for this medicine
            stopActiveAlarm()
        }
    }

    fun deleteReminder(reminder: LocalReminderEntity) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val parts = reminder.reminderTimesJson.split(",").map { it.trim() }
            for (timeStr in parts) {
                val (hour, minute) = MedicineAlarmScheduler.parseHourMinute(timeStr)
                MedicineAlarmScheduler.cancelAlarm(context, reminder.id, hour, minute)
            }
            db.reminderDao().deleteReminder(reminder)
        }
    }

    fun triggerTestAlarmNow(medicineName: String = "Paracetamol", dosage: String = "500mg (1 Tab)") {
        val context = getApplication<Application>()
        MedicineAlarmPlayer.startRinging(context, medicineName, dosage)
    }

    fun stopActiveAlarm() {
        val context = getApplication<Application>()
        MedicineAlarmPlayer.stopRinging(context)
    }

    fun addFamilyMember(name: String, relation: String, age: Int, emergencyPhone: String) {
        viewModelScope.launch {
            val member = LocalFamilyMemberEntity(
                id = "fam_${System.currentTimeMillis()}",
                name = name,
                relation = relation,
                age = age,
                emergencyContact = emergencyPhone,
                hasConsentGiven = true
            )
            db.familyMemberDao().insertFamilyMember(member)
        }
    }

    fun removeFamilyMember(member: LocalFamilyMemberEntity) {
        viewModelScope.launch {
            db.familyMemberDao().deleteFamilyMember(member)
        }
    }
}

