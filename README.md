# Offline Music Player

A zero-budget, feature-rich offline music player for Android that addresses all your music listening needs without requiring a premium subscription service.

## Features ✨

### Core Features
- **📱 Offline Music Playback**: Play your downloaded MP3 files without internet connection
- **🎵 Play Queue Management**: Add songs to queue and manage playback order
- **⏭️ Play Next**: Insert songs to play immediately after the current track
- **🔀 Smart Shuffle**: Automatic random playback when queue ends
- **🔁 Repeat Modes**: Off, Repeat All, Repeat One
- **🎧 Background Playback**: Continues playing even when screen is off
- **🔍 Smart Search**: Search by song title, artist, or album

### User Experience
- **📋 Playlist Management**: Create and organize custom playlists
- **🎨 Modern UI**: Beautiful Material Design 3 interface
- **🌙 Dark/Light Theme**: Automatic theme based on system preference
- **🎛️ Easy Controls**: Intuitive playback controls and navigation
- **📊 Mini Player**: Quick access controls at the bottom of the screen

### Technical Features
- **🔄 Auto Music Scanning**: Automatically discovers music files on your device
- **💾 Local Database**: Fast access to your music library
- **🔊 Media Session**: Integration with system media controls
- **⚡ Background Service**: Efficient background music playback
- **📱 Android 13+ Support**: Modern Android permissions and features

## How It Works 🚀

1. **Grant Permissions**: The app will request storage permissions to access your music files
2. **Automatic Scanning**: Your music library will be automatically scanned and organized
3. **Browse & Search**: Use the Songs tab to browse or search your music collection
4. **Play Music**: Tap any song to start playing, or use "Play All" for full library playback
5. **Manage Queue**: Use the Queue tab to see what's playing next and reorder songs
6. **Create Playlists**: Organize your favorite songs into custom playlists
7. **Enjoy**: Music continues playing in the background, even when the screen is off!

## Installation 📦

1. Build the project using Android Studio
2. Install on your Android device (API level 26+)
3. Grant storage permissions when prompted
4. Enjoy your music!

## Key Features Explained 🎯

### Play Queue System
- **Add to Queue**: Add songs to the end of the current queue
- **Play Next**: Insert songs to play immediately after the current track
- **Queue Management**: View, reorder, and remove songs from the queue
- **Auto-Shuffle**: When the queue ends, automatically shuffles all songs and continues playing

### Background Playback
- Uses Android MediaSession for proper background playback
- Continues playing when screen is off or app is in background
- System notification with playback controls
- Integration with system media controls (headphone buttons, lock screen, etc.)

### Smart Music Discovery
- Automatically scans your device for music files
- Supports MP3 and other common audio formats
- Extracts metadata (title, artist, album, duration)
- Refreshable library for new music files

## Built With 🛠️

- **Kotlin**: Modern Android development language
- **Jetpack Compose**: Modern UI toolkit
- **Media3**: Advanced media playback and control
- **Room Database**: Local data persistence
- **Hilt**: Dependency injection
- **Material Design 3**: Modern design system
- **Navigation Component**: App navigation

## Requirements 📋

- Android 8.0 (API level 26) or higher
- Storage permission for accessing music files
- Music files stored on device storage

## Zero Budget Solution 💰

This app is completely free and open-source, providing all the features you need without:
- ❌ Subscription fees
- ❌ Ads
- ❌ In-app purchases
- ❌ Internet connection requirements
- ❌ Data usage for streaming

Perfect alternative to premium music streaming services when you already have your music collection downloaded!

## License 📄

This project is open source and available under the MIT License.