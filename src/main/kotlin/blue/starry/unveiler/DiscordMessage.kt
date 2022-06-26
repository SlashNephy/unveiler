package blue.starry.unveiler

import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.MessageType

@Serializable
data class DiscordMessage(
    val id: Long,
    val type: MessageType,
    val member: Member,
    val content: String,
    val channel: Channel,
    val guild: Guild,
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
}
