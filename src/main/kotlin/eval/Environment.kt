package eval

import `object`.Object

data class Environment(
    val store: MutableMap<String, Object> = mutableMapOf(),
    val outer: Environment? = null
) : MutableMap<String, Object> by store {
    override operator fun get(key: String): Object? {
        return store[key] ?: outer?.get(key)
    }
}
