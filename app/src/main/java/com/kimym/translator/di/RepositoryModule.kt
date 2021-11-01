package com.kimym.translator.di

import com.kimym.translator.data.api.TranslatorService
import com.kimym.translator.data.repository.TranslatorRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    @ViewModelScoped
    fun provideTranslatorRepository(translatorService: TranslatorService) =
        TranslatorRepositoryImpl(translatorService)
}
