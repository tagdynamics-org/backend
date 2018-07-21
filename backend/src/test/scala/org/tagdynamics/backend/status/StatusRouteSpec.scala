package org.tagdynamics.backend.status

import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class StatusRouteSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val route: Route = StatusRoute.route

  "Status route" should {
    "return OK" in {
      val request = HttpRequest(uri = "/status")

      request ~> route ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should be ("""{"status":"OK"}""")
      }
    }
  }

}
