/*
 *  Twidere X
 *
 *  Copyright (C) 2020 Tlaster <tlaster@outlook.com>
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
package com.twidere.twiderex.utils

import android.content.Context
import android.net.Uri
import com.twidere.services.twitter.TwitterService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ComposeQueue(
    private val context: Context,
) {
    fun commit(
        service: TwitterService,
        content: String,
        images: List<Uri>,
        replyTo: String? = null,
        quoteTo: String? = null,
    ) {
        GlobalScope.launch {
            val mediaIds = arrayListOf<String>()
            images.forEach { uri ->
                val contentResolver = context.contentResolver
                val type = contentResolver.getType(uri)
                val size = contentResolver.openFileDescriptor(uri, "r")?.statSize
                val id = contentResolver.openInputStream(uri)?.use {
                    service.uploadFile(it, type ?: "image/*", size ?: it.available().toLong())
                } ?: throw Error()
                mediaIds.add(id)
            }
            service.update(
                content,
                media_ids = mediaIds,
                in_reply_to_status_id = replyTo,
                repost_status_id = quoteTo,
            )
        }
    }
}
