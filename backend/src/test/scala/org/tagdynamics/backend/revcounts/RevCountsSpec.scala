package org.tagdynamics.backend.revcounts

import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.{DayStamp, ElementState, Visible}
import org.tagdynamics.backend.TestData
import org.tagdynamics.backend.revcounts.SortHelper.SortSpec

class RevCountsSpec extends WordSpec with Matchers {

  "Count loaders" should {
    "load test files" in {
      val testKey: ElementState = Visible(List("0:v3", "2:v4"))
      val expectedStats = Some(TagStats(
        TotalCount(75,39),
        Some(LiveCount(19,25,25.333334f)),
        DayStamp.from("100402"),
        DayStamp.from("150316")
      ))
      TestData.liveTotalRevcountData.toMap.get(testKey) should be (expectedStats)
      TestData.statesTotal should be (167)
      TestData.statesLive should be (79)
    }

    "livePercent:s should be in range 0..100%" in {
      // livePercentages are only defined for ElementStates that have some elements currently live
      val livePercentages: Seq[Float] = for {
        (_, stats) <- TestData.liveTotalRevcountData
        liveStats <- stats.live
      } yield liveStats.livePercent

      for (p <- livePercentages) {
        (p >= 0) should be (true)
        (p <= 100) should be (true)
      }
    }
  }

  "SortCriteria parser" should {
    "return None for invalid input" in {
      SortHelper.parse("LiveCounts.AscendingX").isDefined should be (false)
      SortHelper.parse("LiveCountsX.Ascending").isDefined should be (false)
      SortHelper.parse("LiveCountsAscending").isDefined should be (false)
    }

    "produce one correct output" in {
      val out = SortHelper.parse("LiveCounts.Ascending")
      out.get should be ((SortHelper.SortBy.LiveCounts, SortHelper.SortOrder.Ascending))
    }
  }

  val allSorts: Map[SortSpec, Seq[(ElementState, TagStats)]] = SortHelper.sortByAll(TestData.liveTotalRevcountData)

  "All pre-sorted revcount lists" should {

    def isInc[A](xs: Seq[A], f: A => Double) = {
      xs.sliding(2).foreach(ys => {
        assert(f(ys.head) <= f(ys.last))
      })
      assert(f(xs.head) < f(xs.last))
    }

    def isDec[A](xs: Seq[A], f: A => Double) = isInc(xs, (a: A) => -f(a))

    "satisfy properties for (LiveCounts, Ascending)" in {
      val res = allSorts(SortHelper.parse("LiveCounts.Ascending").get)
      res.length should be (TestData.statesLive)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.counts)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.rank) // rank 1 = most entries
    }

    "satisfy properties for (LiveCounts, Descending)" in {
      val res = allSorts(SortHelper.parse("LiveCounts.Descending").get)
      res.length should be (TestData.statesLive)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.counts)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.rank)
    }

    "satisfy properties for (PercentLive, Ascending)" in {
      val res = allSorts(SortHelper.parse("LivePercent.Ascending").get)
      res.length should be (TestData.statesLive)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.livePercent)
    }

    "satisfy properties for (PercentLive, Descending)" in {
      val res = allSorts(SortHelper.parse("LivePercent.Descending").get)
      res.length should be (TestData.statesLive)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.livePercent)
    }

    "satisfy properties for (TotalCounts, Ascending)" in {
      val res = allSorts(SortHelper.parse("TotalCounts.Ascending").get)
      res.length should be (TestData.statesTotal)
      isInc(res, (x: (ElementState, TagStats)) => x._2.total.counts)
      isDec(res, (x: (ElementState, TagStats)) => x._2.total.rank)
    }

    "satisfy properties for (TotalCounts, Descending)" in {
      val res = allSorts(SortHelper.parse("TotalCounts.Descending").get)
      isDec(res, (x: (ElementState, TagStats)) => x._2.total.counts)
      isInc(res, (x: (ElementState, TagStats)) => x._2.total.rank)
    }
  }
}
