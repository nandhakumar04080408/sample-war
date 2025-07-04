package com.musicplayer.offline.player

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicplayer.offline.data.Song
import com.musicplayer.offline.service.MusicPlaybackService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicPlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaController: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _playQueue = MutableStateFlow<List<Song>>(emptyList())
    val playQueue: StateFlow<List<Song>> = _playQueue.asStateFlow()
    
    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()
    
    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    init {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(PlayerListener())
        }, MoreExecutors.directExecutor())
    }
    
    fun playSong(song: Song) {
        val mediaItem = createMediaItem(song)
        mediaController?.let {
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
            _currentSong.value = song
        }
    }
    
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        
        val mediaItems = songs.map { createMediaItem(it) }
        mediaController?.let {
            it.setMediaItems(mediaItems, startIndex, 0)
            it.prepare()
            it.play()
            _playQueue.value = songs
            _currentQueueIndex.value = startIndex
            _currentSong.value = songs[startIndex]
        }
    }
    
    fun addToQueue(song: Song) {
        val currentQueue = _playQueue.value.toMutableList()
        currentQueue.add(song)
        _playQueue.value = currentQueue
        
        val mediaItem = createMediaItem(song)
        mediaController?.addMediaItem(mediaItem)
    }
    
    fun playNext(song: Song) {
        val currentQueue = _playQueue.value.toMutableList()
        val insertIndex = _currentQueueIndex.value + 1
        currentQueue.add(insertIndex, song)
        _playQueue.value = currentQueue
        
        val mediaItem = createMediaItem(song)
        mediaController?.addMediaItem(insertIndex, mediaItem)
    }
    
    fun removeFromQueue(index: Int) {
        if (index >= 0 && index < _playQueue.value.size) {
            val currentQueue = _playQueue.value.toMutableList()
            currentQueue.removeAt(index)
            _playQueue.value = currentQueue
            mediaController?.removeMediaItem(index)
        }
    }
    
    fun play() {
        mediaController?.play()
    }
    
    fun pause() {
        mediaController?.pause()
    }
    
    fun playPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }
    
    fun seekToNext() {
        mediaController?.seekToNext()
    }
    
    fun seekToPrevious() {
        mediaController?.seekToPrevious()
    }
    
    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }
    
    fun toggleShuffle() {
        val newMode = !_shuffleMode.value
        _shuffleMode.value = newMode
        mediaController?.shuffleModeEnabled = newMode
    }
    
    fun toggleRepeat() {
        val newMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = newMode
        
        val playerRepeatMode = when (newMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
        mediaController?.repeatMode = playerRepeatMode
    }
    
    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val currentQueue = _playQueue.value.toMutableList()
        if (fromIndex >= 0 && fromIndex < currentQueue.size && 
            toIndex >= 0 && toIndex < currentQueue.size) {
            val item = currentQueue.removeAt(fromIndex)
            currentQueue.add(toIndex, item)
            _playQueue.value = currentQueue
            mediaController?.moveMediaItem(fromIndex, toIndex)
        }
    }
    
    private fun createMediaItem(song: Song): MediaItem {
        return MediaItem.Builder()
            .setUri(song.filePath.toUri())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .build()
            )
            .build()
    }
    
    private inner class PlayerListener : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val currentIndex = mediaController?.currentMediaItemIndex ?: 0
            _currentQueueIndex.value = currentIndex
            
            if (currentIndex < _playQueue.value.size) {
                _currentSong.value = _playQueue.value[currentIndex]
            }
        }
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_ENDED -> {
                    // If shuffle is enabled and we've reached the end, shuffle and restart
                    if (_shuffleMode.value && _repeatMode.value == RepeatMode.ALL) {
                        val shuffledQueue = _playQueue.value.shuffled()
                        playQueue(shuffledQueue, 0)
                    }
                }
            }
        }
    }
    
    fun release() {
        MediaController.releaseFuture(controllerFuture)
    }
}

enum class RepeatMode {
    OFF, ONE, ALL
}