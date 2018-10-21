package org.tagdynamics.backend.perdaydeltacount

import akka.actor.Props
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.actor.ActorRef
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.ElementState
import org.tagdynamics.backend.TestData
import org.tagdynamics.backend.perdaydelta.{PerDayDeltaLoader, PerDayDeltaRegistryActor, PerDayDeltaRoute}
import org.tagdynamics.backend.revcounts.{CountLoaders, TagStats}

class RouteSpec extends WordSpec with PerDayDeltaRoute with Matchers with ScalaFutures with ScalatestRouteTest {

  val route: Route = {
    val deltas = PerDayDeltaLoader.process(TestData.data)
    val tagStats: Seq[(ElementState, TagStats)] = CountLoaders.computeStats(TestData.data)
    val sourceMetadata = TestData.metadata
    val deltaActor: ActorRef = system.actorOf(Props(new PerDayDeltaRegistryActor(deltas.toMap, tagStats.toMap, sourceMetadata)), "perdaydeltaActor")
    perDayDeltaRoute(deltaActor, sourceMetadata.selectedTags)
  }

  "per-day-delta route" should {
    "successfully return JSON with OK status code on valid request" in {
      val request = HttpRequest(uri = "/per-day-deltas?amenity=v1")

      request ~> route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)

        val jsonString = entityAs[String]
        entityAs[String].length > 20 should be (true)
      }
    }
  }

}
