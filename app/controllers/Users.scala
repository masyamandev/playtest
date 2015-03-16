package controllers

import controllers.actions.UserSupport
import models.{DB, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.mvc._
import security.PasswordHasher
import services.UserService

case class LoginForm(email: String, password: String)

object Users extends Controller with UserSupport {

  def list = loggedInAction { implicit request =>
    val users = UserService.getAllUsersFromDomain
    Ok(views.html.users.list(users))
  }

  def showUser(id: Long) = loggedInAction { implicit request =>
    val user = UserService.getUserById(id)
    Ok(views.html.users.list(user.toList)) // reuse the same list form for simplicity
  }

  def registerForm = Action { implicit request =>
    Ok(views.html.users.registerForm(userFormData))
  }

  def loginForm = Action { implicit request =>
    Ok(views.html.users.loginForm(loginFormData))
  }

  val userFormData: Form[User] = Form (
    mapping (
      "name" -> (nonEmptyText verifying uniqueNameConstraint),
      "email" -> (email verifying uniqueEmailConstraint),
      "password" -> (nonEmptyText verifying passwordStrengthConstraint)
    ) (User.apply)(User.unapply)
  )

  def uniqueNameConstraint = Constraint[String] { name: String =>
    if (!DB.query[User].whereEqual("name", name).exists) Valid
    else Invalid(s"User with name ${name} is already registered!")
  }

  def uniqueEmailConstraint = Constraint[String] { email: String =>
    if (!DB.query[User].whereEqual("email", email).exists) Valid
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

  def addUser = Action { implicit request =>
    val userForm = userFormData.bindFromRequest
    userForm.fold(
      formWithErrors => {
        BadRequest(views.html.users.registerForm(formWithErrors))
      },
      newUser => {
        val hashedPass = newUser.copy(password = PasswordHasher.hashPassword(newUser.password))
        DB.save(hashedPass)
        Redirect(routes.Users.list).withSession(Security.username -> newUser.email)
      }
    )
  }

  def login = Action { implicit request =>
    val loginForm: Form[LoginForm] = loginFormData.bindFromRequest
    loginForm.fold(
      formWithErrors => {
        BadRequest(views.html.users.loginForm(formWithErrors))
      },
      form => {
        val user: Option[User] = DB.query[User]
          .whereEqual("email", form.email).fetchOne()
          .filter(user => PasswordHasher.checkPassword(form.password, user.password))

        user match {
          case Some(user) =>
            Redirect(routes.Users.list()).withSession(Security.username -> user.email)
          case None =>
            BadRequest(views.html.users.loginForm(loginForm.withGlobalError("User email or password is wrong!")))
        }
      }
    )
  }

  def logout = Action {
    Redirect(routes.Users.loginForm).withNewSession.flashing()
  }
}
