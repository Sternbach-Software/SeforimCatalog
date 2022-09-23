class ListOfCategoriesJPanel : SearchableTableJPanel("Enter name of category$alternatePhrases:") {
    override val originalCollection: Collection<Any>
        get() =
            Catalog
                .entries
                .mapValueToKey({ it.category }, { it.shelfNum }) { shelves -> shelves.sortedWith(shelfNumComparator).joinToString() }
                .map { it.value to it.key }
    override val listBeingDisplayed: MutableList<Any> = originalCollection.toMutableList().toSynchronizedList()
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Shelves that contain category", "Category")
    override val getElementToSearchBy: (Pair<*, *>) -> String = { it.second as String }
}
