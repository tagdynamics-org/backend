package org.tagdynamics.backend.revcounts

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.{onSuccess, parameter}
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
import org.tagdynamics.backend.revcounts.RevisionCountRegistryActorMessages.{ListRequest, ListResponse}
import org.tagdynamics.backend.revcounts.SortHelper.SortSpec

object RevisionCountRegistryActorMessages {

  final case class ListRequest(firstIndex: Int,
                               n: Int,
                               sort: SortSpec)

  final case class ListResponse(entryList: Seq[(ElementState, TagStats)],
                                totalEntries: Int,
                                dataSet: SourceMetadata)

  // TODO: Request data for a specific element state

}

class RevisionCountRegistryActor(tagStats: Seq[(ElementState, TagStats)],
                                 dataSet: SourceMetadata) extends Actor with ActorLogging {

  val allSorts: Map[SortSpec, Seq[(ElementState, TagStats)]] = SortHelper.sortByAll(tagStats)
  log.info(s" Init with data for ${tagStats.length} distinct element states.")

  def receive: Receive = {
    case ListRequest(firstIndex, n, sort) =>
      if (n > 1000) throw new Exception("Max n is 1000")
      if (n < 0) throw new Exception("n should be positive")
      if (n == 0) log.warning("Warning: query with n=0")

      val result = ListResponse(
        entryList = allSorts(sort).slice(firstIndex, firstIndex + n),
        totalEntries = tagStats.length,
        dataSet = dataSet
      )
      sender() ! result

    case _ => throw new Exception("Received unknown message")
  }
}

trait RevisionCountRoutes extends JsonSupport {

  implicit def system: ActorSystem // provided later

  def revisionCountRoutes(revCountActor: ActorRef): Route = {

    implicit lazy val timeout: Timeout = Timeout(5.seconds) // required by ? (ask) below

    /**
     * GET /tag-states/?sort-by=..&first-index=12&n=2000
     *
     * See
     *   - https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/parameter-directives/parameters.html
     *   - local testing
     *      curl -v -H "Origin: http://localhost:8081" 'http://localhost:8080/tag-states?sort-by=".."&first-index=12&n=200'
     */
    val getSortedByTotal: Route = (get & path("tag-states")) {
      parameter("sort-by", "first-index".as[Int], "n".as[Int]) {
        (sortBy, firstIndex, n) => {
          println("GET categorical-tags:", sortBy, firstIndex, n)

          val query = ListRequest(
            firstIndex = firstIndex,
            n = n,
            sort = (SortHelper.SortBy.TotalCounts,SortHelper.SortOrder.Ascending)
          )

          val resultF = (revCountActor ? query).mapTo[ListResponse]
          onSuccess(resultF) {
            result: ListResponse => complete((OK, result))
          }
        }
      }
    }

    getSortedByTotal
  }
}

