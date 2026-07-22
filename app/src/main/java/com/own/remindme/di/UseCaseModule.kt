package com.own.remindme.di

import com.own.remindme.domain.repository.ReminderRepository
import com.own.remindme.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideReminderUseCases(
        repository: ReminderRepository
    ): ReminderUseCases {
        return ReminderUseCases(
            getAllReminders = GetAllRemindersUseCase(repository),
            getReminder = GetReminderUseCase(repository),
            addReminder = AddReminderUseCase(repository),
            updateReminder = UpdateReminderUseCase(repository),
            deleteReminder = DeleteReminderUseCase(repository)
        )
    }
}
