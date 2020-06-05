package org.bloodnation.bloodnation.search

import org.bloodnation.bloodnation.contact.PhoneBookWithNumbers

interface OnAdapterValueChanged {

    fun onValueChanged(index: Int, book: PhoneBookWithNumbers)
}