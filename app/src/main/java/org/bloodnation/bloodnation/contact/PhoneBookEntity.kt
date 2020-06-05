package org.bloodnation.bloodnation.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.GeoPoint
import org.bloodnation.bloodnation.repository.BN

@Entity(tableName = BN.tablePhoneBook)
data class PhoneBookEntity(
    @PrimaryKey @ColumnInfo(name = BN.phoneBookContactId) var contactId:Int = 0,
    @ColumnInfo(name = BN.phoneBookContactName) var contactName: String = "",
    @ColumnInfo(name = BN.phoneBookBloodGroup) var bloodGroup: String = "",
    @ColumnInfo(name = BN.phoneBookDonorId) var donorId: String = "",
    @ColumnInfo(name = BN.phoneBookPriority) var priority: Int = 0,
    @ColumnInfo(name = BN.phoneBookLatitude) var latitude: Double = 0.00,
    @ColumnInfo(name = BN.phoneBookLongitude) var longitude: Double = 0.00)