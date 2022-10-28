package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.i18n._
import play.api.mvc._
import play.api.i18n._
import models.TaskListInMemoryModel
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsError
import play.api.libs.json._

@Singleton
class TaskList @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action{
    Ok(views.html.index())
  }
  def load= Action{ implicit request =>
    Ok(views.html.main())

  }

  implicit val userDataReads = Json.reads[UserData]
//ud is user data
  def validate = Action { implicit request =>
    request.body.asJson.map{ body=>
      Json.fromJson[UserData](body) match{
        case JsSuccess(ud,path) =>
          if (TaskListInMemoryModel.validateUser(ud.username,ud.password)){
            Ok(Json.toJson(true))
              .withSession("username" -> ud.username, "csfrToken" -> play.filters.csrf.CSRF.getToken.get.value)
          }
          else{
            Ok(Json.toJson(false))
            
          }

        case e @ JsError(_) => Redirect(routes.TaskList.load)
      }
    }.getOrElse{Redirect(routes.TaskList.load)}

  }

  def data = Action {
    Ok(Json.toJson(Seq("a","b","c")))
  }

  def taskList = Action {implicit request =>
  val usernameOption = request.session.get("username")
  usernameOption.map { username =>
    Ok(Json.toJson(TaskListInMemoryModel.getTasks(username)))
  }.getOrElse {Ok(Json.toJson(Seq.empty[String]))}
}
  def createUser = Action{ implicit requset =>
    requset.body.asJson.map{ body=>
      Json.fromJson[UserData](body) match{
        case JsSuccess(ud,path) =>
          if (TaskListInMemoryModel.createUser(ud.username,ud.password)){
            Ok(Json.toJson(true))
              .withSession("username" -> ud.username, "csfrToken" -> play.filters.csrf.CSRF.getToken.get.value)
          }
          else{
            Ok(Json.toJson(false))
            
          }

        case e @ JsError(_) => Redirect(routes.TaskList.load)
      }
    }.getOrElse{Redirect(routes.TaskList.load)}

  }
  def addTask = Action{ implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>
    request.body.asJson.map{ body=>
      Json.fromJson[String](body) match{
        case JsSuccess(task,path) =>
          TaskListInMemoryModel.addTask(username,task);
          Ok(Json.toJson(true))
        case e @ JsError(_) => Redirect(routes.TaskList.load)
      }
    }.getOrElse{Ok(Json.toJson(false))}
  }.getOrElse{Ok(Json.toJson(false))}
  }


  def delete = Action{ implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>
    request.body.asJson.map{ body=>
      Json.fromJson[Int](body) match{
        case JsSuccess(index,path) =>
          TaskListInMemoryModel.removeTask(username,index);
          Ok(Json.toJson(true))
        case e @ JsError(_) => Redirect(routes.TaskList.load)
      }
    }.getOrElse{Ok(Json.toJson(false))}
  }.getOrElse{Ok(Json.toJson(false))}
  }

  def logout = Action{ implicit request =>
    Ok(Json.toJson(true)).withNewSession
  }
}







