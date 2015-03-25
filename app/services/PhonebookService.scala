package services

import models.Person.PersonPersisted
import models.Phonebook.PhonebookPersisted
import models.User._
import models.{Person, Phonebook, DB, User}
import security.AccessCheckers._
import security.SecurityService._

object PhonebookService {

  def getPhonebooks(implicit credentials: User): Seq[PhonebookPersisted] = getPhonebooksForUser(credentials)

  def getPhonebooksForUser(user: User)(implicit credentials: User): Seq[PhonebookPersisted] =
    preAuthorize(phonebooksOfUserRead(user)) {
      DB.query[Phonebook].whereEqual("user", user).fetch()
    } filter phonebookRead // not necessary now, but may be useful if shared phonebooks will be implemented

  def addPhonebook(name: String, user: User)(implicit credentials: User) =
    preAuthorize(phonebooksOfUserEdit(user)) {
      DB.save(Phonebook(name, user))
    }

  def getPhonebook(id: Long)(implicit credentials: User): PhonebookPersisted =
    postAuthorize(phonebookRead) {
//      DB.query[Phonebook].whereEqual("id", id).fetchOne()
      DB.fetchById[Phonebook](id)
    }

  def removePhonebook(id: Long)(implicit credentials: User) =
    preAuthorize(phonebookEdit(getPhonebook(id))) {
      DB.delete[Phonebook](getPhonebook(id))
    }

  def getPhonebookRecords(phonebook: Phonebook, query: Option[String])(implicit credentials: User): Seq[PersonPersisted] =
    preAuthorize(phonebookRead(phonebook)) {
      DB.query[Person].whereEqual("phonebook", phonebook).fetch()
    }
}
