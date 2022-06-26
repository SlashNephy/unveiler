package blue.starry.unveiler

import kotlin.properties.ReadOnlyProperty

object Env {
    val DISCORD_TOKEN by string
    val DISCORD_CHANNEL_ID by long
    val MONGO_DATABASE_URI by string { "mongodb://localhost" }
    val DATA_DIRECTORY by string { "data" }
}

private val string: ReadOnlyProperty<Env, String>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name) ?: error("Env: ${property.name} is not present.")
    }

private fun string(block: () -> String): ReadOnlyProperty<Env, String> = ReadOnlyProperty { _, property ->
    System.getenv(property.name) ?: block()
}

private val long: ReadOnlyProperty<Env, Long>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
            ?.toLongOrNull()
            ?: error("Env: ${property.name} is not present.")
    }
