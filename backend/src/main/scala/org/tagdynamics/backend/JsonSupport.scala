package org.tagdynamics.backend

import org.tagdynamics.aggregator.common.JSONCustomProtocols
import org.tagdynamics.backend.revcounts.RevisionCountRegistryActorMessages.ListResponse
import org.tagdynamics.backend.revcounts.{LiveCount, TagStats, TotalCount}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.tagdynamics.backend.status.StatusResponse
import org.tagdynamics.backend.transitions.{ToFromStats, TransitionsMessages}

trait JsonSupport extends SprayJsonSupport with JSONCustomProtocols {
  implicit val jx1 = jsonFormat3(SourceMetadata)
  implicit val jx2 = jsonFormat2(TotalCount)
  implicit val jx3 = jsonFormat3(LiveCount)
  implicit val jx4 = jsonFormat4(TagStats)
  implicit val jx5 = jsonFormat3(ListResponse)
  implicit val jx6 = jsonFormat1(StatusResponse)
  implicit val jx7 = jsonFormat4(ToFromStats)
  implicit val jx8 = jsonFormat2(TransitionsMessages.Response)
}
