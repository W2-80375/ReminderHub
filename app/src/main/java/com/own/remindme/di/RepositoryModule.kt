package com.own.remindme.di

import com.own.remindme.data.repository.NotificationRepositoryImpl
import com.own.remindme.data.repository.ReminderRepositoryImpl
import com.own.remindme.data.repository.UserPreferencesRepositoryImpl
import com.own.remindme.domain.repository.NotificationRepository
import com.own.remindme.domain.repository.ReminderRepository
import com.own.remindme.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        repository: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        repository: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        repository: NotificationRepositoryImpl
    ): NotificationRepository
}
