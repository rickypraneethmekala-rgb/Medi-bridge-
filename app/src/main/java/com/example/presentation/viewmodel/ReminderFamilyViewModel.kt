package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MediBridgeApp
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

    private val _isSeniorMode = MutableStateFlow(secureStorage.isSeniorMode)
    val isSeniorMode: StateFlow<Boolean> = _isSeniorMode.asStateFlow()

    fun toggleSeniorMode(enabled: Boolean) {
        secureStorage.isSeniorMode = enabled
        _isSeniorMode.value = enabled
    }

    fun addReminder(name: String, dosage: String, timesJson: String, totalDays: Int) {
        viewModelScope.launch {
            val entity = LocalReminderEntity(
                id = "rem_${System.currentTimeMillis()}",
                medicineName = name,
                dosage = dosage,
                reminderTimesJson = timesJson,
                totalDays = totalDays,
                daysRemaining = totalDays,
                isTakenToday = false
            )
            db.reminderDao().insertReminder(entity)
        }
    }

    fun markDoseTaken(reminder: LocalReminderEntity) {
        viewModelScope.launch {
            val updated = reminder.copy(
                isTakenToday = true,
                lastTakenTimestamp = System.currentTimeMillis()
            )
            db.reminderDao().updateReminder(updated)
        }
    }

    fun deleteReminder(reminder: LocalReminderEntity) {
        viewModelScope.launch {
            db.reminderDao().deleteReminder(reminder)
        }
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
