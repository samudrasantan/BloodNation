package org.bloodnation.bloodnation.contact

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.bloodnation.bloodnation.repository.BN

@Dao
interface PhoneBookDao {

   @Transaction
   @Query("SELECT * FROM ${BN.tablePhoneBook} ORDER BY ${BN.phoneBookContactName}")
   suspend fun getAll(): List<PhoneBookWithNumbers>


   @Transaction
   @Query("SELECT * FROM ${BN.tablePhoneBook} WHERE ${BN.phoneBookContactName} LIKE :text ORDER BY ${BN.phoneBookContactName}")
   suspend fun getSelectedName(text: String): List<PhoneBookWithNumbers>

   @Transaction
   @Query("SELECT * FROM ${BN.tablePhoneBook} WHERE ${BN.phoneBookBloodGroup} = :bloodGroup ORDER BY ${BN.phoneBookContactName}")
   suspend fun getSelected(bloodGroup: String): List<PhoneBookWithNumbers>

   @Insert(onConflict = OnConflictStrategy.REPLACE)
   suspend fun insert(phoneBook: PhoneBookEntity)

   @Query("UPDATE ${BN.tablePhoneBook} " +
           "SET ${BN.phoneBookBloodGroup} = :bloodGroup, ${BN.phoneBookDonorId} = :donorId, ${BN.phoneBookPriority} = :priorityIndex, ${BN.phoneBookLatitude} =:latitude, ${BN.phoneBookLongitude} = :longitude " +
           "WHERE ${BN.phoneBookContactId} = :contactId")
   suspend fun update(contactId :Int, bloodGroup :String, donorId :String, priorityIndex: Long = 0, latitude: Double, longitude:Double)


   @Query("UPDATE ${BN.tablePhoneBook} " +
           "SET ${BN.phoneBookBloodGroup} = :bloodGroup " +
           "WHERE ${BN.phoneBookContactId} = :contactId")
   suspend fun updateBlood(contactId :Int, bloodGroup :String)

   @Delete
   fun delete(phoneBook: PhoneBookEntity)
}