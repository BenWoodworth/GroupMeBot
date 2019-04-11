package net.benwoodworth.groupme.api

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import net.benwoodworth.groupme.api.GroupMeApiV3.*
import net.benwoodworth.groupme.api.GroupMeApiV3.BlocksApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.BotsApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.DirectMessagesApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.GroupMessagesApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.JoinApi.GroupJoinResponse
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.JoinApi.JoinWithTokenApi
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.MembersApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.MembersApi.MemberApi.MemberUpdateRequest
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.MembersApi.ResultsApi.ResultApi
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupApi.MembersApi.ResultsApi.ResultApi.ResultResponse
import net.benwoodworth.groupme.api.GroupMeApiV3.GroupsApi.GroupLikesApi.GroupLikesResponse
import net.benwoodworth.groupme.api.GroupMeApiV3.MessagesApi.WithConversationIdApi
import net.benwoodworth.groupme.api.GroupMeApiV3.MessagesApi.WithConversationIdApi.WithMessageIdApi
import net.benwoodworth.groupme.api.GroupMeApiV3.UsersApi.*
import net.benwoodworth.groupme.api.GroupMeApiV3.UsersApi.SmsModeApi.SmsModeCreateRequest

class GroupMe(
    private val accessToken: String
) : GroupMeApiV3 {

    private val apiUrlBase = "https://api.groupme.com/v3"

    private val json = Json(
        encodeDefaults = false,
        strictMode = false
    )

    private object NothingSerializer : KSerializer<Nothing> {

        override val descriptor = object : SerialDescriptor {
            override val kind = UnionKind.OBJECT
            override val name = "NothingSerializer"

            override fun getElementIndex(name: String) = emptyError()
            override fun getElementName(index: Int) = emptyError()
            override fun isElementOptional(index: Int) = emptyError()
        }

        private fun emptyError(): Nothing {
            throw IllegalStateException("NothingSerializer cannot serialize elements")
        }

        override fun deserialize(decoder: Decoder) = emptyError()
        override fun serialize(encoder: Encoder, obj: Nothing) = emptyError()
    }

    private suspend fun <TResponse : Any> apiGet(
        url: String,
        parameters: Map<String, String?> = emptyMap(),
        responseSerializer: KSerializer<TResponse>
    ): Response<TResponse> {
        val httpResponse = buildUrl(
            url = url,
            params = parameters + ("token" to accessToken)
        ).request(
            method = "GET"
        )

        return json.parse(
            Response.serializer(responseSerializer),
            httpResponse.data.toString(Charsets.UTF_8)
        )
    }

    private suspend fun <TRequest, TResponse> apiPost(
        url: String,
        parameters: Map<String, String?> = emptyMap(),
        request: TRequest?,
        requestSerializer: KSerializer<TRequest>,
        responseSerializer: KSerializer<TResponse>
    ): Response<TResponse> {
        val requestData = request?.let {
            json.stringify(
                requestSerializer,
                request
            )
        }

        val httpResponse = buildUrl(
            url = url,
            params = parameters + ("token" to accessToken)
        ).request(
            method = "POST",
            data = requestData?.toByteArray(Charsets.UTF_8)
        )

        return json.parse(
            Response.serializer(responseSerializer),
            httpResponse.data.toString(Charsets.UTF_8)
        )
    }

    private suspend fun <TResponse> apiPost(
        url: String,
        parameters: Map<String, String?> = emptyMap(),
        responseSerializer: KSerializer<TResponse>
    ): Response<TResponse> {
        return apiPost(
            url = url,
            parameters = parameters,
            request = null,
            requestSerializer = NothingSerializer,
            responseSerializer = responseSerializer
        )
    }

    private suspend fun <TRequest> apiPost(
        url: String,
        parameters: Map<String, String?> = emptyMap(),
        request: TRequest?,
        requestSerializer: KSerializer<TRequest>
    ): Response<Nothing> {
        return apiPost(
            url = url,
            parameters = parameters,
            request = request,
            requestSerializer = requestSerializer,
            responseSerializer = NothingSerializer
        )
    }

    private suspend fun apiPost(
        url: String,
        parameters: Map<String, String?> = emptyMap()
    ): Response<Nothing> {
        return apiPost(
            url = url,
            parameters = parameters,
            request = null,
            requestSerializer = NothingSerializer,
            responseSerializer = NothingSerializer
        )
    }

    override val groups = object : GroupsApi {
        private val groupsUrlBase = "$apiUrlBase/groups"

        override suspend fun invoke(
            page: Int?,
            per_page: Int?,
            omit: List<String>?
        ): Response<List<Group>> {
            return apiGet(
                url = groupsUrlBase,
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
                url = "$groupsUrlBase/former",
                responseSerializer = Group.serializer().list
            )
        }

        override suspend fun invoke(request: GroupCreateRequest): Response<Group> {
            return apiPost(
                url = groupsUrlBase,
                request = request,
                requestSerializer = GroupCreateRequest.serializer(),
                responseSerializer = Group.serializer()
            )
        }

        override fun get(id: String) = object : GroupApi {
            private val groupUrlBase = "$groupsUrlBase/${id.urlEncode()}"

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
                return apiPost(groupUrlBase)
            }

            override val join = object : JoinApi {
                private val joinUrlBase = "$groupUrlBase/join"

                override suspend fun invoke(): Response<Group> {
                    return apiPost(
                        url = joinUrlBase,
                        responseSerializer = Group.serializer()
                    )
                }

                override fun get(shareToken: String) = object : JoinWithTokenApi {
                    private val shareJoinUrlBase = "$joinUrlBase/${shareToken.urlEncode()}"

                    override suspend fun invoke(): Response<GroupJoinResponse> {
                        return apiPost(
                            url = shareJoinUrlBase,
                            responseSerializer = GroupJoinResponse.serializer()
                        )
                    }
                }
            }

            override val members = object : MembersApi {
                private val membersUrlBase = "$groupUrlBase/members"

                override suspend fun add(request: MemberAddRequest): Response<MemberAddResponse> {
                    return apiPost(
                        url = "$membersUrlBase/add",
                        request = request,
                        requestSerializer = MemberAddRequest.serializer(),
                        responseSerializer = MemberAddResponse.serializer()
                    )
                }

                override fun get(id: String) = object : MemberApi {
                    private val memberIdUrlBase = "$membersUrlBase/${id.urlEncode()}"

                    override suspend fun remove(): Response<Nothing> {
                        return apiPost("$memberIdUrlBase/remove")
                    }

                    override suspend fun update(request: MemberUpdateRequest): Response<Member> {
                        return apiPost(
                            url = "$memberIdUrlBase/update",
                            request = request,
                            requestSerializer = MemberUpdateRequest.serializer(),
                            responseSerializer = Member.serializer()
                        )
                    }
                }

                override val results = object : ResultsApi {
                    private val resultUrlBase = "$membersUrlBase/results"

                    override fun get(resultsId: String) = object : ResultApi {
                        private val resultIdUrlBase = "$resultUrlBase/${resultsId.urlEncode()}"

                        override suspend fun invoke(): Response<ResultResponse> {
                            return apiGet(
                                url = resultIdUrlBase,
                                responseSerializer = ResultResponse.serializer()
                            )
                        }
                    }
                }
            }

            override val messages = object : GroupMessagesApi {
                private val messagesUrlBase = "$groupUrlBase/messages"

                override suspend fun invoke(
                    before_id: String?,
                    since_id: String?,
                    after_id: String?,
                    limit: Int?
                ): Response<GroupMessagesIndexResponse> {
                    return apiGet(
                        url = messagesUrlBase,
                        parameters = mapOf(
                            "before_id" to before_id,
                            "since_id" to since_id,
                            "after_id" to after_id,
                            "limit" to limit?.toString()
                        ),
                        responseSerializer = GroupMessagesIndexResponse.serializer()
                    )
                }

                override suspend fun invoke(
                    request: GroupMessageCreateRequest
                ): Response<GroupMessageCreateResponse> {
                    return apiPost(
                        url = messagesUrlBase,
                        request = request,
                        requestSerializer = GroupMessageCreateRequest.serializer(),
                        responseSerializer = GroupMessageCreateResponse.serializer()
                    )
                }
            }
        }

        override val likes = object : GroupLikesApi {
            private val likesUrlBase = "$groupsUrlBase/likes"

            override suspend fun invoke(period: String): Response<GroupLikesResponse> {
                return apiGet(
                    url = likesUrlBase,
                    parameters = mapOf(
                        "period" to period
                    ),
                    responseSerializer = GroupLikesResponse.serializer()
                )
            }

            override suspend fun mine(): Response<GroupLikesResponse> {
                return apiGet(
                    url = "$likesUrlBase/mine",
                    responseSerializer = GroupLikesResponse.serializer()
                )
            }

            override suspend fun for_me(): Response<GroupLikesResponse> {
                return apiGet(
                    url = "$likesUrlBase/for_me",
                    responseSerializer = GroupLikesResponse.serializer()
                )
            }
        }
    }

    override val direct_messages = object : DirectMessagesApi {
        private val directMessagesUrlBase = "$apiUrlBase/direct_messages"

        override suspend fun invoke(
            other_user_id: String,
            before_id: String?,
            since_id: String?
        ): Response<IndexResponse> {
            return apiGet(
                url = directMessagesUrlBase,
                parameters = mapOf(
                    "other_user_id" to other_user_id,
                    "before_id" to before_id,
                    "since_id" to since_id
                ),
                responseSerializer = IndexResponse.serializer()
            )
        }

        override suspend fun invoke(
            request: DirectMessageCreateRequest
        ): Response<DirectMessageCreateResponse> {
            return apiPost(
                url = directMessagesUrlBase,
                request = request,
                requestSerializer = DirectMessageCreateRequest.serializer(),
                responseSerializer = DirectMessageCreateResponse.serializer()
            )
        }
    }

    override val messages = object : MessagesApi {
        private val messagesUrlBase = "$apiUrlBase/messages"

        override fun get(conversation_id: String) = object : WithConversationIdApi {
            private val conversationIdUrlBase = "$messagesUrlBase/${conversation_id.urlEncode()}"

            override fun get(message_id: String) = object : WithMessageIdApi {
                private val messageIdUrlBase = "$conversationIdUrlBase/${message_id.urlEncode()}"

                override suspend fun like(): Response<Nothing> {
                    return apiPost("$messageIdUrlBase/like")
                }

                override suspend fun unlike(): Response<Nothing> {
                    return apiPost("$messageIdUrlBase/unlike")
                }
            }
        }
    }

    override val bots = object : BotsApi {
        private val botsUrlBase = "$apiUrlBase/bots"

        override suspend fun invoke(request: BotCreateRequest): Response<Bot> {
            return apiPost(
                url = botsUrlBase,
                request = request,
                requestSerializer = BotCreateRequest.serializer(),
                responseSerializer = Bot.serializer()
            )
        }

        override suspend fun post(request: BotPostRequest): Response<Nothing> {
            return apiPost(
                url = "$botsUrlBase/post",
                request = request,
                requestSerializer = BotPostRequest.serializer()
            )
        }

        override suspend fun invoke(): Response<List<Bot>> {
            return apiGet(
                url = botsUrlBase,
                responseSerializer = Bot.serializer().list
            )
        }

        override suspend fun destroy(request: BotDestroyRequest): Response<Nothing> {
            return apiPost("$botsUrlBase/destroy")
        }
    }

    override val users = object : UsersApi {
        private val usersUrlBase = "$apiUrlBase/users"

        override suspend fun me(): Response<User> {
            return apiGet(
                url = "$usersUrlBase/me",
                responseSerializer = User.serializer()
            )
        }

        override suspend fun update(request: UserUpdateRequest): Response<User> {
            return apiPost(
                url = "$usersUrlBase/update",
                request = request,
                requestSerializer = UserUpdateRequest.serializer(),
                responseSerializer = User.serializer()
            )
        }

        override val sms_mode = object : SmsModeApi {
            private val smsModeUrlBase = "$usersUrlBase/sms_mode"

            override suspend fun invoke(request: SmsModeCreateRequest): Response<Nothing> {
                return apiPost(
                    url = smsModeUrlBase,
                    request = request,
                    requestSerializer = SmsModeCreateRequest.serializer()
                )
            }

            override suspend fun delete(): Response<Nothing> {
                return apiPost(smsModeUrlBase)
            }
        }
    }

    override val blocks = object : BlocksApi {
        private val blocksUrlBase = "$apiUrlBase/blocks"

        override suspend fun invoke(user: String): Response<BlocksIndexResponse> {
            return apiGet(
                url = blocksUrlBase,
                responseSerializer = BlocksIndexResponse.serializer()
            )
        }

        override suspend fun between(
            user: String,
            otherUser: String
        ): Response<BlockBetweenResponse> {
            return apiGet(
                url = "$blocksUrlBase/between",
                parameters = mapOf(
                    "user" to user,
                    "otherUser" to otherUser
                ),
                responseSerializer = BlockBetweenResponse.serializer()
            )
        }

        override suspend fun invoke(
            user: String,
            otherUser: String
        ): Response<BlockCreateResponse> {
            return apiPost(
                url = blocksUrlBase,
                parameters = mapOf(
                    "user" to user,
                    "otherUser" to otherUser
                ),
                responseSerializer = BlockCreateResponse.serializer()
            )
        }

        override suspend fun delete(
            user: String,
            otherUser: String
        ): Response<Nothing> {
            return apiPost(
                url = "$blocksUrlBase/delete",
                parameters = mapOf(
                    "user" to user,
                    "otherUser" to otherUser
                )
            )
        }
    }
}
