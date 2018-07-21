package org.tagdynamics.backend

import java.io.File

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.tagdynamics.backend.revcounts.{CountLoaders, RevisionCountRegistryActor, RevisionCountRoutes}
import org.tagdynamics.backend.status.StatusRoute

case class SourceMetadata(downloaded: String, md5: String, selectedTags: Seq[String])

object Main extends App with RevisionCountRoutes {

  // set up ActorSystem and other dependencies here
  implicit val system: ActorSystem = ActorSystem("tag-dynamics-backend")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val log = Logging.getLogger(system, this)

  // directory with aggregated data
  val dataDir: String = Utils.getEnvironmentVariable("DATA_DIRECTORY")
  log.info(s"Directory with aggregated data $dataDir")

  // OSM source metadata
  val sourceMetadata = SourceMetadata(
    downloaded = Utils.getEnvironmentVariable("OSM_SOURCE_DOWNLOADED"),
    md5 = Utils.getEnvironmentVariable("OSM_SOURCE_MD5"),
    selectedTags = Utils.getEnvironmentVariable("SELECTED_TAGS").split(',')
  )
  log.info(s"metadata for input OSM data: $sourceMetadata")
  log.info(s"metadata number of extracted tags = ${sourceMetadata.selectedTags.length}")

  val tagStats = CountLoaders.loadFromDirectory(new File(dataDir).toURI)
  val revCountActor: ActorRef = system.actorOf(Props(new RevisionCountRegistryActor(tagStats, sourceMetadata)), "revCountActor")

  val routes: Route = revisionCountRoutes(revCountActor) ~ StatusRoute.route

  val interface = Utils.getEnvironmentVariable("HTTP_INTERFACE_NAME")
  val port = Utils.getEnvironmentVariable("HTTP_PORT").toInt

  log.info(s"Hosting api on $interface:$port")
  Http().bindAndHandle(routes, interface, port)

  Await.result(system.whenTerminated, Duration.Inf)
}
