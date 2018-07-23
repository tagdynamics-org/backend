package org.tagdynamics.backend.status

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import org.tagdynamics.backend.JsonSupport

final case class StatusResponse(status: String)

/** Define GET /status endpoint */
object StatusRoute extends JsonSupport {

  def route(implicit system: ActorSystem): Route = {
    (get & path("status")) {
      val log = Logging.getLogger(system, this)
      log.info("GET /status")

      complete((OK, StatusResponse("OK")))
    }
  }
}
