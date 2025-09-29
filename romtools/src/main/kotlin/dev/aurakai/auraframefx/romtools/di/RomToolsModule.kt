// File: romtools/src/main/kotlin/dev/aurakai/auraframefx/romtools/di/RomToolsModule.kt
package dev.aurakai.auraframefx.romtools.di

// Hilt imports temporarily commented out
// import dagger.Binds
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
import android.content.Context
import dev.aurakai.auraframefx.romtools.BackupManager
import dev.aurakai.auraframefx.romtools.BackupManagerImpl
import dev.aurakai.auraframefx.romtools.FlashManager
import dev.aurakai.auraframefx.romtools.FlashManagerImpl
import dev.aurakai.auraframefx.romtools.RecoveryManager
import dev.aurakai.auraframefx.romtools.RecoveryManagerImpl
import dev.aurakai.auraframefx.romtools.RomVerificationManager
import dev.aurakai.auraframefx.romtools.RomVerificationManagerImpl
import dev.aurakai.auraframefx.romtools.SystemModificationManager
import dev.aurakai.auraframefx.romtools.SystemModificationManagerImpl
import dev.aurakai.auraframefx.romtools.bootloader.BootloaderManager
import dev.aurakai.auraframefx.romtools.bootloader.BootloaderManagerImpl

/**
 * Hilt module for providing dependencies for the ROM tools.
 *
 * Note: Hilt annotations are temporarily commented out.
 */
// @Module
// @InstallIn(SingletonComponent::class)
class RomToolsModule {

    /**
     * Binds the [BootloaderManagerImpl] to the [BootloaderManager] interface.
     */
    // @Binds
    // @Singleton
    fun bindBootloaderManager(
        bootloaderManagerImpl: BootloaderManagerImpl
    ): BootloaderManager = bootloaderManagerImpl

    /**
     * Binds the [RecoveryManagerImpl] to the [RecoveryManager] interface.
     */
    // @Binds
    // @Singleton
    fun bindRecoveryManager(
        recoveryManagerImpl: RecoveryManagerImpl
    ): RecoveryManager = recoveryManagerImpl

    /**
     * Binds the [SystemModificationManagerImpl] to the [SystemModificationManager] interface.
     */
    // @Binds
    // @Singleton
    fun bindSystemModificationManager(
        systemModificationManagerImpl: SystemModificationManagerImpl
    ): SystemModificationManager = systemModificationManagerImpl

    /**
     * Binds the [FlashManagerImpl] to the [FlashManager] interface.
     */
    // @Binds
    // @Singleton
    fun bindFlashManager(
        flashManagerImpl: FlashManagerImpl
    ): FlashManager = flashManagerImpl

    /**
     * Binds the [RomVerificationManagerImpl] to the [RomVerificationManager] interface.
     */
    // @Binds
    // @Singleton
    fun bindRomVerificationManager(
        romVerificationManagerImpl: RomVerificationManagerImpl
    ): RomVerificationManager = romVerificationManagerImpl

    /**
     * Binds the [BackupManagerImpl] to the [BackupManager] interface.
     */
    // @Binds
    // @Singleton
    fun bindBackupManager(
        backupManagerImpl: BackupManagerImpl
    ): BackupManager = backupManagerImpl

    companion object {

        /**
         * Provides the data directory for the ROM tools.
         */
        // @Provides
        // @RomToolsDataDir
        fun provideRomToolsDataDirectory(
            // @ApplicationContext 
            context: Context
        ): String {
            return "${context.filesDir}/romtools"
        }

        /**
         * Provides the backup directory for the ROM tools.
         */
        // @Provides
        // @RomToolsBackupDir
        fun provideRomToolsBackupDirectory(
            // @ApplicationContext 
            context: Context
        ): String {
            return "${context.getExternalFilesDir(null)}/backups"
        }

        /**
         * Provides the download directory for the ROM tools.
         */
        // @Provides
        // @RomToolsDownloadDir
        fun provideRomToolsDownloadDirectory(
            // @ApplicationContext 
            context: Context
        ): String {
            return "${context.getExternalFilesDir(null)}/downloads"
        }

        /**
         * Provides the temporary directory for the ROM tools.
         */
        // @Provides
        // @RomToolsTempDir
        fun provideRomToolsTempDirectory(
            // @ApplicationContext 
            context: Context
        ): String {
            return "${context.cacheDir}/romtools_temp"
        }
    }
}

/**
 * Qualifier for the data directory for the ROM tools.
 */
@Retention(AnnotationRetention.BINARY)
annotation class RomToolsDataDir

/**
 * Qualifier for the backup directory for the ROM tools.
 */
@Retention(AnnotationRetention.BINARY)
annotation class RomToolsBackupDir

/**
 * Qualifier for the download directory for the ROM tools.
 */
@Retention(AnnotationRetention.BINARY)
annotation class RomToolsDownloadDir

/**
 * Qualifier for the temporary directory for the ROM tools.
 */
@Retention(AnnotationRetention.BINARY)
annotation class RomToolsTempDir
