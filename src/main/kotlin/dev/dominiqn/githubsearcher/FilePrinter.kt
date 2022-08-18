package dev.dominiqn.githubsearcher

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.appendText
import kotlin.io.path.exists
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class FilePrinter(
    private val resourceLoader: ResourceLoader,
) {
    fun print(string: String, filePath: String) {
        val path = Path.of("src/main/resources/${filePath}").also {
            if (it.exists().not()) Files.createFile(it)
        }

        path.appendText("$string\n")
    }
}
