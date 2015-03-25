package security

import models.User
import models.User.UserPersisted
import models.enums.Permissions
import security.SecurityService._
import sorm.Persisted
import utils.StringUtils._

object AccessCheckers {

  /**
   * Check if entity user is currently logged in user.
   */
  def accessToMe = new AccessChecker[UserPersisted]((userToCheck, user) =>
    userToCheck.email == user.email // TODO check id instead of email
  )

  /**
   * Check if currently logged in user is able to read user
   */
  def userReadable = new AccessChecker[User]((userToCheck, user) =>
    if (user.hasPermission(Permissions.USER_READ_ALL))
      true
    else if (user.hasPermission(Permissions.USER_READ_SAME_DOMAIN))
      getEmailServer(userToCheck.email) == getEmailServer(user.email)
    else
      userToCheck.email == user.email
  )

  /**
   * Check if currently logged in user is able to edit user
   */
  def userEditable = new AccessChecker[UserPersisted]((userToCheck, user) =>
    if (user.hasPermission(Permissions.USER_EDIT_ALL))
      true
    else if (user.hasPermission(Permissions.USER_EDIT_SAME_DOMAIN))
      getEmailServer(userToCheck.email) == getEmailServer(user.email)
    else
      userToCheck.email == user.email
//      accessToMe.hasAccess(userToCheck)(user)
  )

  /**
   * Check if currently logged in user is able to make changes to user entity.
   */
  def userEdit(userBeforeEdit: UserPersisted) = userEditable && userEditable(userBeforeEdit) &&
    new AccessChecker[UserPersisted]((userToCheck, user) =>
      (userBeforeEdit.id == userToCheck.id) &&
        (user.hasPermission(Permissions.USER_CHANGE_ROLE) || userBeforeEdit.role == userToCheck.role)
    )

  /**
   * Check if user has permission
   */
  def hasPermission(permission: Permissions.Value) = new AccessChecker0(user => user.hasPermission(permission))

  /**
   * Check equality of entity's id
   */
  def idIs(id: Long) = new AccessChecker[Persisted]((entity, user) =>
    entity.id == id
  )
}
