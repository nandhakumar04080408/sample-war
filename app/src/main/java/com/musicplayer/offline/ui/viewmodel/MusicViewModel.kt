package com.musicplayer.offline.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicplayer.offline.data.Song
import com.musicplayer.offline.data.Playlist
import com.musicplayer.offline.player.MusicPlayerController
import com.musicplayer.offline.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playerController: MusicPlayerController
) : ViewModel() {
    
    val songs = repository.getAllSongs()
    val playlists = repository.getAllPlaylists()
    
    val currentSong = playerController.currentSong
    val isPlaying = playerController.isPlaying
    val playQueue = playerController.playQueue
    val currentQueueIndex = playerController.currentQueueIndex
    val shuffleMode = playerController.shuffleMode
    val repeatMode = playerController.repeatMode
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Song>>(emptyList())
    val searchResults: StateFlow<List<Song>> = _searchResults.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedSongs = MutableStateFlow<Set<Long>>(emptySet())
    val selectedSongs: StateFlow<Set<Long>> = _selectedSongs.asStateFlow()
    
    init {
        refreshMusicLibrary()
    }
    
    fun refreshMusicLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.scanMusicFiles()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchSongs(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            repository.searchSongs(query).collect { results ->
                _searchResults.value = results
            }
        }
    }
    
    fun playSong(song: Song) {
        playerController.playSong(song)
    }
    
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        playerController.playQueue(songs, startIndex)
    }
    
    fun addToQueue(song: Song) {
        playerController.addToQueue(song)
    }
    
    fun playNext(song: Song) {
        playerController.playNext(song)
    }
    
    fun removeFromQueue(index: Int) {
        playerController.removeFromQueue(index)
    }
    
    fun playPause() {
        playerController.playPause()
    }
    
    fun seekToNext() {
        playerController.seekToNext()
    }
    
    fun seekToPrevious() {
        playerController.seekToPrevious()
    }
    
    fun toggleShuffle() {
        playerController.toggleShuffle()
    }
    
    fun toggleRepeat() {
        playerController.toggleRepeat()
    }
    
    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        playerController.moveQueueItem(fromIndex, toIndex)
    }
    
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
    
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }
    
    fun getPlaylistSongs(playlistId: Long) = repository.getPlaylistSongs(playlistId)
    
    fun toggleSongSelection(songId: Long) {
        val currentSelection = _selectedSongs.value.toMutableSet()
        if (currentSelection.contains(songId)) {
            currentSelection.remove(songId)
        } else {
            currentSelection.add(songId)
        }
        _selectedSongs.value = currentSelection
    }
    
    fun clearSelection() {
        _selectedSongs.value = emptySet()
    }
    
    fun addSelectedSongsToQueue() {
        // Implementation depends on getting songs by IDs
        clearSelection()
    }
    
    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}