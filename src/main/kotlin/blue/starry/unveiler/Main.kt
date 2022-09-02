package blue.starry.unveiler

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter

fun main() {
    val jda = JDABuilder.createDefault(
        Env.DISCORD_TOKEN,
        listOf(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
    )
        .setChunkingFilter(ChunkingFilter.NONE)
        .addEventListeners(UnveilerListener)
        .setStatus(OnlineStatus.INVISIBLE)
        .build()

    jda.awaitReady()
}
