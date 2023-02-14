class ListOfAuthorsJPanel : SearchableTableJPanel("Enter name of author$alternatePhrases:") {
    override val displayingCatalogEntry: Boolean = false
    override val columns: List<String> = listOf("Author")
    override fun getOriginalList(): MutableCollection<Any> = Catalog.entries
        .mapNotNullTo(mutableSetOf()) { it.author.ifBlank { null }/*there are a lot of seforim which don't have authors (e.g. chumash), so don't display blanks*/ }
}