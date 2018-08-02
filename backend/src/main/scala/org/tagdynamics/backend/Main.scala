package org.tagdynamics.backend

import java.net.URI

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.HttpOrigin
import akka.http.scaladsl.model.headers.HttpOriginRange
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.tagdynamics.backend.revcounts.{CountLoaders, RevisionCountRegistryActor, RevisionCountRoutes, TagStats}
import org.tagdynamics.backend.status.StatusRoute
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import org.tagdynamics.aggregator.common.ElementState
import org.tagdynamics.backend.transitions.{TransitionCountRoutes, TransitionLoader, TransitionRegistryActor}

case class SourceMetadata(downloaded: String, md5: String, selectedTags: Seq[String])

object Main extends App with RevisionCountRoutes with TransitionCountRoutes {

  // set up ActorSystem and other dependencies here
  implicit val system: ActorSystem = ActorSystem("tag-dynamics-backend")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val log = Logging.getLogger(system, this)

  // OSM source metadata
  val sourceMetadata = SourceMetadata(
    downloaded = Utils.getEnvironmentVariable("OSM_SOURCE_DOWNLOADED"),
    md5 = Utils.getEnvironmentVariable("OSM_SOURCE_MD5"),
    selectedTags = Utils.getEnvironmentVariable("SELECTED_TAGS").split(',')
  )
  log.info(s"metadata for input OSM data: $sourceMetadata")
  log.info(s"metadata number of extracted tags = ${sourceMetadata.selectedTags.length}")

  // directory with aggregated data
  val dataDir: String = Utils.getEnvironmentVariable("DATA_DIRECTORY")
  log.info(s"Directory with aggregated data $dataDir")
  val data = new AggregatedDataSources(new URI(s"file://$dataDir"))
  val tagStats: Seq[(ElementState, TagStats)] = CountLoaders.computeStats(data)
  val revCountActor: ActorRef = system.actorOf(Props(new RevisionCountRegistryActor(tagStats, sourceMetadata)), "revCountActor")

  // transition routes
  val transitionRoutes: Route = {
    val transitions = TransitionLoader.process(data.transitionCounts, tagStats.toMap)
    val transitionActor: ActorRef = system.actorOf(Props(new TransitionRegistryActor(transitions, sourceMetadata)), "transitionActor")
    transitionRoutes(transitionActor, sourceMetadata.selectedTags)
  }

  // CORS settings, see
  //  - https://github.com/lomigmegard/akka-http-cors
  //  - https://github.com/lomigmegard/akka-http-cors/blob/master/akka-http-cors/src/main/resources/reference.conf
  val corsSettings = {
    def allowed: HttpOriginRange = {
      val ok: Seq[String] = Utils.getEnvironmentVariable("CORS_ALLOWED_ORIGINS").split(',')

      if (ok.contains("*") && ok.length > 1) throw new Exception("* may not be included if len > 1")
      if (ok.isEmpty) throw new Exception("CORS_ALLOWED_ORIGINS may not be empty")

      if (ok.length == 1 && ok.head == "*") HttpOriginRange.*
      else HttpOriginRange(ok.map(HttpOrigin(_)): _*)
    }

    CorsSettings.defaultSettings
      .withAllowGenericHttpRequests(Utils.getEnvironmentVariable("CORS_ALLOW_GENERIC_HTTP_REQUESTS").toBoolean)
      .withAllowCredentials(Utils.getEnvironmentVariable("CORS_ALLOW_CREDENTIALS").toBoolean)
      .withAllowedOrigins(allowed)
  }

  val routes: Route = cors(corsSettings) {
    revisionCountRoutes(revCountActor) ~ StatusRoute.route ~ transitionRoutes
  }

  val interface = Utils.getEnvironmentVariable("HTTP_INTERFACE_NAME")
  val port = Utils.getEnvironmentVariable("HTTP_PORT").toInt

  log.info(s"Hosting api on $interface:$port")
  Http().bindAndHandle(routes, interface, port)

  Await.result(system.whenTerminated, Duration.Inf)
}
