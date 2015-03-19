package controllers

import controllers.Login._
import controllers.actions.UserSupport
import play.api.mvc.Controller
import services.UserService

object Users extends Controller with UserSupport {

  def list = loggedInAction { implicit request =>
    val users = UserService.getAllUsersFiltered
    Ok(views.html.users.list(users))
  }

  def showUser(id: Long) = loggedInAction { implicit request =>
    val user = UserService.getUserById(id)
    Ok(views.html.users.list(user.toList)) // reuse the same list form for simplicity
  }

}
