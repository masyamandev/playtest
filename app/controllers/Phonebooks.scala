package controllers

import controllers.actions.UserSupport
import play.api.mvc.Controller
import services.{PhonebookService, UserService}

object Phonebooks extends Controller with UserSupport {

  def list = wrappedAction { implicit request =>
    val phonebooks = PhonebookService.getPhonebooks
    Ok(views.html.phonebooks.list(phonebooks))
  }

  def add = wrappedAction { implicit request =>
//    PhonebookService.addPhonebook(name, getCurrentUser)
    PhonebookService.addPhonebook(request.body.asFormUrlEncoded.get("name")(0), getCurrentUser)
    Redirect(routes.Phonebooks.list)
  }

  def delete(id: Long) = wrappedAction { implicit request =>
    PhonebookService.removePhonebook(id)
    Redirect(routes.Phonebooks.list)
  }

  def showPhonebook(id: Long) = wrappedAction { implicit request =>
    val phonebook = PhonebookService.getPhonebook(id)
    val persons = PhonebookService.getPhonebookRecords(phonebook, None)
    Ok(views.html.phonebooks.show(phonebook, persons)) // reuse the same list form for simplicity
  }

}
