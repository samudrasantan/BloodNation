package org.bloodnation.bloodnation.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.bloodnation.bloodnation.repository.BN

@Entity(tableName = BN.tableSettings)
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false) @ColumnInfo(name = BN.settingsId) val settingsId:Int = 1,
    @ColumnInfo(name = BN.settingsTheme) val settingsTheme: Int = 0
)