package exceptions

/**
 * Throwed when not logged in user tries to access to some resource that requires to be logged in.
 */
case class NotLoggedInException(msg: String) extends Exception(msg)

/**
 * Throwed when permission check failed.
 */
case class AccessDeniedException(msg: String) extends Exception(msg)