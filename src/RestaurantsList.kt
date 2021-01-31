import kotlinx.serialization.Serializable

@Serializable
data class RestaurantsList(
    var restaurants : MutableList<Restaurant>
)

@Serializable
data class Restaurant(
    val blurhash : String,
    val launch_date : String,
    val location : List<Double>,
    val name : String,
    val online : Boolean,
    val popularity : Double
)