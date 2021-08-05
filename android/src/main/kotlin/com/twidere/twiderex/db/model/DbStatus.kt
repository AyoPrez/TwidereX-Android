/*
 *  Twidere X
 *
 *  Copyright (C) 2020-2021 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.twiderex.db.model

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.twidere.services.mastodon.model.Card
import com.twidere.services.mastodon.model.Emoji
import com.twidere.services.mastodon.model.Mention
import com.twidere.services.mastodon.model.Poll
import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.model.enums.MastodonStatusType
import com.twidere.twiderex.model.enums.MastodonVisibility
import com.twidere.twiderex.model.enums.PlatformType
import com.twidere.twiderex.model.enums.TwitterReplySettings
import kotlinx.serialization.Serializable

@Entity(
    tableName = "status",
    indices = [Index(value = ["statusKey"], unique = true)],
)
data class DbStatusV2(
    /**
     * Id that being used in the database
     */
    @PrimaryKey
    val _id: String,
    /**
     * Actual tweet/toots id
     */
    val statusId: String,
    val statusKey: MicroBlogKey,
    val htmlText: String,
    val rawText: String,
    val timestamp: Long,
    var retweetCount: Long,
    var likeCount: Long,
    val replyCount: Long,
    val placeString: String?,
    val source: String,
    val hasMedia: Boolean,
    val userKey: MicroBlogKey,
    val lang: String?,
    val is_possibly_sensitive: Boolean,
    val platformType: PlatformType,
    val previewCard: DbPreviewCard? = null,
    val inReplyToUserId: String? = null,
    val inReplyToStatusId: String? = null,
    var extra: Json
)

interface DbStatusExtra

@Immutable
@Serializable
data class DbPreviewCard(
    val link: String,
    val displayLink: String?,
    val title: String?,
    val desc: String?,
    val image: String?,
)

@Immutable
@Serializable
data class DbTwitterStatusExtra(
    val reply_settings: TwitterReplySettings,
    val quoteCount: Long? = null,
) : DbStatusExtra

@Immutable
@Serializable
data class DbMastodonStatusExtra(
    val type: MastodonStatusType,
    val emoji: List<Emoji>,
    val visibility: MastodonVisibility,
    val sensitive: Boolean,
    val spoilerText: String?,
    val poll: Poll?,
    val card: Card?,
    val mentions: List<Mention>?,
) : DbStatusExtra

data class DbStatusWithMediaAndUser(
    @Embedded
    val data: DbStatusV2,
    @Relation(parentColumn = "statusKey", entityColumn = "belongToKey")
    val media: List<DbMedia>,
    @Relation(parentColumn = "userKey", entityColumn = "userKey")
    val user: DbUser,
    @Relation(parentColumn = "statusKey", entityColumn = "statusKey")
    val reactions: List<DbStatusReaction>,
    @Relation(parentColumn = "statusKey", entityColumn = "statusKey")
    val url: List<DbUrlEntity>,
)

suspend fun List<DbStatusWithMediaAndUser>.saveToDb(
    database: CacheDatabase
) {
    map { it.user }.let {
        database.userDao().insertAll(it)
    }
    database.mediaDao().insertAll(flatMap { it.media })
    map { it.data }.let {
        database.statusDao().insertAll(it)
    }
    flatMap { it.url }.let {
        database.urlEntityDao().insertAll(it)
    }
    flatMap { it.reactions }.let {
        database.reactionDao().insertAll(it)
    }
}
