package services

import models.{Role, DB}

object RoleService {
  val DEFAULT_ROLE_NAME = "guest"

  def findByName(id: String) = DB.query[Role].whereEqual("name", id).fetchOne()
  def findAll = DB.query[Role].fetch()
  def defaultRole = findByName(DEFAULT_ROLE_NAME).get
}
