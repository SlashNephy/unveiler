package blue.starry.unveiler

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus

fun main() {
    val jda = JDABuilder.createDefault(Env.DISCORD_TOKEN)
        .addEventListeners(UnveilerListener)
        .setStatus(OnlineStatus.INVISIBLE)
        .build()

    jda.awaitReady()
}
