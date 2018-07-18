package org.tagdynamics.backend.revcounts

import java.net.URI

import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.{ElementState, Visible}
import org.tagdynamics.backend.revcounts.SortHelper.SortSpec

class RevCountsSpec extends WordSpec with Matchers {

  val testDatadirectory: URI = getClass.getResource("/testdata/3-aggregates/").toURI
  val loadedData: Seq[(ElementState, TagStats)] = CountLoaders.loadFromDirectory(testDatadirectory)

  val statesLive: Int = loadedData.count(x => x._2.live.isDefined)
  val statesTotal: Int = loadedData.length

  "Count loaders" should {
    "load test files" in {
      val testKey: ElementState = Visible(List("0:v3", "2:v4"))
      val expectedStats = Some(TagStats(TotalCount(39,75),Some(LiveCount(25,19,64.0f))))
      loadedData.toMap.get(testKey) should be (expectedStats)
      statesTotal should be (167)
      statesLive should be (79)
    }
  }

  val allSorts: Map[SortSpec, Seq[(ElementState, TagStats)]] = SortHelper.sortByAll(loadedData)

  "All pre-sorted revcount lists" should {

    def isInc[A](xs: Seq[A], f: A => Double) = {
      xs.sliding(2).foreach(ys => {
        assert(f(ys.head) <= f(ys.last))
      })
    }

    def isDec[A](xs: Seq[A], f: A => Double) = isInc(xs, (a: A) => -f(a))

    "satisfy properties for (LiveCounts, Ascending)" in {
      val res = allSorts((SortHelper.SortBy.LiveCounts, SortHelper.SortOrder.Ascending))
      res.length should be (statesLive)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.counts)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.rank) // rank 1 = most entries
    }

    "satisfy properties for (LiveCounts, Descending)" in {
      val res = allSorts((SortHelper.SortBy.LiveCounts, SortHelper.SortOrder.Descending))
      res.length should be (statesLive)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.counts)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.rank)
    }

    "satisfy properties for (PercentLive, Ascending)" in {
      val res = allSorts((SortHelper.SortBy.PercentLive, SortHelper.SortOrder.Ascending))
      res.length should be (statesLive)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.livePercent)
    }

    "satisfy properties for (PercentLive, Descending)" in {
      val res = allSorts((SortHelper.SortBy.PercentLive, SortHelper.SortOrder.Descending))
      res.length should be (statesLive)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.livePercent)
    }

    "satisfy properties for (TotalCounts, Ascending)" in {
      val res = allSorts((SortHelper.SortBy.TotalCounts, SortHelper.SortOrder.Ascending))
      res.length should be (statesTotal)
      isInc(res, (x: (ElementState, TagStats)) => x._2.total.counts)
      isDec(res, (x: (ElementState, TagStats)) => x._2.total.rank)
    }

    "satisfy properties for (TotalCounts, Descending)" in {
      val res = allSorts((SortHelper.SortBy.TotalCounts, SortHelper.SortOrder.Descending))
      isDec(res, (x: (ElementState, TagStats)) => x._2.total.counts)
      isInc(res, (x: (ElementState, TagStats)) => x._2.total.rank)
    }
  }

}
