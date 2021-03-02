package blue.starry.unveiler

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.http.*
import net.dv8tion.jda.api.JDABuilder

val UnveilerHttpClient by lazy {
    HttpClient {
        defaultRequest {
            userAgent("unveiler (+https://github.com/SlashNephy/unveiler)")
        }
    }
}

val UnveilerJDAClient by lazy {
    JDABuilder.createDefault(Env.DISCORD_TOKEN)
        .addEventListeners(Unveiler)
        .build()
}
