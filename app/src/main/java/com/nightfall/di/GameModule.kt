package com.nightfall.di

import com.nightfall.data.repo.ChatRepositoryImpl
import com.nightfall.data.repo.GameRepositoryImpl
import com.nightfall.domain.repo.ChatRepository
import com.nightfall.domain.repo.GameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(
        impl: GameRepositoryImpl
    ): GameRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository
}