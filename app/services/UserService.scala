package services

import models.{DB, User}
import security.AccessCheckers._
import security.SecurityService._
import sorm.Persisted
import utils.StringUtils

object UserService {

//  def getAllUsersFromDomain(implicit user: User): Seq[User] = {
////    DB.query[User].order("name").fetch()
//    val domain = StringUtils.getEmailServer(user.email)
//    DB.query[User].whereLike("email", "%@" + domain).order("name").fetch()
//  }

  def getAllUsersFromDomain(implicit user: User): Seq[User with Persisted] = postFilter(userRead || idIs(1) || userRead(User("", "@xxx.com", ""))) {
    DB.query[User].order("name").fetch()
  }

//  def getUserById(id: Long)(implicit user: User): Option[User] = {
//    DB.query[User].whereEqual("id", id).fetchOne()
//  }

  def getUserById(id: Long)(implicit user: User): Option[User] = postAuthorizeAllowNone(userRead || idIs(1) || userRead(User("", "@xxx.com", ""))) {
//  def getUserById(id: Long)(implicit user: User): Option[User] = postAuthorize((userRead || idIs(1)).allowIfNone) {
    DB.query[User].whereEqual("id", id).fetchOne()
  }

}
