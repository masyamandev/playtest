package models

import sorm.Persisted

case class Phonebook(name: String, user: User)

case class Person(name: String, phonebook: Phonebook, contacts: Seq[Contact])

case class Contact(contactType: String, contactUid: String)


object Phonebook {
  type PhonebookPersisted = (Phonebook with Persisted)
}

object Person {
  type PersonPersisted = (Person with Persisted)
}
