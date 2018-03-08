package com.cashalgo.demo

import java.util.TreeMap

/**
 * Price Alert.
 *
 * This class is for price alert.
 *
 * @param id price alert id.
 * @param mapSymbol version 1.0 param for map<symbol, AlertList>.
 * @param mapSympri version 1.1 param for map<symbol, TreeMap>.
 * @param symbolPrice version 1.1 param for previous price.
 * @param AlertList version 1.0 and 1.1 param.
 * @function addPriceAlert Creates an new price alert.
 * @function removePriceAlert remove price alert.
 * @function checkPriceAlert check price alert.
 * @function clearPriceAlert clear price alert.
 */
class PriceAlert() {
    private var id: Int = 1
    private var mapSympri = mutableMapOf<String, TreeMap<Double, MutableList<AlertList>>>()
    private var symbolPrice = mutableMapOf<String, Double>()

    data class AlertList(val id: Int, val user: String, val symbol: String, val price: Double, val direction: String)

    fun addPriceAlert(user: String, symbol: String, price: Double, direction: String) {
        val al = AlertList(id, user, symbol, price, direction)
        val newPrice = if (direction == "1") price else -price
        val mapPrice = mapSympri.getOrPut(symbol, { TreeMap() })
        val alertList = mapPrice.getOrPut(newPrice, { mutableListOf() })
        alertList.add(al)

        id += 1
    }

    fun removePriceAlert(user: String, symbol: String) {
        val priceMap = mapSympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>())
        val symbolList = priceMap.values
        symbolList.forEach {
            it.removeIf { it.user == user }
        }
        priceMap.values.removeIf{ it.isEmpty()}
        mapSympri.values.removeIf{ it.isEmpty()}
    }

    fun checkPriceAlert(symbol: String, currentPrice: Double) : List<Int> {
        val previousPrice = symbolPrice[symbol]
        symbolPrice[symbol] = currentPrice
        val priceMap = mapSympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>())
        val pairList = listOf<Pair<Double, Double>>(Pair(-(previousPrice ?: Double.POSITIVE_INFINITY), -currentPrice), Pair(previousPrice ?: 0.0, currentPrice))
        return pairList.flatMap { (a,b)->(if (a < b ) priceMap.subMap(a,b) else emptyMap<Double, MutableList<AlertList>>()).map { subs->subs.value }.flatMap { it.map { it.id } } }
    }

    fun clearPriceAlert() {
        id = 1
        mapSympri.clear()
        symbolPrice.clear()
    }
}

fun main(args: Array<String>) {

    var pa = PriceAlert()
    /*
    // case 1
    pa.checkPriceAlert("00001", 100.0)
    pa.clearPriceAlert()
    */

    /*
    // case 2
    pa.addPriceAlert("a", "00001", 100.0, "-1")
    pa.checkPriceAlert("00001", 96.0)
    pa.removePriceAlert("a", "00002")
    pa.removePriceAlert("a", "00001")
    pa.checkPriceAlert("00001", 96.0)
    pa.clearPriceAlert()
    */

    /*
    //case 3
    pa.addPriceAlert("b", "00002", 50.0, "-1")
    pa.addPriceAlert("c", "00003", 200.0, "1")
    pa.checkPriceAlert("00002", 45.0)
    pa.clearPriceAlert()
    */

    /*
    //case 4
    pa.addPriceAlert("a", "00001", 100.0, "1")
    pa.addPriceAlert("a", "00001", 105.0, "1")
    pa.addPriceAlert("b", "00001", 90.0, "-1")
    pa.addPriceAlert("c", "00002", 50.0, "1")
    pa.addPriceAlert("d", "00003", 200.0, "-1")
    pa.addPriceAlert("e", "00003", 180.0, "1")
    pa.addPriceAlert("f", "00003", 200.0, "-1")
    pa.checkPriceAlert("00001", 107.0)
    pa.checkPriceAlert("00001", 93.0)
    pa.removePriceAlert("a", "00001")
    pa.checkPriceAlert("00001", 107.0)
    pa.checkPriceAlert("00003", 190.0)
    pa.checkPriceAlert("00003", 200.0)
    pa.clearPriceAlert()
    */
}