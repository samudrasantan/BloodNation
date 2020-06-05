package org.bloodnation.bloodnation.contact

import androidx.room.Embedded
import androidx.room.Relation
import org.bloodnation.bloodnation.number.PhoneNumberEntity
import org.bloodnation.bloodnation.repository.BN

data class PhoneBookWithNumbers(
    @Embedded
    val phoneBook : PhoneBookEntity = PhoneBookEntity(),
    @Relation(parentColumn = BN.phoneBookContactId, entityColumn = BN.phoneNumberContactId)
    var numberList : List<PhoneNumberEntity> = listOf(
        PhoneNumberEntity()
    )
)