package org.tagdynamics.backend.transitions

import java.net.URI

import org.tagdynamics.aggregator.common
import org.tagdynamics.aggregator.common.{Counted, ElementState, JSONCustomProtocols, Transition}
import org.tagdynamics.backend.revcounts.TagStats

// Loaded data will be of type Map[ElementState, ToFromStats].
// Below `base` refers to the key in the above map.
case class ToFromStats(state: ElementState,
                       tagStats: TagStats, // stats for `state`
                       from: Int,          // transitions from base -> state
                       to: Int             // transitions from state -> base
                      )

object TransitionLoader extends JSONCustomProtocols {

  /** Load a JSONL file transitions */
  def load(filename: URI): Seq[Counted[Transition[ElementState]]] = {
    import spray.json._

    println(s" Loading transition data from $filename")
    val result: Seq[Counted[Transition[ElementState]]] =
      common.Utils.loadJSONL(filename,
        (line: String) => line.parseJson.convertTo[Counted[Transition[ElementState]]])
        .sortBy(x => -x.n)
    println(s"    - Loaded ${result.length} tag counts")
    result
  }

  def process(xs: Seq[Counted[Transition[ElementState]]],
             statsMap: Map[ElementState, TagStats]): Map[ElementState, Seq[ToFromStats]] = {

    // List all element states (once) for which to collect to/from stats.
    // Since we only include transitions that have occurred for at least 5 unique map elements,
    // the below states should also have totalCount >= 5.
    val toFromStates: Seq[ElementState] = (xs.map(_.key.from) ++ xs.map(_.key.to)).distinct

    // for a state `x` give map (a1 -> count1, b2 -> count2, ..) giving counts for transitions
    // x -> a1 (count1), x -> a2 (count2), ...
    val fromCounts: Map[ElementState, Map[ElementState, Int]] =
      xs.groupBy(x => x.key.from)
        .mapValues(ys => ys.map(z => (z.key.to, z.n)).toMap)

    // for a state `x` give map (a1 -> count1, b2 -> count2, ..) giving counts for transitions
    // a1 -> x (count1), a2 -> x (count2), ...
    val toCounts: Map[ElementState, Map[ElementState, Int]] =
      xs.groupBy(x => x.key.to)
        .mapValues(ys => ys.map(z => (z.key.from, z.n)).toMap)

    def tableFor(x: ElementState): Seq[ToFromStats] = {
      val statesWithTransitionsToFromX: Seq[ElementState] =
        (fromCounts.getOrElse(x, Map()).keySet ++ toCounts.getOrElse(x, Map()).keySet).toSeq

      val fromXCounts: Map[ElementState, Int] = fromCounts.getOrElse(x, Map())
      val toXCounts: Map[ElementState, Int] = toCounts.getOrElse(x, Map())

      for {
        // TODO: For now skip transitions to/from NotCreated state.
        // What would reasonable statistics for this be? Or can we leave them out?
        // Tried passing null to `tagStats`, but this seems to crash the serializer
        t0 <- statesWithTransitionsToFromX if !ElementState.isNotCreated(t0)
      } yield ToFromStats(t0,
        tagStats = statsMap(t0),
        from = fromXCounts.getOrElse(t0, 0),
        to = toXCounts.getOrElse(t0, 0)
      )
    }
    toFromStates.map(x => (x, tableFor(x))).toMap
  }
}
