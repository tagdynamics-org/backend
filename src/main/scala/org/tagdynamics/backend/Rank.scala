package org.tagdynamics.backend

object Rank {

  /**
   * Add rank to a sequence `x1, x2, ...` where `extract(x1), extract(x2), ...` is
   * non-increasing.
   *
   * Eg. List(5, 2, 2, 1) -> List((1, 5), (2, 2), (2, 2), (3, 1))
   */
  def addRank[A](xs: Seq[A], extract: A => Int): Seq[(Int, A)] = {
    if (xs.isEmpty) {
      throw new Exception("Cannot add rank to empty list")
    } else if (xs.length == 1) {
      Seq((1, xs.head))
    } else {
      xs.sliding(2).scanLeft((1, xs.head)) {
        case ((prevRank, _), List(a1, a2)) => {
          val (v1, v2) = (extract(a1), extract(a2))

          if (v1 < v2) throw new Exception("Input sequences increases")
          else if (v1 == v2) (prevRank, a2)
          else (prevRank + 1, a2)
        }
      }.toSeq
    }
  }

}
