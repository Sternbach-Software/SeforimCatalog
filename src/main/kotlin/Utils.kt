
/**
 * Takes a collection (e.g. list of seforim) and returns a map of every occurrence of the
 * ...I'm not even going to try.
 *
 * Used to show all categories contained on a single shelf, and all shelves that contain a category.
 * */
fun <E, K, V, TRANSFORM> Collection<E>.mapValueToKey(
    getKey: (E) -> K,
    getValue: (E) -> V,
    transformValues: (HashSet<V>) -> TRANSFORM
): Map<K, TRANSFORM> {
    val mapOfKeyToValues = mutableMapOf<K, HashSet<V>>()
    val mapOfValueToKeys = mutableMapOf<V, HashSet<K>>()
    /**
     * Adds [value] to the hash set stored in [this] map at [key] if [key] is present, or creates a new hash set
     * with [value] and stores it in [this] map at key. The equivalent of [MutableMap.putIfAbsent], but instead of
     * taking a hash set, it takes a value to add to the hash set.
     * */
    fun <K, V> MutableMap<K, HashSet<V>>.putIfAbsent(key: K, value: V) {
        this[key]?.add(value) ?: this.put(key, hashSetOf(value))
    }
    for (sefer in this) {
        mapOfKeyToValues.putIfAbsent(getKey(sefer), getValue(sefer))
        mapOfValueToKeys.putIfAbsent(getValue(sefer), getKey(sefer))
    }
    return mapOfKeyToValues.mapValues { transformValues(it.value) }
}/*
    private fun <K> mapValueToKey(): List<Pair<K, String>> {
        val mapOfCategoryToShelves = mutableMapOf<String*//*category*//*, HashSet<String*//*shelf number*//*>>()
        val mapOfShelfToCategories = mutableMapOf<String*//*shelf number*//*, HashSet<String*//*category*//*>>()
        fun <K, V> MutableMap<K, HashSet<V>>.putIfAbsent(key: K, value: V) {
            this[key]?.add(value) ?: this.put(key, hashSetOf(value))
        }
        for (sefer in Catalog.entries) {
            mapOfCategoryToShelves.putIfAbsent(sefer.category, sefer.shelfNum)
            mapOfShelfToCategories.putIfAbsent(sefer.shelfNum, sefer.category)
        }
        return mapOfCategoryToShelves.map { it.key to it.value.joinToString() }
    }*/