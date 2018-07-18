package org.tagdynamics.backend

import java.io.File

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.tagdynamics.backend.loader.CountLoaders

object Main extends UserRoutes {


  def getEnvironmentVariable(variable: String): String = {
    val res = sys.env.get(variable)

    if (res.isDefined) res.get
    else throw new Exception(s"environment variable $variable not set.")
  }

  // set up ActorSystem and other dependencies here
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

  // from the UserRoutes trait
  lazy val routes: Route = userRoutes

  def main(args: Array[String]): Unit = {
    val dataDir: String = getEnvironmentVariable("DATA_DIRECTORY")
    val liveTotalCounts = CountLoaders.loadFromDirectory(new File(dataDir).toURI)
    println(s" * Loaded total elementState counts ${liveTotalCounts.keySet.size}")

    Http().bindAndHandle(routes, "localhost", 8080)

    println(s"Server online at http://localhost:8080/")

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
