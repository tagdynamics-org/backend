package org.tagdynamics.backend.status

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.onSuccess
import akka.actor.ActorSystem

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import org.tagdynamics.backend.JsonSupport

import scala.concurrent.Future

final case class StatusResponse(status: String)

/** Define GET /status endpoint */
object StatusRoute extends JsonSupport {
  import scala.concurrent.ExecutionContext.Implicits.global

  def route: Route = {
    (get & path("status")) {
      val resultF: Future[StatusResponse] = Future(StatusResponse("OK"))
      onSuccess(resultF) { result: StatusResponse => complete((OK, result)) }
    }
  }
}
