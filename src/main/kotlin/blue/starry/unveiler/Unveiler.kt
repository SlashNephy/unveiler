package blue.starry.unveiler

import io.ktor.client.request.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.AttachmentOption
import java.nio.file.Files
import kotlin.io.path.name
import kotlin.io.path.writeBytes

object Unveiler: ListenerAdapter() {
    private val targetChannel by lazy {
        UnveilerJDAClient.getTextChannelById(Env.DISCORD_CHANNEL_ID)
            ?: error("TextChannel: ${Env.DISCORD_CHANNEL_ID} is not found.")
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author == event.jda.selfUser || !event.isFromType(ChannelType.TEXT)) {
            return
        }

        event.message.attachments.forEach { attachment ->
            GlobalScope.launch {
                val path = dataDirectory.resolve("${event.guild.id}_${event.channel.id}_${event.messageId}_${attachment.fileName}")
                if (Files.exists(path)) {
                    return@launch
                }

                val response = UnveilerHttpClient.get<ByteArray>(attachment.url)
                path.writeBytes(response)
                logger.info { "Downloaded: ${attachment.fileName} posted by ${event.member?.effectiveName} at #${event.channel.name}." }
            }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        val paths = Files.list(dataDirectory)
            .filter { it.fileName.name.startsWith("${event.guild.id}_${event.channel.id}_${event.messageId}_") }
            .toList()
        if (paths.isEmpty()) {
            return
        }

        val request = targetChannel.sendFile(paths.first().toFile(), AttachmentOption.SPOILER)
        paths.drop(1).forEach {
            request.addFile(it.toFile(), it.fileName.name.split("_").drop(3).joinToString("_"), AttachmentOption.SPOILER)
        }

        request.queue {
            paths.forEach {
                Files.delete(it)
                logger.info { "Deleted: ${it.fileName}." }
            }
        }
    }
}
