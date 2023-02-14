class ListOfShelvesJPanel : SearchableTableJPanel("Enter number of shelf$alternatePhrases:") {
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Categories on shelf", "Shelf")
    override val getElementToSearchBy: (Pair<*, *>) -> String = { it.second as String }
    override fun getOriginalList(): MutableCollection<Any> {
        return Catalog
            .entries
            .mapValueToKey({ it.shelfNum }, { it.category }) { categories -> categories.joinToString() }
            .mapTo(mutableListOf()) { it.value to it.key }
    }
}
