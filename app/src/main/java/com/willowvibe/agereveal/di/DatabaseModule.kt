package com.willowvibe.agereveal.di

import android.content.Context
import com.willowvibe.agereveal.data.db.AppDatabase
import com.willowvibe.agereveal.data.db.BadgeDao
import com.willowvibe.agereveal.data.db.BirthdayDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the Room database and its DAOs.
 * Installed in SingletonComponent — one instance per application lifetime.
 *
 * Delegates to [AppDatabase.getInstance] so the widget provider and Hilt share the same instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideBirthdayDao(db: AppDatabase): BirthdayDao = db.birthdayDao()

    @Provides
    fun provideBadgeDao(db: AppDatabase): BadgeDao = db.badgeDao()
}
