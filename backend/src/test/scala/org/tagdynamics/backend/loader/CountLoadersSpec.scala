package org.tagdynamics.backend.loader

import java.net.URI

import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.Visible

class CountLoadersSpec extends WordSpec with Matchers {

  "Count loaders" should {
    "load test files" in {
      val testDatadirectory: URI = getClass.getResource("/testdata/3-aggregates/").toURI
      val loadedData = CountLoaders.loadFromDirectory(testDatadirectory)

      val testKey = Visible(List("0:v3", "2:v4"))
      val expectedStats = TagStats(TotalCount(39,75),Some(LiveCount(25,19,64.0f)))
      loadedData(testKey) should be (expectedStats)
      loadedData.keySet.size should be (167)
    }
  }

}
