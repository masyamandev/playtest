package controllers.actions

import controllers.Login._
import controllers.routes
import exceptions.{AccessDeniedException, NotLoggedInException}
import models.enums.Permissions
import models.{Role, DB, User}
import play.api.mvc._
import services.RoleService

trait UserSupport {

  def loginFormRedirect = Redirect(routes.Login.loginForm())
  def accessDeniedView = views.html.accessDenied

  implicit def getCurrentUserAsOption(implicit request: Request[AnyContent]): Option[User] =
    request.session.get(Security.username) match {
      case None => None
      case Some(username) => DB.query[User].whereEqual("email", username).fetchOne()
    }

  implicit def getCurrentUser(implicit request: Request[AnyContent]): User = getCurrentUserAsOption match {
    case None => throw NotLoggedInException("User is not logged in")
    case Some(user) => user
  }

  /**
   * Wrapped Action that support transaction and exceptions handling with redirects.
   */
  def wrappedAction(action: Request[AnyContent] => Result): Action[AnyContent] = Action { implicit request =>
    try {
      DB.transaction {
        initData
        action(request)
      }
    } catch {
      case ex: NotLoggedInException => loginFormRedirect
//      case ex: AccessDeniedException => Forbidden(accessDeniedView(ex))
    }
  }

  // TODO: put it to SQL
  def initData {
    //    if (RoleService.findAll.size == 0) {
    //      List(
    //        Role("GUEST", Set(Permissions.USER_READ_SAME_DOMAIN)),
    //        Role("LOCAL_ADMIN", Set(Permissions.USER_READ_SAME_DOMAIN, Permissions.USER_EDIT_SAME_DOMAIN, Permissions.USER_CHANGE_ROLE)),
    //        Role("SUPER_ADMIN", Set(Permissions.USER_READ_SAME_DOMAIN, Permissions.USER_READ_ALL, Permissions.USER_EDIT_SAME_DOMAIN, Permissions.USER_EDIT_ALL, Permissions.USER_CHANGE_ROLE))
    //      )
    //    }.map(DB.save)
    if (RoleService.findByName(RoleService.DEFAULT_ROLE_NAME).isEmpty) {
      List(
        Role("GUEST", Set("USER_READ_SAME_DOMAIN")),
        Role("LOCAL_ADMIN", Set("USER_READ_SAME_DOMAIN", "USER_EDIT_SAME_DOMAIN", "USER_CHANGE_ROLE")),
        Role("SUPER_ADMIN", Set("USER_READ_SAME_DOMAIN", "USER_READ_ALL", "USER_EDIT_SAME_DOMAIN", "USER_EDIT_ALL", "USER_CHANGE_ROLE"))
      )
    }.map(DB.save)
  }
}
