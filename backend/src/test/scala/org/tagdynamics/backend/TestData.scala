package org.tagdynamics.backend

import java.net.URI

import org.tagdynamics.aggregator.common.{Counted, DeltasByDay, ElementState, Transition}
import org.tagdynamics.backend.revcounts.{CountLoaders, TagStats}

object TestData {
  val data = {
    val testDatadirectory: URI = getClass.getResource("/testdata/3-aggregates/").toURI
    new AggregatedDataSources(testDatadirectory)
  }

  val liveTotalRevcountData: Seq[(ElementState, TagStats)] = CountLoaders.computeStats(data)

  val metadata = SourceMetadata(
    downloaded = "2018.1.1 (UTC)",
    md5 = "md5 checksum",
    selectedTags = List("amenity", "barrier", "man_made")
  )

  val statesLive: Int = TestData.liveTotalRevcountData.count(x => x._2.live.isDefined)
  val statesTotal: Int = TestData.liveTotalRevcountData.length

  val loadedTransitionData: Seq[Counted[Transition[ElementState]]] = data.transitionCounts
  val transitionsTotal: Int = loadedTransitionData.length

  val deltaCounts: Seq[DeltasByDay[ElementState]] = data.deltaCounts

}
