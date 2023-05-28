package fi.organization.androidproject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Represents a dummy JSON object containing a list of Person-objects.
 * This class is used to deserialize JSON data and retrieve the list of Person-objects.
 *
 * @property users The list of Person-objects
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class DummyJsonObject(var users: MutableList<Person>? = null)