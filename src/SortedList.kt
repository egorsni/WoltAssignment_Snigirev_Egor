import kotlinx.serialization.Serializable

@Serializable
data class SortedList(
    val title : String,
    val restaurants : MutableList<Restaurant>
)