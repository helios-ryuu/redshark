package com.helios.redshark.core.di

import com.helios.redshark.data.remote.firestore.FirestoreSource
import com.helios.redshark.data.remote.firestore.FirestoreSourceImpl
import com.helios.redshark.data.repository.AuthRepositoryImpl
import com.helios.redshark.data.repository.MediaRepositoryImpl
import com.helios.redshark.data.repository.ProfileRepositoryImpl
import com.helios.redshark.domain.repository.AuthRepository
import com.helios.redshark.domain.repository.MediaRepository
import com.helios.redshark.domain.repository.ProfileRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds
    @Singleton
    abstract fun bindFirestoreSource(impl: FirestoreSourceImpl): FirestoreSource
}
