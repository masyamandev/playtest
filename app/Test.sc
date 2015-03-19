import models.User
import models.enums.Permissions
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

