package models

import models.enums.Permissions
import sorm.Persisted

case class User(name: String, email: String, password: String, role: Role) {
  def hasPermission(permission: Permissions.Value): Boolean = role.permissions.contains(permission.toString)
}

object User {
  type UserPersisted = (User with Persisted)
}
