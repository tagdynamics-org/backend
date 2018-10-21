package org.tagdynamics.backend.perdaydelta

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK}
import akka.http.scaladsl.server.Directives.onSuccess
import akka.util.Timeout
import akka.pattern.ask
import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path
import org.tagdynamics.aggregator.common.{DayStamp, ElementState, Visible}
import org.tagdynamics.backend.{JsonSupport, SourceMetadata}
import org.tagdynamics.backend.revcounts.TagStats

object PerDayDeltaRegistryActorMessages {
  final case class Request(state: ElementState)
  final case class Response(deltas: Map[DayStamp, Int],
                            tagstats: TagStats,
                            dataSet: SourceMetadata)
}

class PerDayDeltaRegistryActor(deltas: Map[ElementState, Map[DayStamp, Int]],
                               tagStats: Map[ElementState, TagStats],
                               dataSet: SourceMetadata) extends Actor with ActorLogging {
  log.info(s" Init with tagStats for ${tagStats.keySet.size} element states")
  log.info(s" Init with deltas for ${deltas.keySet.size} element states")

  def receive: Receive = {
    case PerDayDeltaRegistryActorMessages.Request(es: ElementState) =>
      log.info(s" req delta data for: $es")

      // Note: this will crash if key is not found in both maps
      val result = PerDayDeltaRegistryActorMessages.Response(deltas(es), tagStats(es), dataSet)
      sender() ! result

    case _ => throw new Exception("Received unknown message")
  }
}

trait PerDayDeltaRoute extends JsonSupport {
  implicit def system: ActorSystem

  def perDayDeltaRoute(perDayDeltaRegistryActor: ActorRef, selectedTags: Seq[String]): Route = {

    // TODO: the below contains quite a lot of common code with the transition endpoint

    type CategoricalTag = String // "amenity"
    type CategoricalTagHexEncoded = String // "0"

    implicit lazy val timeout: Timeout = Timeout(5.seconds) // required by ? (ask) below

    /**
     * GET /per-day-deltas?key1=value1&key2=value2&...
     */
    val lookUp: Map[CategoricalTag, CategoricalTagHexEncoded] =
      selectedTags.zipWithIndex.map { case (str, idx) => (str, idx.toHexString) }.toMap
    val selectedTagSet = selectedTags.toSet

    val route: Route = (get & path("per-day-deltas")) {
      parameterMap {
        params: Map[CategoricalTag, String] => {
          if (params.keySet.isEmpty) complete ((NotFound, "No tag(s) given"))
          else if (!params.keySet.subsetOf(selectedTagSet)) complete ((NotFound, "Unknown tag"))
          else {
            // Convert eg. { amenity -> bank, man_made -> yes } into List("0:bank", "a:yes") with
            // tags listed in the same order as they appear in `selectedTags`
            val tags: Seq[String] = for {
              t <- selectedTags
              value <- params.get(t)
            } yield s"${lookUp(t)}:$value"

            // do not support deltas for Deleted tag
            val elementState = Visible(tags.toList)
            val query = PerDayDeltaRegistryActorMessages.Request(elementState)
            val resultF = (perDayDeltaRegistryActor ? query).mapTo[PerDayDeltaRegistryActorMessages.Response]
            onSuccess(resultF) {
              result: PerDayDeltaRegistryActorMessages.Response => {
                complete((OK, result))
              }
            }
          }
        }
      }
    }
    route
  }
}


