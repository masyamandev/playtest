package security

import models.User
import models.User.UserPersisted
import models.enums.Permissions
import security.SecurityService._
import sorm.Persisted
import utils.StringUtils._

object AccessCheckers {

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
  )

  /**
   * Check if currently logged in user is able to make changes to user entity.
   */
  def userEdit(userBeforeEdit: UserPersisted) = new AccessChecker[UserPersisted]((userToCheck, user) =>
    user.hasPermission(Permissions.USER_CHANGE_ROLE) || userBeforeEdit.role == userToCheck.role
  ) && userEditable(userBeforeEdit) && userEditable

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
