import kotlin.Throws
import java.io.IOException
import kotlin.jvm.JvmStatic
import java.io.File
import java.nio.file.Files
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
data class CatalogEntry(
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
    val shelfNum: String,
) {
    val split = author.split(",\\s?".toRegex())
    val authorFirstName: String
        get() = split[0]
    val authorLastName: String
        get() = split[1]
    fun everythingExceptNameIsBlank() = numberNotSure.isBlank() &&
    miyunNum.isBlank() &&
    author.isBlank() &&
    publisher.isBlank() &&
    volumeNum.isBlank() &&
    numCopies.isBlank() &&
    dateAdded.isBlank() &&
    initials.isBlank() &&
    needsBinding.isBlank() &&
    comments.isBlank() &&
    category.isBlank() &&
    shelfNum.isBlank()
}