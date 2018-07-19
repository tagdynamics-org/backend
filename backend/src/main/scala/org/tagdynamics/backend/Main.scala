package org.tagdynamics.backend

import java.io.File

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.tagdynamics.backend.revcounts.{CountLoaders, RevisionCountRegistryActor, RevisionCountRoutes}

object Main extends App with RevisionCountRoutes {

  def getEnvironmentVariable(variable: String): String = {
    val res = sys.env.get(variable)

    if (res.isDefined) res.get
    else throw new Exception(s"environment variable $variable not set.")
  }

  // set up ActorSystem and other dependencies here
  implicit val system: ActorSystem = ActorSystem("tag-dynamics-backend")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val dataDir: String = getEnvironmentVariable("DATA_DIRECTORY")
  val tagStats = CountLoaders.loadFromDirectory(new File(dataDir).toURI)

  val revCountActor: ActorRef = system.actorOf(Props(new RevisionCountRegistryActor(tagStats, "dataset_version")), "revCountActor")

  val routes: Route  = revisionCountRoutes(revCountActor)

  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

}
