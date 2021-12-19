class ListOfAuthorsJPanel : SearchableTableJPanel("Enter name of author - $alternateSpellingInstructions") {
    override val originalCollection: Collection<Any>
        get() = Catalog
            .entries
            .mapNotNullTo(mutableSetOf()) { it.author.ifBlank { null }/*there are a lot of seforim which don't have authors (e.g. chumash), so don't display blanks*/ }
            .also { println("New list requested") }
    override val listBeingDisplayed: MutableList<Any> = originalCollection.toMutableList()
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Author")
}