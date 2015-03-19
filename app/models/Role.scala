package models

import models.enums.Permissions

// Unfortunately, SORM works badly with Enumeration
case class Role(name: String, permissions: Set[String])