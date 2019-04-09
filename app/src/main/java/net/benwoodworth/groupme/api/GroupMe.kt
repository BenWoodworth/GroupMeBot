package net.benwoodworth.groupme.api

class GroupMe(
    val accessToken: String
) : GroupMeApiV3 {

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
