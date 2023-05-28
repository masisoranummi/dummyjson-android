package fi.organization.androidproject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
*
* Represents a person with various attributes such as first name, last name, age, email, phone, ID, and image.
* @property firstName The first name of the person.
* @property lastName The last name of the person.
* @property age The age of the person.
* @property email The email address of the person.
* @property phone The phone number of the person.
* @property id The ID of the person.
* @property image The image URL of the person.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Person(var firstName: String? = null, var lastName: String? = null, var age: Int = 0,
                  var email: String? = null, var phone: String? = null,
                  var id: Int = 0, var image: String? = null)