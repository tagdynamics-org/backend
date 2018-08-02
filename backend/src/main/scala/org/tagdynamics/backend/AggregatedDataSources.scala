package org.tagdynamics.backend

import java.net.URI

import org.tagdynamics.aggregator.common
import org.tagdynamics.aggregator.common.{Counted, DeltasByDay, ElementState, JSONCustomProtocols, Transition}

class AggregatedDataSources(dataDirectory: URI) extends JSONCustomProtocols {

  private def loadCountFile(filename: URI): Seq[Counted[ElementState]] = {
    import spray.json._
    println(s"loadCountFile $filename")
    common.Utils.loadJSONL(filename, (line: String) => line.parseJson.convertTo[Counted[ElementState]])
      .sortBy(x => -x.n)
  }

  val liveCounts: Seq[Counted[ElementState]] = loadCountFile(dataDirectory.resolve("live-revcounts.jsonl"))
  val totalCounts: Seq[Counted[ElementState]] = loadCountFile(dataDirectory.resolve("total-revcounts.jsonl"))

  private def loadTransactionFile(filename: URI): Seq[Counted[Transition[ElementState]]] = {
    import spray.json._
    common.Utils.loadJSONL(filename,
      (line: String) => line.parseJson.convertTo[Counted[Transition[ElementState]]])
      .sortBy(x => -x.n)
  }

  val transitionCounts: Seq[Counted[Transition[ElementState]]] =
    loadTransactionFile(dataDirectory.resolve("transition-counts.jsonl"))


  private def loadHistory(filename: URI): Seq[DeltasByDay[ElementState]] = {
    import spray.json._
    common.Utils.loadJSONL(filename, (line: String) => line.parseJson.convertTo[DeltasByDay[ElementState]])
  }

  val deltaCounts: Seq[DeltasByDay[ElementState]] = {
    // Do not load delta:s for element states that are below cut-off (5 unique entries by total count)
    val validStates = totalCounts.map(_.key).toSet
    loadHistory(dataDirectory.resolve("per-day-delta-counts.jsonl"))
      .filter(x => validStates.contains(x.key))
  }
}
