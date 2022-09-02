package blue.starry.unveiler

import kotlinx.coroutines.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object UnveilerListener: ListenerAdapter(), CoroutineScope {
    override val coroutineContext = Executors.newCachedThreadPool().asCoroutineDispatcher()

    private val messages = DiscordMessageStore(Env.MONGO_DATABASE_URI)
    private val attachments = DiscordAttachmentStore(Env.DATA_DIRECTORY)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        launch {
            storeMessage(event.message)
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        launch {
            storeMessage(event.message)
        }
    }

    override fun onMessageDelete(event: MessageDeleteEvent) {
        val channel = event.jda.getTextChannelById(Env.DISCORD_CHANNEL_ID) ?: return

        launch {
            unveilMessage(channel, event.messageIdLong)
            unveilAttachments(channel, event.guild.idLong, event.channel.idLong, event.messageIdLong)
        }
    }

    private suspend fun storeMessage(message: Message) {
        // 自分自身のメッセージは無視する
        if (message.author == message.jda.selfUser) {
            return
        }

        // リッチなメッセージを無視する
        if (message.isWebhookMessage || message.embeds.isNotEmpty()) {
            return
        }

        listOf(
            coroutineScope {
                launch {
                    messages.save(message)
                }
            },
            coroutineScope {
                launch {
                    attachments.save(message.attachments, message)
                }
            }
        ).joinAll()
    }

    private suspend fun unveilMessage(channel: TextChannel, messageId: Long) {
        val message = messages.find(messageId) ?: return

        val embed = EmbedBuilder()
            .setAuthor("${message.member.name ?: message.member.username} (${message.member.username}#${message.member.discriminator})", null, message.member.avatarUrl)
            .setDescription(message.content)
            .setImage(message.stickers.firstOrNull()?.url)
            .setFooter("${message.guild.name} (#${message.channel.name}, ${message.channel.type})", message.guild.iconUrl)
            .build()

        suspendCoroutine { cont ->
            channel.sendMessageEmbeds(embed).queue(
                { cont.resume(Unit) },
                { cont.resumeWithException(it) }
            )
        }
    }

    private suspend fun unveilAttachments(channel: TextChannel, guildId: Long, channelId: Long, messageId: Long) {
        val attachments = this.attachments.find(guildId, channelId, messageId)
        if (attachments.isEmpty()) {
            return
        }

        attachments.map { attachment ->
            coroutineScope {
                launch {
                    suspendCoroutine { cont ->
                        val upload = FileUpload.fromData(attachment.stream, attachment.filename)
                        channel.sendFiles(upload).queue(
                            { cont.resume(Unit) },
                            { cont.resumeWithException(it) }
                        )
                    }
                }
            }
        }.joinAll()
    }
}
