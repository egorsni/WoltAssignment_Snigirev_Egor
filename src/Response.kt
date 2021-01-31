import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val sections : List<SortedList>
)