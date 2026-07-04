package com.helios.redshark.core.di

// File nay khai bao dependency injection dung chung cho Hilt.

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Ham nay gom mot buoc xu ly ro rang de phan con lai co the goi lai.
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
