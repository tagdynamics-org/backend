package org.tagdynamics.backend.transitions

import org.tagdynamics.aggregator.common.Visible
import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.model.StatusCodes.{OK, NotFound}
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
import org.tagdynamics.aggregator.common.ElementState
import org.tagdynamics.backend.{JsonSupport, SourceMetadata}

import scala.concurrent.Future

case object TransitionsMessages {
  case class Request(tagState: ElementState)
  case class Response(transitions: Seq[ToFromStats], dataSet: SourceMetadata)
}

class TransitionRegistryActor(transitionStats: Map[ElementState, Seq[ToFromStats]],
                              dataSet: SourceMetadata) extends Actor with ActorLogging {

  log.info(s" Init with data for ${transitionStats.keySet.size} distinct element states")

  def receive: Receive = {
    case TransitionsMessages.Request(es: ElementState) =>
      log.info(s" req for firstIndex = $es")
      // TODO: will crash if key is not found
      val result = TransitionsMessages.Response(transitionStats(es), dataSet)
      sender() ! result

    case _ => throw new Exception("Received unknown message")
  }
}

trait TransitionCountRoutes extends JsonSupport {

  implicit def system: ActorSystem

  def transitionRoutes(transitionRegistryActor: ActorRef, selectedTags: Seq[String]): Route = {
    type CategoricalTag = String // "amenity"
    type CategoricalTagHexEncoded = String // "0"

    implicit lazy val timeout: Timeout = Timeout(5.seconds) // required by ? (ask) below

    /**
     * GET /transitions?key1=value1&key2=value2&...
     *
     * See
     *   - https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/parameter-directives/parameters.html
     *   - local testing
     *      curl -v -H "Origin: http://localhost:8081" 'http://0.0.0.0:8080/tag-states?sorting=LiveCounts.Ascending&first-index=0&n=200'
     */
    val lookUp: Map[CategoricalTag, CategoricalTagHexEncoded] =
      selectedTags.zipWithIndex.map { case (str, idx) => (str, idx.toHexString) }.toMap
    val selectedTagSet = selectedTags.toSet

    val getSortedByTotal: Route = (get & path("transitions")) {
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


            // For now do not support requests for transitions to/from Deleted/NotCreated states
            // (they are likely a lot)
            val elementState = Visible(tags.toList)
            val query = TransitionsMessages.Request(elementState)
            val resultF: Future[TransitionsMessages.Response] = (transitionRegistryActor ? query).mapTo[TransitionsMessages.Response]
            onSuccess(resultF) {
              result: TransitionsMessages.Response => {
                complete((OK, result))
              }
            }
          }
        }
      }
    }

    getSortedByTotal
  }
}

