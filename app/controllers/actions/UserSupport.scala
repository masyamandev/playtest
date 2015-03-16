package controllers.actions

import controllers.Users._
import controllers.routes
import models.{DB, User}
import play.api.mvc._

trait UserSupport {

  def loginFormRedirect: Result = Redirect(routes.Users.loginForm())

  implicit def getCurrentUser(implicit request: Request[AnyContent]): User = getCurrentUserAsOption(request).get

  implicit def getCurrentUserAsOption(implicit request: Request[AnyContent]): Option[User] =
    request.session.get(Security.username) match {
      case None => None
      case Some(username) => DB.query[User].whereEqual("email", username).fetchOne()
    }

  def loggedInAction(action: Request[AnyContent] => Result): Action[AnyContent] = Action { implicit request =>
    getCurrentUserAsOption match {
      case None => loginFormRedirect
      case Some(_) => action(request)
    }
  }
}
