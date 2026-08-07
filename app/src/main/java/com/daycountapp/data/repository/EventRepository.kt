package com.daycountapp.data.repository

import com.daycountapp.data.local.EventDao
import com.daycountapp.data.model.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao,
) {
    val allEvents: Flow<List<Event>> = eventDao.getAllEvents()

    val visibleEvents: Flow<List<Event>> = eventDao.getVisible()

    val hiddenEvents: Flow<List<Event>> = eventDao.getHidden()

    val countdownEvents: Flow<List<Event>> = eventDao.getCountdownEvents()

    val countUpEvents: Flow<List<Event>> = eventDao.getCountUpEvents()

    val deletedEvents: Flow<List<Event>> = eventDao.getDeletedEvents()

    suspend fun getEventById(id: Long): Event? = eventDao.getEventById(id)

    suspend fun insertEvent(event: Event): Long = eventDao.insertEvent(event)

    suspend fun insertAll(events: List<Event>) = eventDao.insertAll(events)

    suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun deleteEventById(id: Long) {
        eventDao.deleteEventById(id)
    }

    suspend fun deleteAll() {
        eventDao.deleteAll()
    }

    suspend fun togglePin(
        id: Long,
        isPinned: Boolean,
    ) {
        eventDao.togglePin(id, isPinned)
    }

    suspend fun getMaxId(): Long? = eventDao.getMaxId()

    suspend fun hideEvent(event: Event) {
        eventDao.updateEvent(event.copy(isHidden = true))
    }

    suspend fun unhideEvent(event: Event) {
        eventDao.unhideEvent(event.id)
    }

    suspend fun unhideAllEvents() {
        eventDao.unhideAllEvents()
    }

    suspend fun softDeleteEvent(event: Event) {
        eventDao.softDelete(event.id, System.currentTimeMillis())
    }

    suspend fun restoreEvent(event: Event) {
        eventDao.restoreEvent(event.id)
    }

    suspend fun permanentDeleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    suspend fun importOverwrite(events: List<Event>) {
        eventDao.deleteAll()
        eventDao.insertAll(events)
    }

    suspend fun importAppend(events: List<Event>) {
        val maxId = eventDao.getMaxId() ?: 0
        val newEvents = events.map { it.copy(id = it.id + maxId + 1) }
        eventDao.insertAll(newEvents)
    }

    // ==================== 预览事件管理 ====================

    suspend fun getPreviewEvent(): Event? = eventDao.getPreviewEvent()

    suspend fun getOrCreatePreviewEvent(): Event {
        val existing = eventDao.getPreviewEvent()
        if (existing != null) return existing

        // 创建默认预览事件
        val previewEvent = Event(
            title = "DayCount来到你身边",
            description = "这是一段描述QwQ",
            targetDate = System.currentTimeMillis(),
            isCountUp = true,
            isPreview = true,
            colorPreset = 0,
        )
        val id = eventDao.insertEvent(previewEvent)
        return previewEvent.copy(id = id)
    }

    suspend fun updatePreviewEvent(event: Event) {
        eventDao.updateEvent(event.copy(isPreview = true))
    }
}
