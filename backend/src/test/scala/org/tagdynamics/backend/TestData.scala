package org.tagdynamics.backend

import java.net.URI

import org.tagdynamics.aggregator.common.{Counted, ElementState, Transition}
import org.tagdynamics.backend.revcounts.{CountLoaders, TagStats}
import org.tagdynamics.backend.transitions.TransitionLoader

object TestData {
  val liveTotalRevcountData: Seq[(ElementState, TagStats)] = {
    val testDatadirectory: URI = getClass.getResource("/testdata/3-aggregates/").toURI
    CountLoaders.loadFromDirectory(testDatadirectory)
  }

  val metadata = SourceMetadata(
    downloaded = "2018.1.1 (UTC)",
    md5 = "md5 checksum",
    selectedTags = List("amenity", "barrier", "manmade")
  )

  val statesLive: Int = TestData.liveTotalRevcountData.count(x => x._2.live.isDefined)
  val statesTotal: Int = TestData.liveTotalRevcountData.length

  val loadedTransitionData: Seq[Counted[Transition[ElementState]]] = {
    val testDatadirectory: URI = getClass.getResource("/testdata/3-aggregates/transition-counts.jsonl").toURI
    TransitionLoader.load(testDatadirectory)
  }

  val transitionsTotal: Int = loadedTransitionData.length
}
