import java.io.File
import java.nio.file.Files

fun main() {
    val list = mutableListOf<String>()
    val regex = "(?<=(Nanoseconds taken to get shiurID: ))\\d+".toRegex()
    Files.readAllLines(File("/Users/shmuel/AndroidStudioProjects/public-android/raw files/random.txt").toPath())
        .forEach {
//            val find = it.find(regex)
//            if (find.value != null) list.add(find.value)
        }
    File("/Users/shmuel/AndroidStudioProjects/public-android/raw files/random_output.txt")
        .writeText(list.joinToString("\n"))
}