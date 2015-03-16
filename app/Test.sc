import models.User
import models.enums.Permissions
import security.SecurityService._
import security.AccessCheckers._
import services.UserService

userRead.allowIfNone
idIs(1).allowIfNone
userRead.allowIfNone || idIs(1).allowIfNone
(userRead || idIs(1)).allowIfNone
//postAuthorize(userRead.allowIfNone || idIsOne.allowIfNone)
userRead(User("", "@xxx.com", "")) || userRead.allowIfNone || idIs(1).allowIfNone

userRead || idIs(1) || userRead(User("", "@xxx.com", ""))

Permissions.values
Permissions.USER_EDIT_ALL
