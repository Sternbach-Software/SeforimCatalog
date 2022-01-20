/**
 *
 * @author shmue
 */
class FindSeferByCriteriaJPanel(
    searchPhrase: String,
    val getCriteriaLambda: (CatalogEntry) -> String,
) : SearchableTableJPanel(searchPhrase) {
    override val originalCollection: Collection<Any>
        get() = Catalog.entries
    override val listBeingDisplayed: MutableList<Any> = Catalog.entries.toMutableList()
    override val displayingCatalogEntry: Boolean = true
    override val columns: List<String> = listOf(
        "Publisher (הוצאה)",
        "Category (קטיגורי)",
        "Volume (כרך)",
        "Author (שם המחבר)",
        "Shelf (מס' מדף)",
        "Name (שם הספר)"
    )
    override fun getCriteria(entry: CatalogEntry): String = getCriteriaLambda(entry)
}