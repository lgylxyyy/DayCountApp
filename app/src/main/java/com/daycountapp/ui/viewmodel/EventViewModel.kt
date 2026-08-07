package com.daycountapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.daycountapp.data.model.Event
import com.daycountapp.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventViewModel(
    private val repository: EventRepository,
) : ViewModel() {
    val allEvents: StateFlow<List<Event>> =
        repository.allEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visibleEvents: StateFlow<List<Event>> =
        repository.visibleEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenEvents: StateFlow<List<Event>> =
        repository.hiddenEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val countdownEvents: StateFlow<List<Event>> =
        repository.countdownEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val countUpEvents: StateFlow<List<Event>> =
        repository.countUpEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedEvents: StateFlow<List<Event>> =
        repository.deletedEvents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _eventDetail = MutableStateFlow<Event?>(null)
    val eventDetail: StateFlow<Event?> = _eventDetail

    fun loadEventById(id: Long) {
        viewModelScope.launch {
            _eventDetail.value = repository.getEventById(id)
        }
    }

    fun insertEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun deleteEventById(id: Long) {
        viewModelScope.launch {
            repository.deleteEventById(id)
        }
    }

    fun togglePin(
        id: Long,
        isPinned: Boolean,
    ) {
        viewModelScope.launch {
            repository.togglePin(id, isPinned)
        }
    }

    fun hideEvent(event: Event) {
        viewModelScope.launch {
            repository.hideEvent(event)
        }
    }

    fun unhideEvent(event: Event) {
        viewModelScope.launch {
            repository.unhideEvent(event)
        }
    }

    fun unhideAllEvents() {
        viewModelScope.launch {
            repository.unhideAllEvents()
        }
    }

    fun softDeleteEvent(event: Event) {
        viewModelScope.launch {
            repository.softDeleteEvent(event)
        }
    }

    fun restoreEvent(event: Event) {
        viewModelScope.launch {
            repository.restoreEvent(event)
        }
    }

    fun permanentDeleteEvent(event: Event) {
        viewModelScope.launch {
            repository.permanentDeleteEvent(event)
        }
    }

    fun importOverwrite(events: List<Event>) {
        viewModelScope.launch {
            repository.importOverwrite(events)
        }
    }

    fun importAppend(events: List<Event>) {
        viewModelScope.launch {
            repository.importAppend(events)
        }
    }

    class Factory(
        private val repository: EventRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EventViewModel(repository) as T
    }
}
