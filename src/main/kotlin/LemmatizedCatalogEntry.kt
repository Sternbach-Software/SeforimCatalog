data class LemmatizedCatalogEntry(
    val _seferName: Pair<String, Set<Set<String>>>/*name to eachWordInName.map { it.lemmas }*/,
    val _author: Pair<String, Set<Set<String>>>,
    val _publisher: String,
    val _volumeNum: String,
    val _category: String,
    val _shelfNum: String,
): CatalogEntry("", "", _seferName.first, _author.first, _publisher, _volumeNum, "", "", "", "", "", _category, _shelfNum)