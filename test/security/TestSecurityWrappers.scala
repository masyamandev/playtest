package security

import models.enums.Permissions
import models.{Role, User}
import org.scalatest.FunSuite
import SecurityService._

class TestSecurityWrappers extends FunSuite {

  implicit val role = new Role("everything", Permissions.values.map(_.toString))
  implicit val currentUser: User = User("name", "email", "password", role)

  def allow[E](e: E) = new AccessChecker[E] ( (entity: E, user: User) => {
      assert(user === currentUser)
      assert(entity === e)
      true
    }
  )

  def deny[E](e: E) = new AccessChecker[E] ( (entity: E, user: User) => {
      assert(user === currentUser)
      assert(entity === e)
      false
    }
  )

  def userHasEmail[String](email: String) = new AccessChecker0 ( (user: User) => {
      assert(user === currentUser)
      user.email == email
    }
  )

  test("test postAuthorized passed") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = postAuthorize(allow(entity)) {
      entity
    }

    val returnedEntity = returnEntity()

    assert(returnedEntity === entity)
  }

  test("test postAuthorized denied") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = postAuthorize(deny(entity)) {
      entity
    }

    intercept[AccessDeniedException] {
      returnEntity()
    }
  }


  test("test preAuthorized passed") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = preAuthorize(userHasEmail("email")) {
      entity
    }

    val returnedEntity = returnEntity()

    assert(returnedEntity === entity)
  }


  test("test preAuthorized denied") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = preAuthorize(userHasEmail("otherEmail")) {
      entity
    }

    intercept[AccessDeniedException] {
      returnEntity()
    }
  }


  test("test complex postAuthorized passed") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = postAuthorize(allow(entity) && userHasEmail("email")) {
      entity
    }

    val returnedEntity = returnEntity()

    assert(returnedEntity === entity)
  }

  test("test complex postAuthorized denied") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = postAuthorize(userHasEmail("wrongEmail") && allow(entity)) {
      entity
    }

    intercept[AccessDeniedException] {
      returnEntity()
    }
  }
}
