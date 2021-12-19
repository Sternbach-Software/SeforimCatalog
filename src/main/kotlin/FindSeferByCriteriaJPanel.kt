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
    override val columns: List<String> = listOf("Shelf (מס' מדף)", "Name (שם הספר)", "Author (שם המחבר)", "Publisher (הוצאה)", "Volume (כרך)", "Copies (מס' עתי')", "Category (קטיגורי)")
    override fun getCriteria(entry: CatalogEntry): String = getCriteriaLambda(entry)
}