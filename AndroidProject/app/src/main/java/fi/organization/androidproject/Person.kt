package fi.organization.androidproject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(var firstName: String? = null, var lastName: String? = null, var age: Int = 0,
                  var email: String? = null, var phone: String? = null,
                  var id: Int = 0, var image: String? = null)