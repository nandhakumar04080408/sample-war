package com.musicplayer.offline.repository

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.musicplayer.offline.data.MusicDatabase
import com.musicplayer.offline.data.Song
import com.musicplayer.offline.data.Playlist
import com.musicplayer.offline.data.PlaylistSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val database: MusicDatabase,
    private val context: Context
) {
    
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    
    // Song operations
    fun getAllSongs(): Flow<List<Song>> = songDao.getAllSongs()
    
    suspend fun getSongById(id: Long): Song? = songDao.getSongById(id)
    
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)
    
    suspend fun scanMusicFiles() {
        withContext(Dispatchers.IO) {
            val songs = mutableListOf<Song>()
            val contentResolver: ContentResolver = context.contentResolver
            
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )
            
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
            
            val cursor: Cursor? = contentResolver.query(
                uri,
                projection,
                selection,
                null,
                sortOrder
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown"
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val album = it.getString(albumColumn) ?: "Unknown Album"
                    val duration = it.getLong(durationColumn)
                    val data = it.getString(dataColumn)
                    
                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            filePath = data
                        )
                    )
                }
            }
            
            songDao.deleteAllSongs()
            songDao.insertSongs(songs)
        }
    }
    
    // Playlist operations
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    
    suspend fun createPlaylist(name: String): Long {
        val playlist = Playlist(name = name)
        return playlistDao.insertPlaylist(playlist)
    }
    
    suspend fun deletePlaylist(playlist: Playlist) = playlistDao.deletePlaylist(playlist)
    
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        val playlistSong = PlaylistSong(
            playlistId = playlistId,
            songId = songId,
            position = maxPosition + 1
        )
        playlistDao.insertPlaylistSong(playlistSong)
    }
    
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long, position: Int) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        playlistDao.reorderAfterDelete(playlistId, position)
    }
    
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> = playlistDao.getPlaylistSongs(playlistId)
}