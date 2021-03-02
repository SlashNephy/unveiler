package blue.starry.unveiler

import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Paths

internal val logger = KotlinLogging.logger("unveiler")
internal val dataDirectory = Paths.get("data")

fun main() {
    if (!Files.exists(dataDirectory)) {
        Files.createDirectories(dataDirectory)
    }

    UnveilerJDAClient.awaitReady()
}
