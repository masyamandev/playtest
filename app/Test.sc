import models.{Role, User}
import models.enums.Permissions
import security.AccessChecker0
import security.SecurityService._
import security.AccessCheckers._
import services.UserService

userReadable.allowIfNone
idIs(1).allowIfNone
userReadable.allowIfNone || idIs(1).allowIfNone
(userReadable || idIs(1)).allowIfNone
//postAuthorize(userRead.allowIfNone || idIsOne.allowIfNone)

userReadable || idIs(1)

Permissions.values
Permissions.USER_EDIT_ALL.toString

implicit val role = new Role("everything", Permissions.values.map(_.toString))
implicit val currentUser: User = User("name", "email", "password", role)

def userHasEmail[String](email: String) = new AccessChecker0 ( (user: User) => user.email == email)

val result = accessFold(
  userHasEmail("email1") -> {println("invoking1"); "1"},
  userHasEmail("email") -> {println("invoking2"); "2"},
  userHasEmail("email3") -> {println("invoking3"); "3"}
)

val c1 = userHasEmail("email1") -> {println("invoking1"); "1"}
val c2 = userHasEmail("email") -> {println("invoking2"); "2"}
val c3 = userHasEmail("email3") -> {println("invoking3"); "3"}
accessFold(c1, c2, c3)