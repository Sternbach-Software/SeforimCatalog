import kotlin.jvm.JvmStatic
import java.io.File
import java.nio.file.Files
import javax.swing.GroupLayout

object CatalogParser {
    @JvmStatic
    fun main(args: Array<String>) {
        val file = File("C:\\Users\\shmue\\Downloads\\Shmuel Sternbach's Copy of Catalog  - Catalog.tsv")
        val lines = Files.readAllLines(file.toPath())
        val objects = lines.map {
            val split = it.split("\t")
            CatalogEntry(
                split[0],
                split[1],
                split[2],
                split[3],
                split[4],
                split[5],
                split[6],
                split[7],
                split[8],
                split[9],
                split[10],
                split[11],
                split[12]
            )
        }
        objects.forEach { println(it) }
    }
}
