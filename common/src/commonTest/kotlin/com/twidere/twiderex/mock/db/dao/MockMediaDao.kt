/*
 *  Twidere X
 *
 *  Copyright (C) TwidereProject and Contributors
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
package com.twidere.twiderex.mock.db.dao

import com.twidere.twiderex.db.dao.MediaDao
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.model.ui.UiMedia
import org.jetbrains.annotations.TestOnly

internal class MockMediaDao @TestOnly constructor() : MediaDao {
    private var initData: List<UiMedia> = emptyList()
    fun initData(initData: List<UiMedia>) {
        this.initData = initData
    }
    override suspend fun findMediaByBelongToKey(belongToKey: MicroBlogKey): List<UiMedia> {
        return initData.filter {
            it.belongToKey == belongToKey
        }
    }
}
