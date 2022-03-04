/*5319
מס' מיון
שם הספר
שם המחבר
הוצאה
כרך
מס' עתי'
Date
Initials
Needs binding
Comments
קטיגורי
מס' מדף*/
open class CatalogEntry(
    val numberNotSure: String,
    val miyunNum: String,
    val seferName: String,
    val author: String,
    val publisher: String,
    val volumeNum: String,
    val numCopies: String,
    val dateAdded: String,
    val initials: String,
    val needsBinding: String,
    val comments: String,
    val category: String,
    val shelfNum: String
) {
    override fun toString(): String {
        return "CatalogEntry(seferName=$seferName, author=$author, publisher=$publisher, volumeNum=$volumeNum, category=$category, shelfNum=$shelfNum)"
    }
    fun everythingIsBlank() =
        seferName.isBlank() &&
//                numberNotSure.isBlank() &&
//                miyunNum.isBlank() &&
                author.isBlank() &&
                publisher.isBlank() &&
                volumeNum.isBlank() &&
//                numCopies.isBlank() &&
//                dateAdded.isBlank() &&
//                initials.isBlank() &&
//                needsBinding.isBlank() &&
//                comments.isBlank() &&
                category.isBlank() &&
                shelfNum.isBlank()
}