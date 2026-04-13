package com.willowvibe.agereveal.di

import android.content.Context
import androidx.room.Room
import com.willowvibe.agereveal.data.db.AppDatabase
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
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()  // TODO: replace with proper migrations before v2
            .build()

    @Provides
    fun provideBirthdayDao(db: AppDatabase): BirthdayDao = db.birthdayDao()
}
