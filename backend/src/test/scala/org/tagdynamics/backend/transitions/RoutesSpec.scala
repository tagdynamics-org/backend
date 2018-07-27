package org.tagdynamics.backend.transitions

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

class RoutesSpec extends WordSpec with TransitionCountRoutes with Matchers with ScalaFutures with ScalatestRouteTest {

  object I extends JSONCustomProtocols {
    import spray.json._
    def toJson(ds: ListResponse): String = ds.toJson.toString
    def fromJson(line: String): ListResponse = line.parseJson.convertTo[ListResponse]
  }

  val routes: Route = {
    val ltData = TestData.liveTotalRevcountData
    val transitionData = TestData.loadedTransitionData
    val transitions = TransitionLoader.process(transitionData, ltData.toMap)

    val testActor: ActorRef = system.actorOf(Props(new TransitionRegistryActor(transitions, TestData.metadata)), "revCountActor")

    transitionRoutes(testActor, TestData.metadata.selectedTags)
  }

  "transition route" should {
    "successfully return JSON with OK status code on valid request" in {
      val request = HttpRequest(uri = "/transitions?amenity=v1")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)

        val jsonString = entityAs[String]
        entityAs[String].length > 20 should be (true)
      }
    }

    "fail if missing parameters" in {
      val request = HttpRequest(uri = "/transitions")
      request ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }

    "fail on non-existing query" in {
      val request = HttpRequest(uri = "/transitions?mystery_tag=something")
      request ~> routes ~> check {
        status should ===(StatusCodes.NotFound)
      }
    }
  }

}
