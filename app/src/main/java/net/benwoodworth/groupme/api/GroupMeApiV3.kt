package net.benwoodworth.groupme.api

import kotlinx.serialization.Serializable

/**
 * [https://dev.groupme.com/docs/v3]
 */
interface GroupMeApiV3 {

    /**
     * [https://dev.groupme.com/docs/v3#v3]
     */
    @Serializable
    data class Response<T : Any>(
        val response: T?,
        val meta: Meta? = null
    ) {

        @Serializable
        data class Meta(
            val code: Int,
            val errors: List<String>? = null
        )
    }

    @Serializable
    data class Group(
        val id: String,
        val name: String,
        val type: String,
        val description: String,
        val image_url: String,
        val creator_user_id: String,
        val created_at: Long,
        val updated_at: Long,
        val members: List<Member>?,
        val share_url: String,
        val messages: Messages
    ) {

        @Serializable
        data class Messages(
            val count: Int,
            val last_message_id: String,
            val last_message_creates_at: String,
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
        val image_url: String,
        val autokicked: Boolean,
        val app_installed: Boolean,
        val roles: List<String>,
        val state: String
    )

    @Serializable
    data class Attachment(
        val type: String,
        val url: String? = null,
        val lat: Float? = null,
        val lng: Float? = null,
        val name: String? = null,
        val token: String? = null,
        val placeholder: String? = null,
        val charmap: List<List<Int>>? = null,
        val user_ids: List<String>?
    )

    val groups: GroupsApi

    interface GroupsApi {

        @Serializable
        data class GroupMessage(
            val id: String,
            val source_guid: String,
            val created_at: Long,
            val user_id: String,
            val group_id: String,
            val name: String,
            val avatar_url: String,
            val text: String,
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
        suspend fun create(
            name: String,
            description: String? = null,
            image_url: String? = null,
            share: Boolean? = null
        ): Response<Group>

        operator fun get(id: String): GroupApi

        interface GroupApi {

            /**
             * [https://dev.groupme.com/docs/v3#groups_show]
             */
            suspend operator fun invoke(): Response<List<Group>>

            /**
             * [https://dev.groupme.com/docs/v3#groups_update]
             */
            suspend fun update(
                name: String? = null,
                description: String? = null,
                image_url: String? = null,
                office_mode: Boolean? = null,
                share: Boolean? = null
            ): Response<Group>

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
                    suspend operator fun invoke(): Response<JoinResponse>
                }

                @Serializable
                data class JoinResponse(
                    val group: Group
                )
            }

            val members: MembersApi

            interface MembersApi {

                /**
                 * [https://dev.groupme.com/docs/v3#members_add]
                 */
                suspend fun add(request: AddRequest): Response<AddResponse>

                @Serializable
                data class AddRequest(
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
                data class AddResponse(
                    val results_id: String
                )

                operator fun get(membershipId: String): MemberApi

                interface MemberApi {

                    /**
                     * [https://dev.groupme.com/docs/v3#members_remove]
                     */
                    suspend fun remove(): Response<Nothing>

                    /**
                     * [https://dev.groupme.com/docs/v3#members_update]
                     */
                    suspend fun update(request: UpdateRequest): Response<Member>

                    @Serializable
                    data class UpdateRequest(
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
                        suspend operator fun invoke(): Response<ResultsResponse>

                        @Serializable
                        data class ResultsResponse(
                            val messages: List<GroupMessage>
                        )
                    }
                }
            }

            val messages: GroupMessagesApi

            interface GroupMessagesApi {

                /**
                 * [https://dev.groupme.com/docs/v3#messages]
                 */
                suspend fun invoke(
                    before_id: String? = null,
                    since_id: String? = null,
                    after_id: String? = null,
                    limit: Int? = null
                ): Response<IndexResponse>

                @Serializable
                data class IndexResponse(
                    val count: Int,
                    val messages: List<GroupMessage>
                )

                /**
                 * [https://dev.groupme.com/docs/v3#messages_create]
                 */
                suspend fun invoke(
                    request: CreateRequest
                ): Response<CreateResponse>

                @Serializable
                data class CreateRequest(
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
                data class CreateResponse(
                    val message: GroupMessage
                )
            }
        }
    }

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
        suspend operator fun invoke(
            request: CreateRequest
        ): Response<CreateResponse>

        @Serializable
        data class CreateRequest(
            val direct_message: CreateRequestMessage
        ) {

            @Serializable
            data class CreateRequestMessage(
                val source_guid: String,
                val recipient_id: String,
                val text: String,
                val attachments: List<Attachment>? = null
            )
        }

        @Serializable
        data class  CreateResponse(
            val direct_message: DirectMessage
        )
    }
}