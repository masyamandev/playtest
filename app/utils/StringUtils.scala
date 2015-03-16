package utils

object StringUtils {
  def getEmailServer(email: String) = email.split('@')(1)
}
