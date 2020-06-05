package org.bloodnation.bloodnation.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.bloodnation.bloodnation.number.PhoneNumberEntity
import org.bloodnation.bloodnation.repository.BN

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(settings: SettingsEntity)

    @Query("SELECT * FROM ${BN.tableSettings}")
    fun getSettings(): Flow<SettingsEntity>

    @Query("UPDATE ${BN.tableSettings} SET ${BN.settingsTheme} = :state WHERE ${BN.settingsId} = 1 ")
    fun updateTheme(state: Int)
}