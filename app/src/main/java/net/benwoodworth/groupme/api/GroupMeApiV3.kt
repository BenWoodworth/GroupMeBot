package net.benwoodworth.groupme.api

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

/**
 * [https://dev.groupme.com/docs/v3]
 */
interface GroupMeApiV3 {

    /**
     * [https://dev.groupme.com/docs/v3#v3]
     */
    @Serializable
    data class Response<T>(
        val response: T? = null,
        val meta: Meta
    ) {
        @Serializable
        data class Meta(
            val code: Int,
            @Optional val errors: List<String>? = null
        )
    }

    @Serializable
    data class Attachment(
        val type: String,
        @Optional val url: String? = null,
        @Optional val lat: Float? = null,
        @Optional val lng: Float? = null,
        @Optional val name: String? = null,
        @Optional val token: String? = null,
        @Optional val placeholder: String? = null,
        @Optional val charmap: List<List<Int>>? = null,
        @Optional val user_ids: List<String>? = null
    )


    /**
     * [https://dev.groupme.com/docs/v3#groups]
     */
    val groups: GroupsApi

    interface GroupsApi {

        @Serializable
        data class Group(
            val id: String,
            val name: String,
            val type: String,
            val description: String,
            val image_url: String? = null,
            val creator_user_id: String,
            val created_at: Long,
            val updated_at: Long,
            val members: List<Member>,
            val share_url: String? = null,
            val messages: Messages
        ) {
            @Serializable
            data class Messages(
                val count: Int,
                val last_message_id: String,
                @Optional val last_message_creates_at: String? = null,
                val preview: Preview
            ) {
                @Serializable
                data class Preview(
                    val nickname: String,
                    val text: String,
                    val image_url: String,
                    val attachments: List<Attachment>
                )
            }
        }

        @Serializable
        data class Member(
            val id: String,
            val user_id: String,
            val nickname: String,
            val muted: Boolean,
            val image_url: String? = null,
            val autokicked: Boolean,
            @Optional val app_installed: Boolean? = null,
            val roles: List<String>,
            @Optional val state: String? = null
        )

        @Serializable
        data class GroupMessage(
            val id: String,
            val source_guid: String,
            val created_at: Long,
            val user_id: String,
            val group_id: String,
            val name: String,
            val avatar_url: String?,
            val text: String?,
            val system: Boolean,
            val favorited_by: List<String>,
            val attachments: List<Attachment>
        )

        /**
         * [https://dev.groupme.com/docs/v3#groups_index]
         */
        suspend operator fun invoke(
            page: Int? = null,
            per_page: Int? = null,
            omit: List<String>? = null
        ): Response<List<Group>>

        /**
         * [https://dev.groupme.com/docs/v3#groups_index_former]
         */
        suspend fun former(): Response<List<Group>>

        /**
         * [https://dev.groupme.com/docs/v3#groups_create]
         */
        suspend operator fun invoke(request: GroupCreateRequest): Response<Group>

        @Serializable
        data class GroupCreateRequest(
            val name: String,
            val description: String? = null,
            val image_url: String? = null,
            val share: Boolean? = null
        )


        operator fun get(id: String): GroupApi

        interface GroupApi {

            /**
             * [https://dev.groupme.com/docs/v3#groups_show]
             */
            suspend operator fun invoke(): Response<Group>

            /**
             * [https://dev.groupme.com/docs/v3#groups_update]
             */
            suspend fun update(request: GroupUpdateRequest): Response<Group>

            @Serializable
            data class GroupUpdateRequest(
                val name: String? = null,
                val description: String? = null,
                val image_url: String? = null,
                val office_mode: Boolean? = null,
                val share: Boolean? = null
            )

            /**
             * [https://dev.groupme.com/docs/v3#groups_destroy]
             */
            suspend fun destroy(): Response<Nothing>


            val join: JoinApi

            interface JoinApi {

                /**
                 * [https://dev.groupme.com/docs/v3#groups_rejoin]
                 */
                suspend operator fun invoke(): Response<Group>


                operator fun get(shareToken: String): JoinWithTokenApi

                interface JoinWithTokenApi {

                    /**
                     * [https://dev.groupme.com/docs/v3#groups_join]
                     */
                    suspend operator fun invoke(): Response<GroupJoinResponse>
                }

                @Serializable
                data class GroupJoinResponse(
                    val group: Group
                )
            }


            /**
             * [https://dev.groupme.com/docs/v3#members]
             */
            val members: MembersApi

            interface MembersApi {

                /**
                 * [https://dev.groupme.com/docs/v3#members_add]
                 */
                suspend fun add(request: MemberAddRequest): Response<MemberAddResponse>

                @Serializable
                data class MemberAddRequest(
                    val members: List<AddRequestMember>
                ) {
                    @Serializable
                    data class AddRequestMember(
                        val nickname: String,
                        val user_id: String? = null,
                        val phone_number: String? = null,
                        val email: String? = null,
                        val guid: String? = null
                    )
                }

                @Serializable
                data class MemberAddResponse(
                    val results_id: String
                )


                operator fun get(id: String): MemberApi

                interface MemberApi {

                    /**
                     * [https://dev.groupme.com/docs/v3#members_remove]
                     */
                    suspend fun remove(): Response<Nothing>

                    /**
                     * [https://dev.groupme.com/docs/v3#members_update]
                     */
                    suspend fun update(request: MemberUpdateRequest): Response<Member>

                    @Serializable
                    data class MemberUpdateRequest(
                        val membership: Membership
                    ) {
                        @Serializable
                        data class Membership(
                            val nickname: String? = null,
                            val avatar_url: String? = null
                        )
                    }
                }


                val results: ResultsApi

                interface ResultsApi {

                    operator fun get(resultsId: String): ResultApi

                    interface ResultApi {

                        /**
                         * [https://dev.groupme.com/docs/v3#members_results]
                         */
                        suspend operator fun invoke(): Response<ResultResponse>

                        @Serializable
                        data class ResultResponse(
                            val messages: List<GroupMessage>
                        )
                    }
                }
            }


            /**
             * [https://dev.groupme.com/docs/v3#messages]
             */
            val messages: GroupMessagesApi

            interface GroupMessagesApi {

                /**
                 * [https://dev.groupme.com/docs/v3#messages_index]
                 */
                suspend operator fun invoke(
                    before_id: String? = null,
                    since_id: String? = null,
                    after_id: String? = null,
                    limit: Int? = null
                ): Response<GroupMessagesIndexResponse>

                @Serializable
                data class GroupMessagesIndexResponse(
                    val count: Int,
                    val messages: List<GroupMessage>
                )

                /**
                 * [https://dev.groupme.com/docs/v3#messages_create]
                 */
                suspend operator fun invoke(request: GroupMessageCreateRequest): Response<GroupMessageCreateResponse>

                @Serializable
                data class GroupMessageCreateRequest(
                    val message: CreateRequestMessage
                ) {
                    @Serializable
                    data class CreateRequestMessage(
                        val source_guid: String,
                        val text: String,
                        val attachments: List<Attachment>? = null
                    )
                }

                @Serializable
                data class GroupMessageCreateResponse(
                    val message: GroupMessage
                )
            }
        }


        /**
         * [https://dev.groupme.com/docs/v3#likes]
         */
        val likes: GroupLikesApi

        interface GroupLikesApi {

            @Serializable
            data class GroupLikesResponse(
                val messages: List<GroupMessage>
            )

            /**
             * [https://dev.groupme.com/docs/v3#leaderboard_index]
             */
            suspend operator fun invoke(period: String): Response<GroupLikesResponse>

            /**
             * [https://dev.groupme.com/docs/v3#leaderboard_mine]
             */
            suspend fun mine(): Response<GroupLikesResponse>

            /**
             * [https://dev.groupme.com/docs/v3#leaderboard_for_me]
             */
            suspend fun for_me(): Response<GroupLikesResponse>
        }
    }


    /**
     * [https://dev.groupme.com/docs/v3#direct_messages]
     */
    val direct_messages: DirectMessagesApi

    interface DirectMessagesApi {

        @Serializable
        data class DirectMessage(
            val id: String,
            val source_guid: String,
            val created_at: Long,
            val user_id: String,
            val recipient_id: String,
            val name: String,
            val avatar_url: String,
            val text: String,
            val system: Boolean,
            val favorited_by: List<String>,
            val attachments: List<Attachment>
        )

        /**
         * [https://dev.groupme.com/docs/v3#direct_messages_index]
         */
        suspend operator fun invoke(
            other_user_id: String,
            before_id: String? = null,
            since_id: String? = null
        ): Response<IndexResponse>

        @Serializable
        data class IndexResponse(
            val count: Int,
            val direct_messages: List<DirectMessage>
        )

        /**
         * [https://dev.groupme.com/docs/v3#direct_messages_create]
         */
        suspend operator fun invoke(request: DirectMessageCreateRequest): Response<DirectMessageCreateResponse>

        @Serializable
        data class DirectMessageCreateRequest(
            val direct_message: DirectMessageCreateRequestMessage
        ) {
            @Serializable
            data class DirectMessageCreateRequestMessage(
                val source_guid: String,
                val recipient_id: String,
                val text: String,
                val attachments: List<Attachment>? = null
            )
        }

        @Serializable
        data class DirectMessageCreateResponse(
            val direct_message: DirectMessage
        )
    }


    val messages: MessagesApi

    interface MessagesApi {

        operator fun get(conversation_id: String): WithConversationIdApi

        interface WithConversationIdApi {

            operator fun get(message_id: String): WithMessageIdApi

            interface WithMessageIdApi {

                /**
                 * [https://dev.groupme.com/docs/v3#likes_create]
                 */
                suspend fun like(): Response<Nothing>

                /**
                 * [https://dev.groupme.com/docs/v3#likes_destroy]
                 */
                suspend fun unlike(): Response<Nothing>
            }
        }
    }


    /**
     * [https://dev.groupme.com/docs/v3#bots]
     */
    val bots: BotsApi

    interface BotsApi {

        @Serializable
        data class Bot(
            val bot_id: String,
            val group_id: String,
            val name: String,
            val avatar_url: String,
            val callback_url: String,
            val dm_notification: Boolean
        )

        /**
         * [https://dev.groupme.com/docs/v3#bots_create]
         */
        suspend operator fun invoke(request: BotCreateRequest): Response<Bot>

        @Serializable
        data class BotCreateRequest(
            val bot: BotCreateRequestBot
        ) {
            @Serializable
            data class BotCreateRequestBot(
                val name: String,
                val group_id: String,
                val avatar_url: String? = null,
                val callback_url: String? = null,
                val dm_notification: Boolean? = null
            )
        }

        /**
         * [https://dev.groupme.com/docs/v3#bots_post]
         */
        suspend fun post(request: BotPostRequest): Response<Nothing>

        @Serializable
        data class BotPostRequest(
            val bot_id: String,
            val text: String,
            val picture_url: String? = null
        )

        /**
         * [https://dev.groupme.com/docs/v3#bots_index]
         */
        suspend operator fun invoke(): Response<List<Bot>>

        /**
         * [https://dev.groupme.com/docs/v3#bots_destroy]
         */
        suspend fun destroy(request: BotDestroyRequest): Response<Nothing>

        @Serializable
        data class BotDestroyRequest(
            val bot_id: String
        )
    }


    /**
     * [https://dev.groupme.com/docs/v3#users]
     */
    val users: UsersApi

    interface UsersApi {

        @Serializable
        data class User(
            val id: String,
            val phone_number: String,
            val image_url: String,
            val name: String,
            val created_at: Long,
            val updated_at: Long,
            val email: String,
            val sms: Boolean
        )

        /**
         * [https://dev.groupme.com/docs/v3#users_me]
         */
        suspend fun me(): Response<User>

        /**
         * [https://dev.groupme.com/docs/v3#users_update]
         */
        suspend fun update(request: UserUpdateRequest): Response<User>

        @Serializable
        data class UserUpdateRequest(
            val avatar_url: String? = null,
            val name: String? = null,
            val email: String? = null,
            val zip_code: String? = null
        )

        /**
         * [https://dev.groupme.com/docs/v3#sms_mode]
         */
        val sms_mode: SmsModeApi

        interface SmsModeApi {

            /**
             * [https://dev.groupme.com/docs/v3#sms_mode_create]
             */
            suspend operator fun invoke(request: SmsModeCreateRequest): Response<Nothing>

            @Serializable
            data class SmsModeCreateRequest(
                val duration: Int,
                val registration_id: String? = null
            )

            /**
             * [https://dev.groupme.com/docs/v3#sms_mode_delete]
             */
            suspend fun delete(): Response<Nothing>
        }
    }


    /**
     * [https://dev.groupme.com/docs/v3#blocks]
     */
    val blocks: BlocksApi

    interface BlocksApi {

        @Serializable
        data class Block(
            val user_id: String,
            val blocked_user_id: String,
            val created_at: Long
        )

        /**
         * [https://dev.groupme.com/docs/v3#blocks_index]
         */
        suspend operator fun invoke(user: String): Response<BlocksIndexResponse>

        @Serializable
        data class BlocksIndexResponse(
            val blocks: List<Block>
        )

        /**
         * [https://dev.groupme.com/docs/v3#blocks_between]
         */
        suspend fun between(
            user: String,
            otherUser: String
        ): Response<BlockBetweenResponse>

        @Serializable
        data class BlockBetweenResponse(
            val between: Boolean
        )

        /**
         * [https://dev.groupme.com/docs/v3#blocks_create]
         */
        suspend operator fun invoke(
            user: String,
            otherUser: String
        ): Response<BlockCreateResponse>

        @Serializable
        data class BlockCreateResponse(
            val block: Block
        )

        /**
         * [https://dev.groupme.com/docs/v3#blocks_post_delete]
         */
        suspend fun delete(
            user: String,
            otherUser: String
        ): Response<Nothing>
    }
}