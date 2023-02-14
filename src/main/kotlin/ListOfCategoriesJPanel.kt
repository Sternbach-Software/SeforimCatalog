class ListOfCategoriesJPanel : SearchableTableJPanel("Enter name of category$alternatePhrases:") {
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Shelves that contain category", "Category")
    override val getElementToSearchBy: (Pair<*, *>) -> String = { it.second as String }
    override fun getOriginalList(): MutableCollection<Any> {
        return Catalog
            .entries
            .mapValueToKey({ it.category }, { it.shelfNum }) { shelves ->
                shelves.sortedWith(shelfNumComparator).joinToString()
            }
            .mapTo(mutableListOf()) { it.value to it.key }
    }
}
