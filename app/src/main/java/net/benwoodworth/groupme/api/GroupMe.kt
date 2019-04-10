package net.benwoodworth.groupme.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.coroutines.suspendCoroutine

class GroupMe(
    val accessToken: String
) : GroupMeApiV3 {

    private companion object {
        const val BASE_URL = "https://api.groupme.com/v3"

        val json = Json(
            encodeDefaults = false
        )
    }

    private suspend inline fun <TResponse : Any> get(
        url: String,
        parameters: Map<String, String?> = emptyMap(),
        responseSerializer: KSerializer<TResponse>
    ): GroupMeApiV3.Response<TResponse> {
        return suspendCoroutine { continuation ->
            try {
                @Suppress("UNCHECKED_CAST")
                val tokenParams = parameters
                    .filterValues { it != null }
                    .toMutableMap() as MutableMap<String, String>

                tokenParams += "token" to accessToken

                val httpResponse = khttp.get(
                    url = "$BASE_URL$url",
                    params = tokenParams
                )

                val response = json.parse(
                    GroupMeApiV3.Response.serializer(responseSerializer),
                    httpResponse.text
                )

                continuation.resumeWith(Result.success(response))
            } catch (throwable: Throwable) {
                continuation.resumeWith(Result.failure(throwable))
            }
        }
    }

    private suspend inline fun <TRequest : Any, TResponse : Any> post(
        url: String,
        request: TRequest? = null,
        requestSerializer: KSerializer<TRequest>,
        responseSerializer: KSerializer<TResponse>
    ): GroupMeApiV3.Response<TResponse> {
        return suspendCoroutine { continuation ->
            try {
                @Suppress("UNCHECKED_CAST")
                val tokenParams = mapOf("token" to accessToken)

                val requestJson = request?.let {
                    json.stringify(
                        requestSerializer,
                        request
                    )
                }

                val httpResponse = khttp.post(
                    url = "$BASE_URL$url",
                    params = tokenParams,
                    json = requestJson
                )

                val response = json.parse(
                    GroupMeApiV3.Response.serializer(responseSerializer),
                    httpResponse.text
                )

                continuation.resumeWith(Result.success(response))
            } catch (throwable: Throwable) {
                continuation.resumeWith(Result.failure(throwable))
            }
        }
    }

    override val groups = object : GroupMeApiV3.GroupsApi {

        override suspend fun invoke(
            page: Int?,
            per_page: Int?,
            omit: List<String>?
        ): GroupMeApiV3.Response<List<GroupMeApiV3.Group>> {
            TODO("not implemented")
        }

        override suspend fun former(): GroupMeApiV3.Response<List<GroupMeApiV3.Group>> {
            TODO("not implemented")
        }

        override suspend fun create(
            name: String,
            description: String?,
            image_url: String?,
            share: Boolean?
        ): GroupMeApiV3.Response<GroupMeApiV3.Group> {
            TODO("not implemented")
        }

        override fun get(id: String): GroupMeApiV3.GroupsApi.GroupApi {
            TODO("not implemented")
        }

        override val likes = object : GroupMeApiV3.GroupsApi.GroupLikesApi {

            override suspend fun invoke(
                period: String
            ): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.GroupLikesApi.GroupLikesResponse> {
                TODO("not implemented")
            }

            override suspend fun mine(
            ): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.GroupLikesApi.GroupLikesResponse> {
                TODO("not implemented")
            }

            override suspend fun for_me(
            ): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.GroupLikesApi.GroupLikesResponse> {
                TODO("not implemented")
            }
        }
    }

    override val direct_messages = object : GroupMeApiV3.DirectMessagesApi {

        override suspend fun invoke(
            other_user_id: String,
            before_id: String?,
            since_id: String?
        ): GroupMeApiV3.Response<GroupMeApiV3.DirectMessagesApi.IndexResponse> {
            TODO("not implemented")
        }

        override suspend fun invoke(
            request: GroupMeApiV3.DirectMessagesApi.DirectMessageCreateRequest
        ): GroupMeApiV3.Response<GroupMeApiV3.DirectMessagesApi.DirectMessageCreateResponse> {
            TODO("not implemented")
        }
    }

    override val messages = object : GroupMeApiV3.MessagesApi {

        override fun get(conversation_id: String): GroupMeApiV3.MessagesApi.WithConversationIdApi {
            TODO("not implemented")
        }
    }

    override val bots = object : GroupMeApiV3.BotsApi {

        override suspend fun invoke(
            request: GroupMeApiV3.BotsApi.BotCreateRequest
        ): GroupMeApiV3.Response<GroupMeApiV3.BotsApi.Bot> {
            TODO("not implemented")
        }

        override suspend fun post(
            request: GroupMeApiV3.BotsApi.BotPostRequest
        ): GroupMeApiV3.Response<Nothing> {
            TODO("not implemented")
        }

        override suspend fun invoke(): GroupMeApiV3.Response<List<GroupMeApiV3.BotsApi.Bot>> {
            TODO("not implemented")
        }

        override suspend fun destroy(
            request: GroupMeApiV3.BotsApi.BotDestroyRequest
        ): GroupMeApiV3.Response<Nothing> {
            TODO("not implemented")
        }
    }

    override val users = object : GroupMeApiV3.UsersApi {

        override suspend fun me(): GroupMeApiV3.Response<GroupMeApiV3.UsersApi.User> {
            TODO("not implemented")
        }

        override suspend fun update(
            request: GroupMeApiV3.UsersApi.UserUpdateRequest
        ): GroupMeApiV3.Response<GroupMeApiV3.UsersApi.User> {
            TODO("not implemented")
        }

        override val sms_mode = object : GroupMeApiV3.UsersApi.SmsModeApi {

            override suspend fun invoke(
                request: GroupMeApiV3.UsersApi.SmsModeApi.SmsModeCreateRequest
            ): GroupMeApiV3.Response<Nothing> {
                TODO("not implemented")
            }

            override suspend fun delete(): GroupMeApiV3.Response<Nothing> {
                TODO("not implemented")
            }
        }
    }

    override val blocks = object : GroupMeApiV3.BlocksApi {

        override suspend fun invoke(
            user: String
        ): GroupMeApiV3.Response<GroupMeApiV3.BlocksApi.BlocksIndexResponse> {
            TODO("not implemented")
        }

        override suspend fun between(
            user: String,
            otherUser: String
        ): GroupMeApiV3.Response<GroupMeApiV3.BlocksApi.BlockBetweenResponse> {
            TODO("not implemented")
        }

        override suspend fun invoke(
            user: String,
            otherUser: String
        ): GroupMeApiV3.Response<GroupMeApiV3.BlocksApi.BlockCreateResponse> {
            TODO("not implemented")
        }

        override suspend fun delete(
            user: String,
            otherUser: String
        ): GroupMeApiV3.Response<Nothing> {
            TODO("not implemented")
        }
    }
}
