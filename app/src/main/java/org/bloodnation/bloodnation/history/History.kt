package org.bloodnation.bloodnation.history

import com.google.firebase.Timestamp
import java.util.*

data class History (
    var donor_id: String= "",
    var donation: Timestamp = Timestamp(Date())
)