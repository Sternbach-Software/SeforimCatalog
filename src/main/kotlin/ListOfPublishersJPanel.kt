class ListOfPublishersJPanel : SearchableTableJPanel("Enter name of publisher:") {
    override val originalCollection: Collection<Any>
        get() = Catalog.entries
            .mapTo(mutableSetOf()) { it.publisher }
    override val listBeingDisplayed: MutableList<Any> = originalCollection.toMutableList()
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Publisher")
}
