package org.tagdynamics.backend.revcounts

import org.tagdynamics.aggregator.common.ElementState

object SortHelper {

  case object SortBy {
    sealed trait Alternatives
    case object LiveCounts extends Alternatives // same as LiveRank (but in reverse order)
    case object TotalCounts extends Alternatives // same as TotalRank (but in reverse order)
    case object LivePercent extends Alternatives
    case object FirstEdit extends Alternatives
    case object LastEdit extends Alternatives
  }

  case object SortOrder {
    sealed trait Alternatives
    case object Ascending extends Alternatives
    case object Descending extends Alternatives
  }

  type SortSpec = (SortBy.Alternatives, SortOrder.Alternatives)

  val allSortCriteria: Seq[SortSpec] = for {
    sortBy <- Seq(
      SortBy.LiveCounts,
      SortBy.TotalCounts,
      SortBy.LivePercent,
      SortBy.FirstEdit,
      SortBy.LastEdit)
    sortOrder <- Seq(
      SortOrder.Ascending,
      SortOrder.Descending)
  } yield (sortBy, sortOrder)

  // parse eg. "LiveCounts.Ascending"
  def parse(sortCriteria: String): Option[SortSpec] = {
    val split = sortCriteria.split('.')

    if (split.length == 2) {
      val sortByO = split(0) match {
        case "LiveCounts" => Some(SortBy.LiveCounts)
        case "TotalCounts" => Some(SortBy.TotalCounts)
        case "LivePercent" => Some(SortBy.LivePercent)
        case "FirstEdit" => Some(SortBy.FirstEdit)
        case "LastEdit" => Some(SortBy.LastEdit)
        case _ => None
      }
      val sortOrderO = split(1) match {
        case "Ascending" => Some(SortOrder.Ascending)
        case "Descending" => Some(SortOrder.Descending)
        case _ => None
      }
      val pair = (sortByO, sortOrderO)

      pair match {
        case (Some(sortBy), Some(sortOrder)) => Some((sortBy, sortOrder))
        case _ => None
      }
    } else {
      None
    }
  }

  def sort(inData: Seq[(ElementState, TagStats)], s: SortSpec): Seq[(ElementState, TagStats)] = {
    val sortBy: SortBy.Alternatives = s._1
    val sortOrder: SortOrder.Alternatives = s._2

    val multiplier: Int = sortOrder match {
      case SortOrder.Ascending => 1
      case SortOrder.Descending => -1
    }

    def f(t: TagStats): Option[Double] =
      sortBy match {
        case SortBy.LivePercent => t.live.map(x => x.livePercent)
        case SortBy.TotalCounts => Some(t.total.counts)
        case SortBy.LiveCounts => t.live.map(x => x.counts)
        case SortBy.FirstEdit => Some(t.firstEdit.yymmdd)
        case SortBy.LastEdit => Some(t.lastEdit.yymmdd)
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
    (for { s <- allSortCriteria } yield (s, sort(inData, s))).toMap

}