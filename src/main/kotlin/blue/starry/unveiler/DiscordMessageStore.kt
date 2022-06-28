package blue.starry.unveiler

import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Message
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

class DiscordMessageStore(private val databaseUri: String) {
    private val logger = KotlinLogging.logger("DiscordMessageStore")

    private val database by lazy {
        KMongo.createClient(databaseUri).getDatabase("unveiler").coroutine
    }

    suspend fun find(messageId: Long): DiscordMessage? {
        val collection = database.getCollection<DiscordMessage>()
        val filter = DiscordMessage::id eq messageId

        return collection.findOneAndDelete(filter)?.also {
            logger.info { "Deleted: message = $messageId" }
        }
    }

    suspend fun save(message: Message) {
        val collection = database.getCollection<DiscordMessage>()
        val filter = DiscordMessage::id eq message.idLong

        // すでに登録されているメッセージなら更新する
        if (collection.findOne(filter) != null) {
            collection.replaceOne(filter, DiscordMessage.from(message))
            logger.info { "Replaced: message = ${message.id}" }
        } else {
            collection.insertOne(DiscordMessage.from(message))
            logger.info { "Inserted: message = ${message.id}" }
        }
    }
}
