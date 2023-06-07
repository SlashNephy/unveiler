package blue.starry.unveiler

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.Attachment
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class DiscordAttachmentStore(dataDirectory: String) {
    private val logger = KotlinLogging.logger("DiscordAttachmentStore")

    private val dataDirectory = Paths.get(dataDirectory)
    private val httpClient = HttpClient {
        defaultRequest {
            userAgent("unveiler (+https://github.com/SlashNephy/unveiler)")
        }
    }

    data class AttachmentData(val stream: InputStream, val filename: String)

    suspend fun find(guildId: Long, channelId: Long, messageId: Long): List<AttachmentData> {
        val paths = withContext(Dispatchers.IO) {
            Files.list(dataDirectory)
        }.filter {
            it.fileName.name.startsWith("${guildId}_${channelId}_${messageId}_")
        }.toList()

        return paths.map { path ->
            val stream = withContext(Dispatchers.IO) {
                path.readBytes().inputStream().also {
                    Files.delete(path)
                    logger.info { "Deleted: attachment = $path" }
                }
            }
            val filename = path.fileName.name.split("_").drop(3).joinToString("_")

            AttachmentData(stream, filename)
        }
    }

    suspend fun save(attachments: List<Attachment>, message: Message) {
        attachments.map {
            coroutineScope {
                launch {
                    saveOne(it, message)
                }
            }
        }.joinAll()
    }

    private suspend fun saveOne(attachment: Attachment, message: Message) {
        val response = httpClient.get(attachment.url)
        val data = response.readBytes()

        val filename = "${message.guild.id}_${message.channel.id}_${message.id}_${attachment.fileName}"
        val path = dataDirectory.resolve(filename)

        withContext(Dispatchers.IO) {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory)
            }

            if (!Files.exists(path)) {
                path.writeBytes(data)
            }
        }

        logger.info { "Downloaded: attachment = ${attachment.fileName}" }
    }
}
