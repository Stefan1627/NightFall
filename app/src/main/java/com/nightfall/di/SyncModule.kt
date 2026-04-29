package com.nightfall.di

import com.nightfall.data.repo.LobbyRepositoryImpl
import com.nightfall.domain.repo.LobbyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindLobbyRepository(
        impl: LobbyRepositoryImpl
    ): LobbyRepository
}