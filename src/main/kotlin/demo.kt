package com.cashalgo.demo

import javafx.scene.control.Alert
import java.util.ArrayList
import java.util.HashMap
import java.util.TreeMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
    //var mapSymbol = mutableMapOf<String, MutableList<AlertList>>()
    private var mapSympri = mutableMapOf<String, TreeMap<Double, MutableList<AlertList>>>()
    private var symbolPrice = mutableMapOf<String, Double>()

    data class AlertList(val id: Int, val user: String, val symbol: String, val price: Double, val direction: String)

    fun addPriceAlert(user: String, symbol: String, price: Double, direction: String) {
        val al = AlertList(id, user, symbol, price, direction)
        /* version 1.0
        if (mapSymbol.getOrDefault(symbol, mutableListOf<AlertList>()).isNotEmpty()) {
            mapSymbol.getValue(symbol).add(al)
        } else {
            mapSymbol.put(symbol, mutableListOf<AlertList>(al))
        }
        */

        /* version 1.1
        var newPrice = if (direction == "1") price else -price
        if (mapSympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>()).isNotEmpty()) {
            var mapPrice = mapSympri.getValue(symbol)
            if (mapPrice.getOrDefault(newPrice, mutableListOf<AlertList>()).isNotEmpty()) {
                mapPrice.getValue(newPrice).add(al)
            } else {
                mapPrice.put(newPrice, mutableListOf<AlertList>(al))
            }
        } else {
            var mapPrice = TreeMap<Double, MutableList<AlertList>>()
            mapPrice.put(newPrice, mutableListOf<AlertList>(al))
            mapSympri.put(symbol, mapPrice)
        }
        */

        // version 1.2
        val newPrice = if (direction == "1") price else -price
        val mapPrice = mapSympri.getOrPut(symbol, { TreeMap() })
        val alertList = mapPrice.getOrPut(newPrice, { mutableListOf() })
        alertList.add(al)

        id += 1
    }

    fun removePriceAlert(user: String, symbol: String) {
        /* version 1.0
        var symbolList = mapSymbol.getOrDefault(symbol, mutableListOf<AlertList>())
        if (symbolList.isNotEmpty()) {
            symbolList.removeIf({ it.user == user })
        }
        */

        // version 1.2
        val priceMap = mapSympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>())
        val symbolList = priceMap.values
        symbolList.forEach {
            it.removeIf { it.user == user }
        }
        priceMap.values.removeIf{ it.isEmpty()}
        mapSympri.values.removeIf{ it.isEmpty()}
    }

    fun checkPriceAlert(symbol: String, currentPrice: Double) : List<Int> {
        /* version 1.0
        var id_list = mutableListOf<Int>()
        var check_list = mapSymbol.getOrDefault(symbol, mutableListOf<AlertList>())
        if (check_list.isNotEmpty()) {
            for (item in check_list) {
                if (item.price > currentPrice && item.direction == "-1") {
                    id_list.add(item.id)
                }
                if (item.price < currentPrice && item.direction == "1") {
                    id_list.add(item.id)
                }
            }
        }
        println(id_list)
        */

        /* version 1.1
        var previous_price = 0.0
        symbolPrice.put(symbol, currentPrice)
        previous_price = symbolPrice.getValue(symbol)
        println(previous_price)

        var alert_list = mutableListOf<AlertList>()
        var alert_list_pos = mutableListOf<AlertList>()
        var price_map = map_sympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>())
        if (price_map.isNotEmpty()) {
            var price_list = price_map.keys
            val (price_list_pos, price_list_neg) = price_list.partition { it > 0 }
            var neg_index_first = price_list_neg.indexOfFirst { abs(it) > currentPrice }
            var neg_index_last = price_list_neg.indexOfLast { abs(it) > currentPrice }
            var pos_index_first = price_list_pos.indexOfFirst { it < currentPrice }
            var pos_index_last = price_list_pos.indexOfLast { it < currentPrice }
            if (neg_index_first == -1) {
                alert_list = emptyList<AlertList>().toMutableList()
            } else {
                var price_list = price_list_neg.subList(neg_index_first, neg_index_last+1)
                val iterator = price_list.iterator()
                var list = mutableListOf<AlertList>()
                iterator.forEach {
                    list = price_map.getValue(it)
                    alert_list.addAll(list)
                }
            }
            if (pos_index_first == -1) {
                alert_list_pos = emptyList<AlertList>().toMutableList()
            } else {
                var price_list = price_list_pos.subList(pos_index_first, pos_index_last+1)
                val iterator = price_list.iterator()
                var list = mutableListOf<AlertList>()
                iterator.forEach {
                    list = price_map.getValue(it)
                    alert_list_pos.addAll(list)
                }
            }
            alert_list.addAll(alert_list_pos)
            var id_list = mutableListOf<Int>()
            var iterator = alert_list.iterator()
            iterator.forEach {
                if (it.price > min(previous_price, currentPrice) && it.price < max(previous_price, currentPrice)) {
                    id_list.add(it.id)
                }
            }
            println(id_list)
        }
        */

        // version 1.2
        //var previous_price : Double? = 0.0
        val previousPrice = symbolPrice[symbol]
        symbolPrice[symbol] = currentPrice
        val priceMap = mapSympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>())
        val pairList = listOf<Pair<Double, Double>>(Pair(-(previousPrice ?: Double.POSITIVE_INFINITY), -currentPrice), Pair(previousPrice ?: 0.0, currentPrice))
        
        return pairList.flatMap { (a,b)->(if (a < b ) priceMap.subMap(a,b) else emptyMap<Double, MutableList<AlertList>>()).map { subs->subs.value }.flatMap { it.map { it.id } } }
        //if (previous_price > currentPrice) price_map.subMap(-previous_price, -currentPrice).map { subs -> subs.value }.flatMap { it.map { it.id } }
        //else price_map.subMap(previous_price, currentPrice).map { subs -> subs.value }.flatMap { it.map { it.id } }

        //return idList
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