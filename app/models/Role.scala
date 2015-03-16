package models

import java.security.Permissions

case class Role(name: String, permissions: Set[Permissions])