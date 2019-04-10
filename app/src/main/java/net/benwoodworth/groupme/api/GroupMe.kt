package net.benwoodworth.groupme.api

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import net.benwoodworth.groupme.api.GroupMeApiV3.*
import net.benwoodworth.groupme.api.GroupMeApiV3.BlocksApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.BotsApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.DirectMessagesApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.JoinApi.GroupJoinResponse
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.JoinApi.JoinWithTokenApi
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupLikesApi.GroupLikesResponse
import net.benwoodworth.groupme.api.GroupMeApiV3.MessagesApi.WithConversationIdApi
import net.benwoodworth.groupme.api.GroupMeApiV3.UsersApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.UsersApi.SmsModeApi.SmsModeCreateRequest
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

        @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
        inline operator fun <T> invoke(): T = this as T
    }

    private suspend fun <TResponse : Any> apiGet(
        url: String,
        parameters: Map<String, String?> = emptyMap(),
        responseSerializer: KSerializer<TResponse>
    ): Response<TResponse> {
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
                    Response.serializer(responseSerializer),
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
    ): Response<TResponse> {
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
                    Response.serializer(responseSerializer),
                    httpResponse.text
                )

                continuation.resumeWith(Result.success(response))
            } catch (throwable: Throwable) {
                continuation.resumeWith(Result.failure(throwable))
            }
        }
    }

    override val groups = object : GroupsApi {

        override suspend fun invoke(
            page: Int?,
            per_page: Int?,
            omit: List<String>?
        ): Response<List<Group>> {
            return apiGet(
                url = "/groups",
                parameters = mapOf(
                    "page" to page?.toString(),
                    "per_page" to per_page?.toString(),
                    "omit" to omit?.joinToString(",")
                ),
                responseSerializer = Group.serializer().list
            )
        }

        override suspend fun former(): Response<List<Group>> {
            return apiGet(
                url = "/groups/former",
                responseSerializer = Group.serializer().list
            )
        }

        override suspend fun invoke(request: GroupCreateRequest): Response<Group> {
            return apiPost(
                url = "/groups",
                request = request,
                requestSerializer = GroupCreateRequest.serializer(),
                responseSerializer = Group.serializer()
            )
        }

        override fun get(id: String) = object : GroupApi {
            val groupUrlBase = "/groups/${id.urlEncoding()}"

            override suspend fun invoke(): Response<Group> {
                return apiGet(
                    url = groupUrlBase,
                    responseSerializer = Group.serializer()
                )
            }

            override suspend fun update(request: GroupUpdateRequest): Response<Group> {
                return apiPost(
                    url = groupUrlBase,
                    request = request,
                    requestSerializer = GroupUpdateRequest.serializer(),
                    responseSerializer = Group.serializer()
                )
            }

            override suspend fun destroy(): Response<Nothing> {
                return apiPost(
                    url = groupUrlBase,
                    request = null
                )
            }

            override val join = object : JoinApi {
                val joinUrlBase = "$groupUrlBase/join"

                override suspend fun invoke(): Response<Group> {
                    return apiPost(
                        url = joinUrlBase,
                        request = null,
                        responseSerializer = Group.serializer()
                    )
                }

                override fun get(shareToken: String) = object : JoinWithTokenApi {
                    val shareJoinUrlBase = "$joinUrlBase/${shareToken.urlEncoding()}"

                    override suspend fun invoke(): Response<GroupJoinResponse> {
                        return apiPost(
                            url = shareJoinUrlBase,
                            request = null,
                            responseSerializer = GroupJoinResponse.serializer()
                        )
                    }
                }
            }

            override val members: MembersApi
                get() = TODO("not implemented")
            override val messages: GroupMessagesApi
                get() = TODO("not implemented")
        }

        override val likes = object : GroupLikesApi {

            override suspend fun invoke(period: String): Response<GroupLikesResponse> {
                TODO("not implemented")
            }

            override suspend fun mine(
            ): Response<GroupLikesResponse> {
                TODO("not implemented")
            }

            override suspend fun for_me(
            ): Response<GroupLikesResponse> {
                TODO("not implemented")
            }
        }
    }

    override val direct_messages = object : DirectMessagesApi {

        override suspend fun invoke(
            other_user_id: String,
            before_id: String?,
            since_id: String?
        ): Response<IndexResponse> {
            TODO("not implemented")
        }

        override suspend fun invoke(
            request: DirectMessageCreateRequest
        ): Response<DirectMessageCreateResponse> {
            TODO("not implemented")
        }
    }

    override val messages = object : MessagesApi {

        override fun get(conversation_id: String): WithConversationIdApi {
            TODO("not implemented")
        }
    }

    override val bots = object : BotsApi {

        override suspend fun invoke(request: BotCreateRequest): Response<Bot> {
            TODO("not implemented")
        }

        override suspend fun post(request: BotPostRequest): Response<Nothing> {
            TODO("not implemented")
        }

        override suspend fun invoke(): Response<List<Bot>> {
            TODO("not implemented")
        }

        override suspend fun destroy(request: BotDestroyRequest): Response<Nothing> {
            TODO("not implemented")
        }
    }

    override val users = object : UsersApi {

        override suspend fun me(): Response<User> {
            TODO("not implemented")
        }

        override suspend fun update(request: UserUpdateRequest): Response<User> {
            TODO("not implemented")
        }

        override val sms_mode = object : SmsModeApi {

            override suspend fun invoke(request: SmsModeCreateRequest): Response<Nothing> {
                TODO("not implemented")
            }

            override suspend fun delete(): Response<Nothing> {
                TODO("not implemented")
            }
        }
    }

    override val blocks = object : BlocksApi {

        override suspend fun invoke(user: String): Response<BlocksIndexResponse> {
            TODO("not implemented")
        }

        override suspend fun between(
            user: String,
            otherUser: String
        ): Response<BlockBetweenResponse> {
            TODO("not implemented")
        }

        override suspend fun invoke(
            user: String,
            otherUser: String
        ): Response<BlockCreateResponse> {
            TODO("not implemented")
        }

        override suspend fun delete(
            user: String,
            otherUser: String
        ): Response<Nothing> {
            TODO("not implemented")
        }
    }
}
