package blue.starry.unveiler

import kotlin.properties.ReadOnlyProperty

object Env {
    val DISCORD_TOKEN by string
    val DISCORD_CHANNEL_ID by long
}

private val string: ReadOnlyProperty<Env, String>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name) ?: error("Env: ${property.name} is not present.")
    }

private val long: ReadOnlyProperty<Env, Long>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
            ?.toLongOrNull()
            ?: error("Env: ${property.name} is not present.")
    }
