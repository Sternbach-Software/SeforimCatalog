class ListOfShelvesJPanel : SearchableTableJPanel("Enter number of shelf$alternatePhrases:") {
    override val originalCollection: Collection<Any>
        get() =
            Catalog
                .entries
                .mapValueToKey({ it.shelfNum }, { it.category }) { categories -> categories.joinToString() }
                .map { it.value to it.key }
    override val listBeingDisplayed: MutableList<Any> = originalCollection.toMutableList().toSynchronizedList()
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Categories on shelf", "Shelf")
    override val getElementToSearchBy: (Pair<*, *>) -> String = { it.second as String }
}
