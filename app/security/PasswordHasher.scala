package security

object PasswordHasher {
  def hashPassword(password: String): String = password + "123"
  def checkPassword(password: String, hash: String): Boolean = hash == hashPassword(password)
}
