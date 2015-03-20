package services

import models.User.UserPersisted
import models.enums.Permissions
import models.{DB, User}
import security.AccessCheckers._
import security.PasswordHasher
import security.SecurityService._
import sorm.Persisted
import utils.StringUtils

object UserService {

  def getAllUsersFromDomain(implicit user: User): Seq[UserPersisted] = {
    val domain = StringUtils.getEmailServer(user.email)
    DB.query[User].whereLike("email", "%@" + domain).order("name").fetch
  }
  def getAllUsersFiltered(implicit user: User): Seq[UserPersisted] = accessFold(
    hasPermission(Permissions.USER_READ_ALL) -> DB.query[User].order("name").fetch,
    hasPermission(Permissions.USER_READ_SAME_DOMAIN) -> getAllUsersFromDomain,
    withDefaultValue -> DB.query[User].whereEqual("email", user.email).fetchOne.toSeq
  )

//  def getAllUsersFiltered(implicit user: User): Seq[UserPersisted] = postFilter(userReadable) {
//    DB.query[User].order("name").fetch()
//  }

  def getUserById(id: Long)(implicit user: User): Option[UserPersisted] = postAuthorizeAllowNone(userReadable) {
    DB.query[User].whereEqual("id", id).fetchOne
  }

  def createUser(user: User) = DB.save(user)

  def getUserByEmailAndPassword(email: String, password: String): Option[User] =
    DB.query[User].whereEqual("email", email).fetchOne
    .filter(user => PasswordHasher.checkPassword(password, user.password))
  def isEmailRegistered(email: String): Boolean = DB.query[User].whereEqual("email", email).exists

  def isNameRegistered(name: String): Boolean = DB.query[User].whereEqual("name", name).exists

}
