/**
 *
 * @author shmue
 */
class FindSeferByCriteriaJPanel(
    searchPhrase: String,
    val getCriteriaLambda: (CatalogEntry) -> String,
) : SearchableTableJPanel(searchPhrase) {
    override val originalCollection: Collection<Any>
        get() = Catalog.entries.also { println("New list requested") }
    override val listBeingDisplayed: MutableList<Any> = Catalog.entries.toMutableList()
    override val displayingCatalogEntry: Boolean = true
    override val columns: List<String> = listOf("Shelf", "Name", "Author", "Publisher", "Volume", "Copies", "Category")
    override fun getCriteria(entry: CatalogEntry): String = getCriteriaLambda(entry)
}