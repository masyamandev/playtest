package controllers

import controllers.actions.UserSupport
import models.User.UserPersisted
import models.{User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.mvc._
import security.PasswordHasher
import services.{RoleService, UserService}
import sorm.Persisted
import utils.FormHelpers._

case class RegisterUserForm(name: String, email: String, password: String, role: Option[String]) {
  def toUser: User = User(
    name = this.name,
    email = this.email,
    password = PasswordHasher.hashPassword(this.password),
    role = this.role match {
      case None => RoleService.defaultRole
      case Some(roleId) => RoleService.findByName(roleId) getOrElse RoleService.defaultRole
    }
  )
}
case class LoginForm(email: String, password: String)

object Login extends Controller with UserSupport {

  val successLoginRedirect = Redirect(routes.Users.list)

  def registerForm = wrappedAction { implicit request =>
    Ok(views.html.users.registerForm(userFormData))
  }

  def loginForm = wrappedAction { implicit request =>
    Ok(views.html.users.loginForm(loginFormData))
  }

  val userFormData: Form[RegisterUserForm] = Form (
    mapping (
      "name" -> (nonEmptyText verifying uniqueNameConstraint),
      "email" -> (email verifying uniqueEmailConstraint),
      "password" -> (nonEmptyText verifying passwordStrengthConstraint),
      "role" -> optional(text)
    ) (RegisterUserForm.apply)(RegisterUserForm.unapply)
  )

  //def uniqueNameConstraint = toConstraint[String](name => !UserService.isEmailRegistered(name), s"User with name ${name} is already registered!")
  def uniqueNameConstraint = Constraint[String] { name: String =>
    if (!UserService.isEmailRegistered(name)) Valid
    else Invalid(s"User with name ${name} is already registered!")
  }

  def uniqueEmailConstraint = Constraint[String] { email: String =>
    if (!UserService.isEmailRegistered(email)) Valid
    else Invalid(s"User with email ${email} is already registered!")
  }

  def passwordStrengthConstraint = Constraint[String] { password: String =>
    if (password.size >= 3) Valid
    else Invalid(s"Password is too weak! Please use password with length >= 3")
  }

  val loginFormData: Form[LoginForm] = Form (
    mapping (
      "email" -> email,
      "password" -> nonEmptyText
    ) (LoginForm.apply)(LoginForm.unapply)
  )

  def addUser = wrappedAction { implicit request =>
    val userForm = userFormData.bindFromRequest
    userForm.fold(
      formWithErrors => {
        BadRequest(views.html.users.registerForm(formWithErrors))
      },
      newUser => {
        UserService.createUser(newUser.toUser)
        successLoginRedirect.withSession(Security.username -> newUser.email)
      }
    )
  }

  def login = wrappedAction { implicit request =>
    val loginForm: Form[LoginForm] = loginFormData.bindFromRequest
    loginForm.fold(
      formWithErrors => {
        BadRequest(views.html.users.loginForm(formWithErrors))
      },
      form => {
        val user: Option[User] = UserService.getUserByEmailAndPassword(form.email, form.password)

        user match {
          case Some(user) =>
            successLoginRedirect.withSession(Security.username -> user.email)
          case None =>
            BadRequest(views.html.users.loginForm(loginForm.withGlobalError("User email or password is wrong!")))
        }
      }
    )
  }

  def logout = wrappedAction { implicit request =>
    Redirect(routes.Login.loginForm).withNewSession.flashing()
  }
}
