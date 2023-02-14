/**
 *
 * @author shmue
 */
class FindSeferByCriteriaJPanel(
    searchPhrase: String,
    val getCriteriaLambda: (CatalogEntry) -> String,
    getLemmatizedCriteriaLambda: ((LemmatizedCatalogEntry) -> Set<Set<String>>)? = null,
) : SearchableTableJPanel(searchPhrase, getLemmatizedCriteriaLambda) {
    override val originalCollectionLemmatized: MutableList<LemmatizedCatalogEntry> by lazy { Catalog.entriesLemmatized.toMutableList() }
    override val displayingCatalogEntry: Boolean = true
    val seferNameColumnString = "Name (שם הספר)"
    val shelfNumColumnString = "Shelf (מס' מדף)"
    override val columns: List<String> = listOf(
        "Publisher (הוצאה)",
        "Category (קטיגורי)",
        "Volume (כרך)",
        "Author (שם המחבר)",
        shelfNumColumnString,
        seferNameColumnString
    )
    override fun getCriteria(entry: CatalogEntry): String = getCriteriaLambda(entry)
}
