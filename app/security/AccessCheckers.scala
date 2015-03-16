package security

import models.User
import security.SecurityService._
import sorm.Persisted
import utils.StringUtils._

object AccessCheckers {

  def userRead = new AccessChecker[User]((userToCheck, user) =>
    // Check domain names for now
    getEmailServer(userToCheck.email) == getEmailServer(user.email)
  )

  def idIs(id: Long) = new AccessChecker[Persisted]((entity, user) =>
    entity.id == id
  )
}
