package net.benwoodworth.groupme

import kotlinx.coroutines.runBlocking
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.GroupMessagesApi
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupMessage

fun GroupMessagesApi.toSequence(
    beforeId: String? = null,
    pageSize: Int? = null
): Sequence<GroupMessage> = sequence {
    val messages = this@toSequence
    var lastMessageId: String? = beforeId

    while (true) {
        val response = runBlocking {
            messages(
                before_id = lastMessageId,
                limit = pageSize
            )
        }

        when (val code = response.meta.code) {
            200 -> { // OK
                yieldAll(response.response!!.messages)
            }
            304 -> { // No messages
                return@sequence
            }
            else -> {
                throw Exception("Bad response code: $code. Errors: ${response.meta.errors}")
            }
        }

        lastMessageId = response.response.messages
            .lastOrNull()?.id ?: return@sequence
    }
}
