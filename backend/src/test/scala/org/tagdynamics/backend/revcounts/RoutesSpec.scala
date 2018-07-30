package org.tagdynamics.backend.revcounts

import akka.actor.Props
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.JSONCustomProtocols
import org.tagdynamics.backend.TestData
import org.tagdynamics.backend.revcounts.RevisionCountRegistryActorMessages.ListResponse

class RoutesSpec extends WordSpec with RevisionCountRoutes with Matchers with ScalaFutures with ScalatestRouteTest {

  object I extends JSONCustomProtocols {
    import spray.json._
    def toJson(ds: ListResponse): String = ds.toJson.toString
    def fromJson(line: String): ListResponse = line.parseJson.convertTo[ListResponse]
  }

  val tagStats = TestData.liveTotalRevcountData
  val revCountActor: ActorRef = system.actorOf(Props(new RevisionCountRegistryActor(tagStats, TestData.metadata)), "revCountActor")

  val routes: Route = revisionCountRoutes(revCountActor)

  "RevisionCount: tag-states route" should {
    "successfully return JSON with OK status code on valid request" in {
      // %5B%5D = []
      val request = HttpRequest(uri = "/tag-states?sorting=LivePercent.Ascending&first-index=0&n=10&search-tags=%5B%5D")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)

        val jsonString = entityAs[String]
        entityAs[String].length > 20 should be (true)

        val o1 = I.fromJson(entityAs[String])
        o1.entryList.length should equal (10)
      }
    }

    "fail if missing a parameter" in {
      val request = HttpRequest(uri = "/tag-states?sort-by=LivePercent.Ascending&n=10")
      request ~> routes ~> check { handled should be (false) }
    }

    "fail on non-existing route" in {
      val request = HttpRequest(uri = "/tag-states/fail-me")
      request ~> routes ~> check { handled should be (false) }
    }
  }
}
