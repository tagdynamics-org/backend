package org.tagdynamics.backend.transitions

import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.{Counted, DayStamp, Deleted, ElementState, NotCreated, Transition, Visible}
import org.tagdynamics.backend.TestData
import org.tagdynamics.backend.revcounts.{TagStats, TotalCount}

class TransitionsSpec extends WordSpec with Matchers {
  "Transitions loader" should {
    "load correct number of lines" in {
      TestData.transitionsTotal should be(644)
    }
  }

  "Transitions parser" should {
    "handle simple example correctly" in {
      val es1 = Visible(List("0:tag1", "1:tag", "a:tagged"))
      val es2 = Visible(List("0:tag1"))
      val del = Deleted
      val nc = NotCreated

      val t_es1_del = Transition[ElementState](from = es1, to = del)
      val t_es1_es2 = Transition[ElementState](from = es1, to = es2)
      val t_es2_es1 = Transition[ElementState](from = es2, to = es1)
      val t_nc_es1 = Transition[ElementState](from = nc, to = es1)
      val t_nc_es2 = Transition[ElementState](from = nc, to = es2)

      val data: Seq[Counted[Transition[ElementState]]] = Seq(
        Counted(t_es1_del, 23),
        Counted(t_es1_es2, 201),
        Counted(t_es2_es1, 29),
        Counted(t_nc_es1, 41),
        Counted(t_nc_es2, 99)
      )

      val d1 = DayStamp.from("170102")
      val d2 = DayStamp.from("170104")

      val statsMap = Map[ElementState, TagStats](
        es1 -> TagStats(total = TotalCount(counts = 111, rank = 9), live = None, firstEntry = d1, lastEntry = d2),
        es2 -> TagStats(total = TotalCount(counts = 112, rank = 8), live = None, firstEntry = d1, lastEntry = d2),
        del -> TagStats(total = TotalCount(counts = 113, rank = 7), live = None, firstEntry = d1, lastEntry = d2),
        nc -> TagStats(total = TotalCount(counts = 114, rank = 6), live = None, firstEntry = d1, lastEntry = d2)
      )

      val result: Map[ElementState, Seq[ToFromStats]] = TransitionLoader.process(data, statsMap)

      result.keySet should be (Set[ElementState](es1, es2, del, nc))

      result(es1).toSet should be (Set(
        ToFromStats(del, statsMap(del), 23, 0),
        ToFromStats(es2, statsMap(es2), 201, 29)
        //ToFromStats(nc, statsMap(nc), 0, 41) // for now skipping NC
      ))

      result(es2).toSet should be (Set(
        ToFromStats(es1, statsMap(es1), 29, 201)
        //ToFromStats(nc, statsMap(nc), 0, 99) // for now skipping NC
      ))

      result(del).toSet should be (Set(
        ToFromStats(es1, statsMap(es1), 0, 23)
      ))

      result(nc).toSet should be (Set(
        ToFromStats(es1, statsMap(es1), 41, 0),
        ToFromStats(es2, statsMap(es2), 99, 0)
      ))
    }
  }
}
