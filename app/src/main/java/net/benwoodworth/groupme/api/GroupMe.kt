package net.benwoodworth.groupme.api

import kotlinx.serialization.*
import kotlinx.serialization.internal.NullableSerializer
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import kotlin.coroutines.suspendCoroutine

internal class GroupMe(
    val accessToken: String
) : GroupMeApiV3 {

    private companion object {
        const val BASE_URL = "https://api.groupme.com/v3"

        val json = Json(
            encodeDefaults = false
        )

        fun String.urlEncoding(): String {
            return URLEncoder.encode(this)
        }
    }

    private object EmptySerializer : KSerializer<Nothing> {

        override val descriptor = object : SerialDescriptor {
            override val kind = UnionKind.OBJECT
            override val name = "EmptySerializer"

            override fun getElementIndex(name: String) = emptyError()
            override fun getElementName(index: Int) = emptyError()
            override fun isElementOptional(index: Int) = emptyError()
        }

        private fun emptyError(): Nothing {
            throw IllegalStateException("EmptySerializer cannot serialize elements")
        }

        override fun deserialize(decoder: Decoder) = emptyError()
        override fun serialize(encoder: Encoder, obj: Nothing) = emptyError()

        @Suppress("UNCHECKED_CAST")
        operator fun <T> invoke(): T = this as T
    }

    private suspend fun <TResponse : Any> apiGet(
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

    private suspend fun <TRequest : Any, TResponse : Any> apiPost(
        url: String,
        request: TRequest? = null,
        requestSerializer: KSerializer<TRequest> = EmptySerializer(),
        responseSerializer: KSerializer<TResponse> = EmptySerializer()
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
        ): GroupMeApiV3.Response<List<GroupMeApiV3.GroupsApi.Group>> {
            return apiGet(
                url = "/groups",
                parameters = mapOf(
                    "page" to page?.toString(),
                    "per_page" to per_page?.toString(),
                    "omit" to omit?.joinToString(",")
                ),
                responseSerializer = GroupMeApiV3.GroupsApi.Group.serializer().list
            )
        }

        override suspend fun former(): GroupMeApiV3.Response<List<GroupMeApiV3.GroupsApi.Group>> {
            return apiGet(
                url = "/groups/former",
                responseSerializer = GroupMeApiV3.GroupsApi.Group.serializer().list
            )
        }

        override suspend fun invoke(
            request: GroupMeApiV3.GroupsApi.GroupCreateRequest
        ): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.Group> {
            return apiPost(
                url = "/groups",
                request = request,
                requestSerializer = GroupMeApiV3.GroupsApi.GroupCreateRequest.serializer(),
                responseSerializer = GroupMeApiV3.GroupsApi.Group.serializer()
            )
        }

        override fun get(id: String) = object : GroupMeApiV3.GroupsApi.GroupApi {
            val groupUrlBase = "/groups/${id.urlEncoding()}"

            override suspend fun invoke(): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.Group> {
                return apiGet(
                    url = groupUrlBase,
                    responseSerializer = GroupMeApiV3.GroupsApi.Group.serializer()
                )
            }

            override suspend fun update(
                request: GroupMeApiV3.GroupsApi.GroupApi.GroupUpdateRequest
            ): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.Group> {
                return apiPost(
                    url = groupUrlBase,
                    request = request,
                    requestSerializer = GroupMeApiV3.GroupsApi.GroupApi.GroupUpdateRequest.serializer(),
                    responseSerializer = GroupMeApiV3.GroupsApi.Group.serializer()
                )
            }

            override suspend fun destroy(): GroupMeApiV3.Response<Nothing> {
                return apiPost(
                    url = groupUrlBase,
                    request = null
                )
            }

            override val join = object : GroupMeApiV3.GroupsApi.GroupApi.JoinApi {
                val joinUrlBase = "$groupUrlBase/join"

                override suspend fun invoke(): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.Group> {
                    return apiPost(
                        url = joinUrlBase,
                        request = null,
                        responseSerializer = GroupMeApiV3.GroupsApi.Group.serializer()
                    )
                }

                override fun get(shareToken: String) =
                    object : GroupMeApiV3.GroupsApi.GroupApi.JoinApi.JoinWithTokenApi {
                        val shareJoinUrlBase = "$joinUrlBase/${shareToken.urlEncoding()}"

                        override suspend fun invoke(): GroupMeApiV3.Response<GroupMeApiV3.GroupsApi.GroupApi.JoinApi.JoinResponse> {
                            return apiPost(
                                url = shareJoinUrlBase,
                                request = null,
                                responseSerializer = GroupMeApiV3.GroupsApi.GroupApi.JoinApi.JoinResponse.serializer()
                            )
                        }
                    }
            }

            override val members: GroupMeApiV3.GroupsApi.GroupApi.MembersApi
                get() = TODO("not implemented")
            override val messages: GroupMeApiV3.GroupsApi.GroupApi.GroupMessagesApi
                get() = TODO("not implemented")
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
