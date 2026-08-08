package com.daycountapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daycountapp.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE isHidden = 0 AND isDeleted = 0 AND isPreview = 0 ORDER BY isPinned DESC, sortOrder ASC, targetDate ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isHidden = 0 AND isDeleted = 0 AND isPreview = 0 ORDER BY isPinned DESC, sortOrder ASC, targetDate ASC")
    fun getVisible(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isHidden = 1 AND isDeleted = 0 AND isPreview = 0 ORDER BY createTime DESC")
    fun getHidden(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isCountUp = 0 AND isHidden = 0 AND isDeleted = 0 AND isPreview = 0 ORDER BY isPinned DESC, sortOrder ASC, targetDate ASC")
    fun getCountdownEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isCountUp = 1 AND isHidden = 0 AND isDeleted = 0 AND isPreview = 0 ORDER BY isPinned DESC, sortOrder ASC, targetDate ASC")
    fun getCountUpEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE isDeleted = 1 ORDER BY deleteTime DESC")
    fun getDeletedEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): Event?

    @Query("SELECT * FROM events WHERE isPreview = 1 LIMIT 1")
    suspend fun getPreviewEvent(): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("UPDATE events SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(
        id: Long,
        isPinned: Boolean,
    )

    @Query("UPDATE events SET isDeleted = 1, deleteTime = :deleteTime WHERE id = :id")
    suspend fun softDelete(
        id: Long,
        deleteTime: Long,
    )

    @Query("UPDATE events SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreEvent(id: Long)

    @Query("UPDATE events SET isHidden = 0 WHERE id = :id")
    suspend fun unhideEvent(id: Long)

    @Query("UPDATE events SET isHidden = 0 WHERE isHidden = 1")
    suspend fun unhideAllEvents()

    @Query("SELECT MAX(id) FROM events")
    suspend fun getMaxId(): Long?

    // 拖拽排序相关方法
    @androidx.room.Transaction
    suspend fun updateSortOrders(events: List<Event>) {
        events.forEach { event ->
            updateSortOrder(event.id, event.sortOrder)
        }
    }

    @Query("UPDATE events SET sortOrder = :sortOrder WHERE id = :eventId")
    suspend fun updateSortOrder(eventId: Long, sortOrder: Int)
}
