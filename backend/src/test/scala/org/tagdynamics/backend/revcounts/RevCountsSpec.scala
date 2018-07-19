package org.tagdynamics.backend.revcounts

import java.net.URI

import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.aggregator.common.{ElementState, Visible}
import org.tagdynamics.backend.SourceMetadata
import org.tagdynamics.backend.revcounts.SortHelper.SortSpec

object TestData {
  private val testDatadirectory: URI = getClass.getResource("/testdata/3-aggregates/").toURI
  val loadedData: Seq[(ElementState, TagStats)] = CountLoaders.loadFromDirectory(testDatadirectory)

  val metadata = SourceMetadata(
    downloaded = "2018.1.1 (UTC)",
    md5 = "md5 checksum",
    selectedTags = List("amenity", "barrier", "manmade")
  )
}

class RevCountsSpec extends WordSpec with Matchers {

  val statesLive: Int = TestData.loadedData.count(x => x._2.live.isDefined)
  val statesTotal: Int = TestData.loadedData.length

  "Count loaders" should {
    "load test files" in {
      val testKey: ElementState = Visible(List("0:v3", "2:v4"))
      val expectedStats = Some(TagStats(TotalCount(39,75),Some(LiveCount(25,19,64.0f))))
      TestData.loadedData.toMap.get(testKey) should be (expectedStats)
      statesTotal should be (167)
      statesLive should be (79)
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

  val allSorts: Map[SortSpec, Seq[(ElementState, TagStats)]] = SortHelper.sortByAll(TestData.loadedData)

  "All pre-sorted revcount lists" should {

    def isInc[A](xs: Seq[A], f: A => Double) = {
      xs.sliding(2).foreach(ys => {
        assert(f(ys.head) <= f(ys.last))
      })
    }

    def isDec[A](xs: Seq[A], f: A => Double) = isInc(xs, (a: A) => -f(a))

    "satisfy properties for (LiveCounts, Ascending)" in {
      val res = allSorts(SortHelper.parse("LiveCounts.Ascending").get)
      res.length should be (statesLive)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.counts)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.rank) // rank 1 = most entries
    }

    "satisfy properties for (LiveCounts, Descending)" in {
      val res = allSorts(SortHelper.parse("LiveCounts.Descending").get)
      res.length should be (statesLive)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.counts)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.rank)
    }

    "satisfy properties for (PercentLive, Ascending)" in {
      val res = allSorts(SortHelper.parse("PercentLive.Ascending").get)
      res.length should be (statesLive)
      isInc(res, (x: (ElementState, TagStats)) => x._2.live.get.livePercent)
    }

    "satisfy properties for (PercentLive, Descending)" in {
      val res = allSorts(SortHelper.parse("PercentLive.Descending").get)
      res.length should be (statesLive)
      isDec(res, (x: (ElementState, TagStats)) => x._2.live.get.livePercent)
    }

    "satisfy properties for (TotalCounts, Ascending)" in {
      val res = allSorts(SortHelper.parse("TotalCounts.Ascending").get)
      res.length should be (statesTotal)
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
