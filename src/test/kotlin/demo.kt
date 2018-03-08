package com.cashalgo.demo

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on


object PriceAlertSpec: Spek({
    given("price alert") {
        val pa = PriceAlert()
        beforeEachTest {
            pa.clearPriceAlert()
        }
        on("check empty price alert") {
            val cpa = pa.checkPriceAlert("00001", 100.0)
            it("should return empty id list because of no price alert added") {
                assertEquals(emptyList<Int>(),cpa)
            }
        }
        on("remove and check price alert") {
            pa.addPriceAlert("a", "00001", 100.0, "-1")
            val cpa_1 = pa.checkPriceAlert("00001", 96.0)
            it("should return listOf(1) because of price alert level triggered") {
                assertEquals(listOf(1), cpa_1)
            }
            pa.removePriceAlert("a", "00002")
            pa.removePriceAlert("a", "00001")
            val cpa_2 = pa.checkPriceAlert("00001", 96.0)
            it("should return empty id list because of price alert was removed") {
                assertEquals(emptyList<Int>(), cpa_2)
            }
        }
        on("check price alert") {
            pa.addPriceAlert("b", "00002", 50.0, "-1")
            pa.addPriceAlert("c", "00003", 200.0, "1")
            val cpa = pa.checkPriceAlert("00002", 45.0)
            it("should return listOf(1) because of price alert level triggered") {
                assertEquals(listOf(1), cpa)
            }
        }
        on("check same direction price alert, different direction price alert, equal value price alert and different users with same price alert") {
            pa.addPriceAlert("a", "00001", 100.0, "1")
            pa.addPriceAlert("a", "00001", 105.0, "1")
            pa.addPriceAlert("b", "00001", 90.0, "-1")
            pa.addPriceAlert("c", "00002", 50.0, "1")
            pa.addPriceAlert("d", "00003", 200.0, "-1")
            pa.addPriceAlert("e", "00003", 180.0, "1")
            pa.addPriceAlert("f", "00003", 200.0, "-1")
            val cpa_1 = pa.checkPriceAlert("00001", 107.0)
            it("should return id list [1,2] because price alert on the same direction") {
                assertEquals(listOf(1,2), cpa_1)
            }
            val cpa_2 = pa.checkPriceAlert("00001", 93.0)
            it ("should return empty id list because of price alert not exit") {
                assertEquals(emptyList<Int>(), cpa_2)
            }
            pa.removePriceAlert("a", "00001")
            val cpa_3 = pa.checkPriceAlert("00001", 107.0)
            it ("should return empty id list because of price alert was removed") {
                assertEquals(emptyList<Int>(), cpa_3)
            }
            val cpa_4 = pa.checkPriceAlert("00003", 190.0)
            it ("should return id list[5,7,6] because price alert exist and edge triggered limitation") {
                assertEquals(listOf(5,7,6), cpa_4)
            }
            val cpa_5 = pa.checkPriceAlert("00003", 200.0)
            it ("should return empty id list because of edge triggered limitation") {
                assertEquals(emptyList<Int>(), cpa_5)
            }
        }
    }
})
