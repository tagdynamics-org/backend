package org.tagdynamics.backend.perdaydelta

import org.tagdynamics.aggregator.common.{DayStamp, ElementState, JSONCustomProtocols}
import org.tagdynamics.backend.AggregatedDataSources

object PerDayDeltaLoader extends JSONCustomProtocols {

  // Note: this may contain element states with <= total count
  def process(data: AggregatedDataSources): Seq[(ElementState, Map[DayStamp, Int])] = {
    data.deltaCounts.map(x => (x.key, x.deltas))
  }

}