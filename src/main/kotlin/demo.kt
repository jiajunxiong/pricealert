
import org.eclipse.jetty.server.Server
import org.glassfish.jersey.jetty.JettyHttpContainerFactory
import org.glassfish.jersey.server.ResourceConfig
import java.util.*
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriBuilder

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
@Path("home")
class PriceAlert {
    private var id: Int = 1
    private var mapSympri = mutableMapOf<String, TreeMap<Double, MutableList<AlertList>>>()
    private var symbolPrice = mutableMapOf<String, Double>()

    data class AlertList(val id: Int, val user: String, val symbol: String, val price: Double, val direction: String)

    @GET
    @Path("add")
    @Produces(MediaType.TEXT_PLAIN)
    fun addPriceAlert(@QueryParam("user") user: String, @QueryParam("symbol") symbol: String, @QueryParam("price") price: Double, @QueryParam("direction") direction: String) {
        val al = AlertList(id, user, symbol, price, direction)
        val newPrice = if (direction == "1") price else -price
        val mapPrice = mapSympri.getOrPut(symbol, { TreeMap() })
        val alertList = mapPrice.getOrPut(newPrice, { mutableListOf() })
        alertList.add(al)
        id += 1
        println("addPriceAlert successful!")
    }

    @GET
    @Path("remove")
    @Produces(MediaType.TEXT_PLAIN)
    fun removePriceAlert(@QueryParam("user") user: String, @QueryParam("symbol") symbol: String) {
        val priceMap = mapSympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>())
        val symbolList = priceMap.values
        symbolList.forEach {
            it.removeIf { it.user == user }
        }
        priceMap.values.removeIf{ it.isEmpty()}
        mapSympri.values.removeIf{ it.isEmpty()}
        println("removePriceAlert successful!")
    }

    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    fun checkPriceAlert(@QueryParam("symbol") symbol: String, @QueryParam("currentPrice") currentPrice: Double) : List<Int> {
        val previousPrice = symbolPrice[symbol]
        symbolPrice[symbol] = currentPrice
        val priceMap = mapSympri.getOrDefault(symbol, TreeMap<Double, MutableList<AlertList>>())
        val pairList = listOf<Pair<Double, Double>>(Pair(-(previousPrice ?: Double.POSITIVE_INFINITY), -currentPrice), Pair(previousPrice ?: 0.0, currentPrice))
        //println(pairList.flatMap { (a,b)->(if (a < b ) priceMap.subMap(a,b) else emptyMap<Double, MutableList<AlertList>>()).map { subs->subs.value }.flatMap { it.map { it.id } } })
        return pairList.flatMap { (a,b)->(if (a < b ) priceMap.subMap(a,b) else emptyMap<Double, MutableList<AlertList>>()).map { subs->subs.value }.flatMap { it.map { it.id } } }
    }

    @GET
    @Path("clear")
    fun clearPriceAlert() {
        id = 1
        mapSympri.clear()
        symbolPrice.clear()
        println("clearPriceAlert successful!")
    }
}

/**
 * Executes the given [block] function on this Server and then destroys it.
 *
 * This Mimic's the kotlin stdlib's Closeable::use.
 *
 * @param block a function to process this Server.
 */
public inline fun Server.use(block: (Server) -> Unit) {
    try {
        block(this)
    } finally {
        this.destroy()
    }
}

fun main(args: Array<String>) {
    val config = ResourceConfig().register(PriceAlert())
    val baseUri = UriBuilder.fromUri("http://localhost/").port(2222).build()
    JettyHttpContainerFactory.createServer(baseUri, config).use { server -> server.join() }
}


