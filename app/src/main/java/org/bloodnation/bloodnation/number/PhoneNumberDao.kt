package org.bloodnation.bloodnation.number

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import org.bloodnation.bloodnation.number.PhoneNumberEntity

@Dao
interface PhoneNumberDao {

    //@Query("SELECT * FROM ${BN.tablePhoneBook} ORDER BY ${BN.phoneBookContactName}")
   // suspend fun getAll(): LiveData<List<PhoneBookEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(phoneNumber: PhoneNumberEntity)
}