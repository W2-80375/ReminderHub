package com.own.remindme.di

import com.own.remindme.data.repository.ReminderRepositoryImpl
import com.own.remindme.domain.repository.ReminderRepository
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
}