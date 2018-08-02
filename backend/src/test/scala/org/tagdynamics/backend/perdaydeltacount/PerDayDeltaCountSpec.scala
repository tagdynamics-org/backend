package org.tagdynamics.backend.perdaydeltacount

import org.scalatest.{Matchers, WordSpec}
import org.tagdynamics.backend.TestData

class PerDayDeltaCountSpec extends WordSpec with Matchers {
  "Delta-Per-Day count loader" should {
    "load correct number of entries with delta counts" in {
      TestData.deltaCounts.length should be (TestData.statesTotal)
    }
  }
}
