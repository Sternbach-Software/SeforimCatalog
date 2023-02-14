class ListOfPublishersJPanel : SearchableTableJPanel("Enter name of publisher$alternatePhrases:") {
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Publisher")
    override fun getOriginalList(): MutableCollection<Any> {
        return Catalog.entries.mapTo(mutableSetOf()) { it.publisher }
    }
}
