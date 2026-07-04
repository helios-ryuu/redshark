package com.helios.redshark.core.di

// File nay khai bao dependency injection dung chung cho Hilt.

import com.helios.redshark.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Module
@InstallIn(SingletonComponent::class)
object R2Module {

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()
}
