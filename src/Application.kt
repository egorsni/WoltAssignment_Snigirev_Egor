import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun rad(x: Double): Double {
    return x * Math.PI / 180
};

fun Boolean.toInt() = if (this) 1 else 0

fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6378137; // Earth’s mean radius in meter
    val dLat = rad(lat2 - lat1)
    val dLong = rad(lon2 - lon1)
    val a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLong / 2) * Math.sin(
            dLong / 2
        )
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val d = R * c
    return d; // returns the distance in meter
}

fun Application.module() {
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
            accept(ContentType.Application.Json)
        }
    }
    install(ContentNegotiation) {
        json()
    }
    routing {
        route("/discovery") {
            get {
                val formatter = DateTimeFormatter.ofPattern("yyy-MM-dd")
                val lat = call.request.queryParameters["lat"]?.toDouble() ?: error("Invalid delete request")
                val lon = call.request.queryParameters["lon"]?.toDouble() ?: error("Invalid delete request")
                val popular = SortedList("Popular Restaurants", mutableListOf())
                val newest = SortedList("New Restaurants", mutableListOf())
                val nearest = SortedList("Nearby Restaurants", mutableListOf())

                val list: RestaurantsList = client.get("https://api.mocki.io/v1/5fa4f916") // given json url

                // Creating popular list
                list.restaurants.removeIf { distance(it.location.last(), it.location.first(), lat, lon) > 1500 }
                list.restaurants.sortWith(compareBy({ -it.online.toInt() }, { -it.popularity }))
                for (i in 0 until minOf(10, list.restaurants.size)) {
                    popular.restaurants.add(list.restaurants[i])
                }

                // Creating nearby list
                list.restaurants.sortWith(compareBy({ -it.online.toInt() }, { distance(it.location.last(), it.location.first(), lat, lon) }))
                for (i in 0 until minOf(10, list.restaurants.size)) {
                    nearest.restaurants.add(list.restaurants[i])
                }

                // Creating newest list
                list.restaurants.removeIf { LocalDate.parse("2020-09-31", formatter) > LocalDate.parse(it.launch_date, formatter) }  // compare to the date 4 month ago
                list.restaurants.sortWith(compareBy<Restaurant> {-it.online.toInt()}.thenByDescending { LocalDate.parse(it.launch_date, formatter) })
                for (i in 0 until minOf(10, list.restaurants.size)) {
                    newest.restaurants.add(list.restaurants[i])
                }

                // Sending response
                val response = Response(listOf(popular, newest, nearest))
                call.respond(response)
            }
        }
    }
}

