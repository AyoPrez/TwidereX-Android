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
package com.twidere.twiderex.jobs.compose

import android.content.Context
import com.twidere.services.twitter.TwitterService
import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.db.mapper.toDbStatusWithReference
import com.twidere.twiderex.db.model.saveToDb
import com.twidere.twiderex.kmp.ExifScrambler
import com.twidere.twiderex.kmp.FileResolver
import com.twidere.twiderex.kmp.RemoteNavigator
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.model.job.ComposeData
import com.twidere.twiderex.model.transform.toUi
import com.twidere.twiderex.model.ui.UiStatus
import com.twidere.twiderex.notification.AppNotificationManager
import com.twidere.twiderex.repository.AccountRepository
import com.twidere.twiderex.repository.StatusRepository
import com.twidere.twiderex.viewmodel.compose.ComposeType

class TwitterComposeJob constructor(
    context: Context,
    accountRepository: AccountRepository,
    notificationManager: AppNotificationManager,
    exifScrambler: ExifScrambler,
    remoteNavigator: RemoteNavigator,
    private val statusRepository: StatusRepository,
    private val fileResolver: FileResolver,
    private val cacheDatabase: CacheDatabase,
) : ComposeJob<TwitterService>(
    context,
    accountRepository,
    notificationManager,
    exifScrambler,
    remoteNavigator
) {
    override suspend fun compose(
        service: TwitterService,
        composeData: ComposeData,
        accountKey: MicroBlogKey,
        mediaIds: ArrayList<String>
    ): UiStatus {
        val lat = composeData.lat
        val long = composeData.long
        val content = composeData.content.let {
            if (composeData.composeType == ComposeType.Quote && composeData.statusKey != null) {
                val status = statusRepository.loadFromCache(
                    composeData.statusKey,
                    accountKey = accountKey
                )
                it + " ${status?.generateShareLink()}"
            } else {
                it
            }
        }
        val result = service.update(
            content,
            media_ids = mediaIds,
            in_reply_to_status_id = if (composeData.composeType == ComposeType.Reply || composeData.composeType == ComposeType.Thread) composeData.statusKey?.id else null,
            repost_status_id = if (composeData.composeType == ComposeType.Quote) composeData.statusKey?.id else null,
            lat = lat,
            long = long,
            exclude_reply_user_ids = composeData.excludedReplyUserIds
        ).toDbStatusWithReference(accountKey)
        listOf(result).saveToDb(cacheDatabase)
        return result.toUi(accountKey)
    }

    override suspend fun uploadImage(
        originUri: String,
        scramblerUri: String,
        service: TwitterService
    ): String {
        val type = fileResolver.getMimeType(originUri)
        val size = fileResolver.getFileSize(scramblerUri)
        return fileResolver.openInputStream(scramblerUri)?.use {
            service.uploadFile(
                it,
                type ?: "image/*",
                size ?: it.available().toLong()
            )
        } ?: throw Error()
    }
}
