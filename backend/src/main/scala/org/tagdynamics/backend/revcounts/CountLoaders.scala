package org.tagdynamics.backend.revcounts

import java.net.URI

import org.tagdynamics.aggregator.common.{DayStamp, DeltasByDay}
import org.tagdynamics.backend.AggregatedDataSources

// import common so we can reference common.Util without colliding with backend's own Util:s
import org.tagdynamics.aggregator.common
import org.tagdynamics.aggregator.common.{Counted, ElementState, JSONCustomProtocols}
import org.tagdynamics.backend.Utils

//
// Tag statistics assigned to an element state
//
// TODO: Could this type of data merging should be done earlier in the pipeline. Not in the HTTP server.
// TODO: add:
//    - out/in-flux
//    - growth of live entries (for 1/3/6/12 months)
//    - date of first/last entry
//
case class TotalCount(counts: Int, rank: Int)
case class LiveCount(counts: Int, rank: Int, livePercent: Float)
case class TagStats(total: TotalCount, live: Option[LiveCount], firstEntry: DayStamp, lastEntry: DayStamp)

object CountLoaders extends JSONCustomProtocols {

  type Rank = Int
  type Count = Int

  def addRank(xs: Seq[Counted[ElementState]]): Map[ElementState, (Rank, Count)] = {
    val countsWithRank: Seq[(Rank, Counted[ElementState])] =
      Utils.addRank(xs.sortBy(x => -x.n), (x: Counted[ElementState]) => x.n)

    val result = countsWithRank
      .map{ case(rank, tagState) => (tagState.key, (rank, tagState.n))}
      .toMap
    println("    - Done.")
    result
  }

  def computeStats(data: AggregatedDataSources): Seq[(ElementState, TagStats)] = {

    val liveCounts: Map[ElementState, (Rank, Count)] = addRank(data.liveCounts)
    val totalCounts: Map[ElementState, (Rank, Count)] = addRank(data.totalCounts)

    println("Computing minMap ..")
    val firstEntry: Map[ElementState, DayStamp] = data.deltaCounts.map(x => (x.key, DayStamp.min(x.deltas.keySet.toSeq))).toMap
    println("Computing maxMap ..")
    val lastEntry: Map[ElementState, DayStamp] = data.deltaCounts.map(x => (x.key, DayStamp.max(x.deltas.keySet.toSeq))).toMap
    println("Creating statistics ..")

    val unsorted: Iterable[(ElementState, TagStats)] =
    for {
      (es, (totalRank, totalCount)) <- totalCounts
      rcO = liveCounts.get(es)
    } yield (es, TagStats(
      total = TotalCount(counts = totalCount, rank = totalRank),
      live = rcO.map {
        case (liveRank, liveCount) =>
          LiveCount(
            counts = liveCount,
            rank = liveRank,
            livePercent = 100f * liveCount / totalCount
          )
      },
      firstEntry = firstEntry(es),
      lastEntry = lastEntry(es)
    ))

    unsorted.toSeq
  }

}
