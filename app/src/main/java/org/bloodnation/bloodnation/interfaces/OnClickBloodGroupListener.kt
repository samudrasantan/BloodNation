package org.bloodnation.bloodnation.interfaces

import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers

interface OnClickBloodGroupListener
{
    fun onClickItem(contacts: PhoneBookWithNumbers, position: Int)
} //interface