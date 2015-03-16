package models.enums

object Permissions extends Enumeration {
  //type Permissions = Value
  val
    USER_READ_SAME_DOMAIN,
    USER_READ_ALL,
    USER_EDIT_SAME_DOMAIN,
    USER_EDIT_ALL,
    USER_CHANGE_ROLE
    = Value
}
