package org.tagdynamics.backend

import org.scalatest.{Matchers, WordSpec}

import scala.util.Try

class UtilsSpec extends WordSpec with Matchers {

  "Add rank to a list" should {
    "work for decreasing sequence I" in {
      Utils.addRank(List(5, 4, 3, 2, 1), (x: Int) => x) should
        be (List((1, 5), (2, 4), (3, 3), (4, 2), (5, 1)))
    }

    "work for decreasing sequence (duplicates at start)" in {
      Utils.addRank(List(13, 13, 12, 11, 10), (x: Int) => x) should
        be (List((1, 13), (1, 13), (2, 12), (3, 11), (4, 10)))
    }

    "work for decreasing sequence (duplicates in middle)" in {
      Utils.addRank(List(13, 12, 12, 11, 10), (x: Int) => x) should
        be (List((1, 13), (2, 12), (2, 12), (3, 11), (4, 10)))
    }

    "work for decreasing sequence (duplicates at end)" in {
      Utils.addRank(List(13, 12, 11, 10, 10), (x: Int) => x) should
        be (List((1, 13), (2, 12), (3, 11), (4, 10), (4, 10)))
    }

    "should not increase rank if extract-function is constant" in {
      Utils.addRank(List(5, 4, 3, 2, 1), (_: Int) => 0) should
        be (List((1, 5), (1, 4), (1, 3), (1, 2), (1, 1)))
    }

    "should not crash for increasing sequence if extract-function is constant" in {
      Utils.addRank(List(1, 2, 3, 4, 5), (_: Int) => 0) should
        be (List((1, 1), (1, 2), (1, 3), (1, 4), (1, 5)))
    }

    "work with repeated entries" in {
      Utils.addRank(List(7, 7, 7, 3, 3), (x: Int) => x) should
        be (List((1, 7), (1, 7), (1, 7), (2, 3), (2, 3)))
    }

    def repeat(n: Int, value: Int): List[Int] = (1 to n).map(_ => value).toList

    "work when all entries are the same" in {
      for (n <- 1 to 100) {
        Utils.addRank(repeat(n, n), (x: Int) => x) should
          be (repeat(n, n).map(v => (1, v)))
      }
    }

    "work for list of length one" in {
      Utils.addRank(List(2), (x: Int) => x) should be (List((1, 2)))
    }

    "abort if entries are increasing or if input is an empty list" in {
      val shouldFailLists = List(
        List[Int](),
        List[Int](1, 2),
        List[Int](3, 3, 5),
        List[Int](1, 3, 3, 4)
      )

      for (shouldFail <- shouldFailLists) {
        Try { Utils.addRank(shouldFail, (x: Int) => x) }
          .map(_.toList) // getting the first entry will always succeed except for empty input
          .isFailure should be (true)
      }
    }

  }

}
