package org.bloodnation.bloodnation.number

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.bloodnation.bloodnation.repository.BN

@Entity(tableName = BN.tablePhoneNumber)
data class PhoneNumberEntity(
    @PrimaryKey @ColumnInfo(name = BN.phoneNumberPhoneNumber) val phoneNumber:String = "",
    @ColumnInfo(name = BN.phoneNumberContactId) val contactId: Int = 0
    )