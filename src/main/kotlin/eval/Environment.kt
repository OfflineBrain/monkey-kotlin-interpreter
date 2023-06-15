package eval

data class Environment(
    val store: MutableMap<String, Object> = mutableMapOf(),
    val outer: Environment? = null
) : MutableMap<String, Object> by store
