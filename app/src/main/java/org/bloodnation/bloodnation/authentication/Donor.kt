package org.bloodnation.bloodnation.authentication

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.*

data class Donor (var donor_id: String= "",
                  var name : String = "",
                  var blood : String = "",
                  var eagerness: Long = 0,
                  var date: Timestamp = Timestamp(Date()),
                  var email: String = "",
                  var phone: String = "",
                  var location: GeoPoint = GeoPoint(0.00, 0.00),
                  var donation: Timestamp = Timestamp(Date())
) {




}