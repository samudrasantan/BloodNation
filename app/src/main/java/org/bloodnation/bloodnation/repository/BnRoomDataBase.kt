package org.bloodnation.bloodnation.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.bloodnation.bloodnation.contact.PhoneBookEntity
import org.bloodnation.bloodnation.number.PhoneNumberEntity
import org.bloodnation.bloodnation.contact.PhoneBookDao
import org.bloodnation.bloodnation.number.PhoneNumberDao
import org.bloodnation.bloodnation.settings.SettingsDao
import org.bloodnation.bloodnation.settings.SettingsEntity

@Database(entities = [PhoneBookEntity::class, PhoneNumberEntity::class, SettingsEntity::class], version = 1, exportSchema = false)
public abstract  class BnRoomDataBase: RoomDatabase() {

    abstract fun phoneBookDao(): PhoneBookDao
    abstract fun phoneNumberDao(): PhoneNumberDao
    abstract fun settingsDao(): SettingsDao
    companion object {

        @Volatile
        private var INSTANCE: BnRoomDataBase? = null

        fun database(context: Context) : BnRoomDataBase
        { val temporaryInstance = INSTANCE

            if(temporaryInstance != null){
                return temporaryInstance
            }

            else
            {
                synchronized(this) {

                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        BnRoomDataBase::class.java,
                        BN.databaseName
                    ).build()

                    INSTANCE = instance
                    return instance
                }


            }
        }

    }
}
