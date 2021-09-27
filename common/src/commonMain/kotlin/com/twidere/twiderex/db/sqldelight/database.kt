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
package com.twidere.twiderex.db.sqldelight

import com.squareup.sqldelight.db.SqlDriver
import com.twidere.twiderex.db.sqldelight.adapter.DMConversationAdapterFactory
import com.twidere.twiderex.db.sqldelight.adapter.DMEventAdapterFactory
import com.twidere.twiderex.db.sqldelight.adapter.DraftAdapterFactory
import com.twidere.twiderex.db.sqldelight.adapter.MediaAdapterFactory
import com.twidere.twiderex.db.sqldelight.adapter.SearchAdapterFactory
import com.twidere.twiderex.db.sqldelight.adapter.UrlEntityAdapterFactory
import com.twidere.twiderex.db.sqldelight.adapter.UserAdapterFactory
import com.twidere.twiderex.sqldelight.SqlDelightAppDatabase
import com.twidere.twiderex.sqldelight.SqlDelightCacheDatabase

internal fun createAppDataBase(driver: SqlDriver): SqlDelightAppDatabase {
    SqlDelightAppDatabase.Schema.create(driver)
    return SqlDelightAppDatabase(
        driver = driver,
        draftAdapter = DraftAdapterFactory.create(),
        searchAdapter = SearchAdapterFactory.create()
    )
}

internal fun createCacheDataBase(driver: SqlDriver): SqlDelightCacheDatabase {
    SqlDelightCacheDatabase.Schema.create(driver)
    return SqlDelightCacheDatabase(
        driver = driver,
        DMEventAdapter = DMEventAdapterFactory.create(),
        MediaAdapter = MediaAdapterFactory.create(),
        UrlEntityAdapter = UrlEntityAdapterFactory.create(),
        UserAdapter = UserAdapterFactory.create(),
        DMConversationAdapter = DMConversationAdapterFactory.create()
    )
}
