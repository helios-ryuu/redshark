package com.helios.redshark.core.di

// File nay khai bao dependency injection dung chung cho Hilt.

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
