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

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.twidere.twiderex.db.CacheDatabase
import com.twidere.twiderex.model.MicroBlogKey
import com.twidere.twiderex.model.enums.ReferenceType
import java.util.UUID

@Entity(
    tableName = "status_reference",
    indices = [
        Index(
            value = [
                "referenceType",
                "statusKey",
                "referenceStatusKey",
            ],
            unique = true,
        ),
    ],
)
data class DbStatusReference(
    /**
     * Id that being used in the database
     */
    @PrimaryKey
    val _id: String,
    val referenceType: ReferenceType,
    val statusKey: MicroBlogKey,
    val referenceStatusKey: MicroBlogKey,
)

data class DbStatusReferenceWithStatus(
    @Embedded
    val reference: DbStatusReference,
    @Relation(
        parentColumn = "referenceStatusKey",
        entityColumn = "statusKey",
        entity = DbStatusV2::class
    )
    val status: DbStatusWithMediaAndUser
)

fun DbStatusWithMediaAndUser?.toDbStatusReference(
    statusKey: MicroBlogKey,
    referenceType: ReferenceType,
): DbStatusReferenceWithStatus? {
    return if (this == null) {
        null
    } else {
        DbStatusReferenceWithStatus(
            reference = DbStatusReference(
                _id = UUID.randomUUID().toString(),
                referenceType = referenceType,
                statusKey = statusKey,
                referenceStatusKey = data.statusKey
            ),
            status = this,
        )
    }
}

data class DbStatusWithReference(
    @Embedded
    val status: DbStatusWithMediaAndUser,
    @Relation(
        parentColumn = "statusKey",
        entityColumn = "statusKey",
        entity = DbStatusReference::class
    )
    val references: List<DbStatusReferenceWithStatus>,
)

suspend fun List<DbStatusWithReference>.saveToDb(
    database: CacheDatabase
) {
    this.map { it.references.map { it.status } + it.status }
        .flatten()
        .saveToDb(database = database)
    this.flatMap { it.references }.map { it.reference }.let {
        database.statusReferenceDao().insertAll(it)
    }
}
