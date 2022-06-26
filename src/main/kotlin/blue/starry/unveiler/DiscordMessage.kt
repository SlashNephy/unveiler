package blue.starry.unveiler

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType

@Serializable
data class DiscordMessage(
    val id: Long,
    val type: MessageType,
    val member: Member,
    val content: String,
    val channel: Channel,
    val guild: Guild,
    val stickers: List<Sticker>,
) {
    @Serializable
    data class Member(
        val id: Long,
        val name: String?,
        val username: String,
        val discriminator: String,
        val avatarUrl: String?,
    )

    @Serializable
    data class Channel(
        val id: Long,
        val name: String?,
        val type: ChannelType,
    )

    @Serializable
    data class Guild(
        val id: Long,
        val name: String,
        val iconUrl: String?,
    )

    @Serializable
    data class Sticker(
        val id: Long,
        val name: String,
        val url: String,
    )

    companion object {
        fun from(message: Message): DiscordMessage {
            return DiscordMessage(
                id = message.idLong,
                type = message.type,
                member = Member(
                    id = message.author.idLong,
                    name = message.member?.nickname,
                    username = message.author.name,
                    discriminator = message.author.discriminator,
                    avatarUrl = message.member?.effectiveAvatarUrl
                ),
                content = message.contentDisplay,
                guild = Guild(
                    id = message.guild.idLong,
                    name = message.guild.name,
                    iconUrl = message.guild.iconUrl,
                ),
                channel = Channel(
                    id = message.channel.idLong,
                    name = message.channel.name,
                    type = message.channel.type,
                ),
                stickers = message.stickers.map {
                    Sticker(
                        id = it.idLong,
                        name = it.name,
                        url = it.iconUrl,
                    )
                }
            )
        }
    }
}
