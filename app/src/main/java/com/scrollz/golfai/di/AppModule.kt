package com.scrollz.golfai.di

import android.app.Application
import androidx.room.Room
import com.scrollz.golfai.data.aimodels.VideoProcessor
import com.scrollz.golfai.data.local.GolfAIDataBase
import com.scrollz.golfai.data.repository.GolfAIRepositoryImpl
import com.scrollz.golfai.domain.repository.GolfAIRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideVideoProcessor(application: Application): VideoProcessor {
        return VideoProcessor(application)
    }

    @Provides
    @Singleton
    fun provideGolfAIDataBase(app: Application): GolfAIDataBase {
        return Room.databaseBuilder(
            app,
            GolfAIDataBase::class.java,
            GolfAIDataBase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideGolfAIRepository(
        db: GolfAIDataBase
    ): GolfAIRepository {
        return GolfAIRepositoryImpl(db)
    }

}
