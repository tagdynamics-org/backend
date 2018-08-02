package org.tagdynamics.backend

import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.{DayStamp, Visible}
import org.tagdynamics.backend.revcounts.RevisionCountRegistryActorMessages.ListResponse
import org.tagdynamics.backend.revcounts.{LiveCount, TagStats, TotalCount}

class JSONTests extends WordSpec with Matchers {
  "JSON serialization" should {
    "serialize/deserialize ListResponse object" in {
      object I extends JsonSupport {
        import spray.json._
        def toJson(ds: ListResponse): String = ds.toJson.toString
        def fromJson(line: String): ListResponse = line.parseJson.convertTo[ListResponse]
      }

      val v = Visible(List("0:a", "1:b", "3:c"))
      val ts = TagStats(
        total = TotalCount(123, 3),
        live = Some(LiveCount(counts = 23, rank = 12, livePercent = 20.2f)),
        firstEntry = DayStamp.from("090101"),
        lastEntry = DayStamp.from("190101")
      )

      val example = ListResponse(
        entryList = List((v, ts)),
        totalEntries = 123,
        dataSet = TestData.metadata)

      I.toJson(example).length > 30 should be (true)
      val example2 = I.fromJson(I.toJson(example))

      example should equal (example2)
    }
  }
}
