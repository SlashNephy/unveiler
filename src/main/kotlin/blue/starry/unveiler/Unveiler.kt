package blue.starry.unveiler

import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.userAgent
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.io.path.name
import kotlin.io.path.writeBytes

object UnveilerListener: ListenerAdapter(), CoroutineScope {
    override val coroutineContext = Executors.newCachedThreadPool().asCoroutineDispatcher()

    private val logger = KotlinLogging.logger("unveiler")
    private val dataDirectory: Path = Paths.get(Env.DATA_DIRECTORY)
    private val httpClient = HttpClient {
        defaultRequest {
            userAgent("unveiler (+https://github.com/SlashNephy/unveiler)")
        }
    }
    private val mongo by lazy {
        KMongo.createClient(Env.MONGO_DATABASE_URI).getDatabase("unveiler").coroutine
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        // 自分自身のメッセージは無視する
        if (event.author == event.jda.selfUser) {
            return
        }

        // リッチなメッセージを無視する
        if (event.isWebhookMessage || event.message.embeds.isNotEmpty()) {
            return
        }

        launch {
            cacheMessage(event.message)
        }

        event.message.attachments.forEach { attachment ->
            launch {
                val filename = "${event.guild.id}_${event.channel.id}_${event.messageId}_${attachment.fileName}"
                cacheAttachment(attachment, filename)
            }
        }
    }

    private suspend fun cacheMessage(message: Message) {
        val collection = mongo.getCollection<DiscordMessage>()

        // すでに登録されているメッセージなら無視する
        if (collection.findOne(DiscordMessage::id eq message.idLong) != null) {
            return
        }

        collection.insertOne(DiscordMessage(
            id = message.idLong,
            type = message.type,
            member = DiscordMessage.Member(
                id = message.author.idLong,
                name = message.member?.nickname,
                username = message.author.name,
                discriminator = message.author.discriminator,
                avatarUrl = message.member?.effectiveAvatarUrl
            ),
            content = message.contentDisplay,
            guild = DiscordMessage.Guild(
                id = message.guild.idLong,
                name = message.guild.name,
                iconUrl = message.guild.iconUrl,
            ),
            channel = DiscordMessage.Channel(
                id = message.channel.idLong,
                name = message.channel.name,
                type = message.channel.type,
            ),
        ))
    }

    private suspend fun cacheAttachment(attachment: Message.Attachment, filename: String) {
        val response = httpClient.get(attachment.url)
        val data = response.readBytes()

        val path = dataDirectory.resolve(filename)

        withContext(Dispatchers.IO) {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory)
            }

            if (Files.exists(path)) {
                return@withContext
            }

            path.writeBytes(data)
            logger.info { "Cached: attachment = ${attachment.fileName}" }
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        val channel = event.jda.getTextChannelById(Env.DISCORD_CHANNEL_ID) ?: return

        launch {
            sendMessage(channel, event.messageIdLong)
            sendAttachments(channel, event.guild.idLong, event.channel.idLong, event.messageIdLong)
        }
    }

    private suspend fun fetchMessage(id: Long): DiscordMessage? {
        val collection = mongo.getCollection<DiscordMessage>()

        return collection.findOneAndDelete(DiscordMessage::id eq id)?.also {
            logger.info { "Deleted: message = ${it.id}" }
        }
    }

    private suspend fun sendMessage(channel: TextChannel, messageId: Long) {
        val message = fetchMessage(messageId) ?: return

        val embed = EmbedBuilder()
            .setAuthor("${message.member.name ?: message.member.username} (${message.member.username}#${message.member.discriminator})", null, message.member.avatarUrl)
            .setDescription(message.content)
            .setFooter("${message.guild.name} (#${message.channel.name}, ${message.channel.type})", message.guild.iconUrl)
            .build()

        suspendCoroutine { cont ->
            channel.sendMessageEmbeds(embed).queue(
                { cont.resume(Unit) },
                { cont.resumeWithException(it) }
            )
        }
    }

    private suspend fun sendAttachments(channel: TextChannel, guildId: Long, channelId: Long, messageId: Long) {
        val files = withContext(Dispatchers.IO) {
            Files.list(dataDirectory)
        }

        val paths = files.filter { it.fileName.name.startsWith("${guildId}_${channelId}_${messageId}_") }.toList()
        if (paths.isEmpty()) {
            return
        }

        paths.map {
            coroutineScope {
                launch {
                    sendAttachment(channel, it)
                }
            }
        }.joinAll()
    }

    private suspend fun sendAttachment(channel: TextChannel, path: Path) {
        suspendCoroutine { cont ->
            channel.sendFile(path.toFile()).queue(
                { cont.resume(Unit) },
                { cont.resumeWithException(it) }
            )
        }

        logger.info { "Deleted: attachment = ${path.fileName}" }
    }
}
