package org.tagdynamics.backend.revcounts

import org.tagdynamics.aggregator.common.ElementState

object SortHelper {

  case object SortBy {
    sealed trait Alternatives
    case object LiveCounts extends Alternatives // same as LiveRank (but in reverse order)
    case object TotalCounts extends Alternatives // same as TotalRank (but in reverse order)
    case object PercentLive extends Alternatives
  }

  case object SortOrder {
    sealed trait Alternatives
    case object Ascending extends Alternatives
    case object Descending extends Alternatives
  }

  type SortSpec = (SortBy.Alternatives, SortOrder.Alternatives)

  def sort(inData: Seq[(ElementState, TagStats)], s: SortSpec): Seq[(ElementState, TagStats)] = {
    val sortBy: SortBy.Alternatives = s._1
    val sortOrder: SortOrder.Alternatives = s._2

    val multiplier: Int = sortOrder match {
      case SortOrder.Ascending => 1
      case SortOrder.Descending => -1
    }

    def f(t: TagStats): Option[Double] =
      sortBy match {
        case SortBy.PercentLive => t.live.map(x => x.livePercent)
        case SortBy.TotalCounts => Some(t.total.counts)
        case SortBy.LiveCounts => t.live.map(x => x.counts)
      }

    val table: Seq[(ElementState, TagStats, Double)] =
      for {
        (elementState, tagStats) <- inData
        pos <- f(tagStats)
      } yield (elementState, tagStats, multiplier * pos)

    table.sortBy { case (_, _, pos) => pos }
      .map { case (es, tagStats, _) => (es, tagStats) }
  }

  def sortByAll(inData: Seq[(ElementState, TagStats)]): Map[SortSpec, Seq[(ElementState, TagStats)]] =
    (for {
      sortBy <- Seq(
        SortBy.LiveCounts,
        SortBy.TotalCounts,
        SortBy.PercentLive)
      sortOrder <- Seq(
        SortOrder.Ascending,
        SortOrder.Descending,
      )
      s: SortSpec = (sortBy, sortOrder)
    } yield (s, sort(inData, s))).toMap

}