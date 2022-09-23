/**
 *
 * @author shmue
 */
class FindSeferByCriteriaJPanel(
    searchPhrase: String,
    val getCriteriaLambda: (CatalogEntry) -> String,
    getLemmatizedCriteriaLambda: ((LemmatizedCatalogEntry) -> Set<Set<String>>)? = null,
) : SearchableTableJPanel(searchPhrase, getLemmatizedCriteriaLambda) {
    override val originalCollection: Collection<Any>
        get() = Catalog.entries
    override val originalCollectionLemmatized: List<LemmatizedCatalogEntry>
        get() = Catalog.entriesLemmatized
    override val listBeingDisplayed: MutableList<Any> = Catalog.entries.toMutableList().toSynchronizedList() as MutableList<Any>
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
