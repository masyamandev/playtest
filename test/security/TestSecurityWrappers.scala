package security

import exceptions.AccessDeniedException
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

  def globalUserHasEmail[String](email: String) = new AccessChecker0 ( (user: User) => {
      assert(user === currentUser)
      user.email == email
    }
  )

  def entityUserHasEmail[String](email: String) = new AccessChecker[User] ( (entity: User, user: User) => {
      assert(user === currentUser)
      entity.email == email
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

    def returnEntity(): User = preAuthorize(globalUserHasEmail("email")) {
      entity
    }

    val returnedEntity = returnEntity()

    assert(returnedEntity === entity)
  }


  test("test preAuthorized denied") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = preAuthorize(globalUserHasEmail("otherEmail")) {
      entity
    }

    intercept[AccessDeniedException] {
      returnEntity()
    }
  }


  test("test complex postAuthorized passed") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = postAuthorize(allow(entity) && globalUserHasEmail("email")) {
      entity
    }

    val returnedEntity = returnEntity()

    assert(returnedEntity === entity)
  }

  test("test complex postAuthorized denied") {
    val entity: User = User("otherName", "otherEmail", "password", role)

    def returnEntity(): User = postAuthorize(globalUserHasEmail("wrongEmail") && allow(entity)) {
      entity
    }

    intercept[AccessDeniedException] {
      returnEntity()
    }
  }

  test("test postFilter") {
    val users = List(
      User("name1", "email1", "password", role),
      User("name2", "email2", "password", role),
      User("name3", "email3", "password", role),
      User("name4", "email4", "password", role))

    val filtered = postFilter(entityUserHasEmail("email2") || entityUserHasEmail("email3")) { users }

    assert(filtered.size === 2)
    assert(filtered(0).email === "email2")
    assert(filtered(1).email === "email3")
  }

  test("test collection filter") {
    val users = List(
      User("name1", "email1", "password", role),
      User("name2", "email2", "password", role),
      User("name3", "email3", "password", role),
      User("name4", "email4", "password", role))

    val filtered = users.filter(entityUserHasEmail("email2") || entityUserHasEmail("email3"))

    assert(filtered.size === 2)
    assert(filtered(0).email === "email2")
    assert(filtered(1).email === "email3")
  }

  test("test accessFold lazy invocation") {
    val result = accessFold(
      globalUserHasEmail("email1") -> fail("Method should not be invoked"),
      globalUserHasEmail("email") -> "ok",
      globalUserHasEmail("email2") -> fail("Method should not be invoked")
    )
    assert(result === "ok")
  }

  test("test accessFold default action") {
    val result = accessFold(
      globalUserHasEmail("email1") -> fail("Method should not be invoked"),
      globalUserHasEmail("email2") -> fail("Method should not be invoked"),
      globalUserHasEmail("email3") -> fail("Method should not be invoked"),
      withDefaultValue -> "ok"
    )
    assert(result === "ok")
  }

  test("test accessFold throws AccessDeniedException if no matched rule is found") {
    intercept[AccessDeniedException] {
      val result = accessFold(
        globalUserHasEmail("email1") -> fail("Method should not be invoked"),
        globalUserHasEmail("email2") -> fail("Method should not be invoked"),
        globalUserHasEmail("email3") -> fail("Method should not be invoked")
      )
    }
  }
}
