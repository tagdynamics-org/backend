package org.tagdynamics.backend.revcounts

import java.net.URI

import org.tagdynamics.aggregator.common.{Counted, ElementState, JSONCustomProtocols, Utils}
import org.tagdynamics.backend.Rank

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
case class TagStats(total: TotalCount, live: Option[LiveCount])

object CountLoaders extends JSONCustomProtocols {

  type Rank = Int
  type Count = Int

  /** Load a JSONL file with `Counted[ElementState]` lines and add rank */
  private def loadCountFile(filename: URI): Map[ElementState, (Rank, Count)] = {
    import spray.json._

    println(s" Loading tag analytics data")
    println(s" * Loading count filename $filename")
    val counts: Seq[Counted[ElementState]] =
      Utils.loadJSONL(filename, (line: String) => line.parseJson.convertTo[Counted[ElementState]])
          .sortBy(x => -x.n)
    println(s"    - Loaded ${counts.length} tag counts. Adding rank ...")

    val countsWithRank: Seq[(Rank, Counted[ElementState])] =
      Rank.addRank(counts, (x: Counted[ElementState]) => x.n)

    val result = countsWithRank
      .map{ case(rank, tagState) => (tagState.key, (tagState.n, rank))}
      .toMap
    println("    - Done.")
    result
  }


  /** Load total+live count files from a directory */
  def loadFromDirectory(dataDirectory: URI): Seq[(ElementState, TagStats)] = {

    val liveCounts: Map[ElementState, (Rank, Count)] = loadCountFile(dataDirectory.resolve("live-revcounts.jsonl"))
    val totalCounts: Map[ElementState, (Rank, Count)] = loadCountFile(dataDirectory.resolve("total-revcounts.jsonl"))

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
            livePercent = Math.round((100d * liveCount) / totalCount)
          )
      })
    )

    unsorted.toSeq
  }

}
