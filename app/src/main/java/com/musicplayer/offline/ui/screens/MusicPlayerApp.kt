package com.musicplayer.offline.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.musicplayer.offline.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp(viewModel: MusicViewModel) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            Column {
                // Mini player when song is playing
                val currentSong by viewModel.currentSong.collectAsState()
                if (currentSong != null) {
                    MiniPlayer(
                        viewModel = viewModel,
                        onPlayerClick = {
                            navController.navigate("player") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                
                // Bottom navigation
                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Songs") },
                        label = { Text("Songs") },
                        selected = currentDestination?.hierarchy?.any { it.route == "songs" } == true,
                        onClick = {
                            navController.navigate("songs") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.QueueMusic, contentDescription = "Queue") },
                        label = { Text("Queue") },
                        selected = currentDestination?.hierarchy?.any { it.route == "queue" } == true,
                        onClick = {
                            navController.navigate("queue") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.PlaylistPlay, contentDescription = "Playlists") },
                        label = { Text("Playlists") },
                        selected = currentDestination?.hierarchy?.any { it.route == "playlists" } == true,
                        onClick = {
                            navController.navigate("playlists") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "songs",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("songs") {
                SongsScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = {
                        navController.navigate("player") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable("queue") {
                QueueScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = {
                        navController.navigate("player") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable("playlists") {
                PlaylistsScreen(
                    viewModel = viewModel,
                    onNavigateToPlayer = {
                        navController.navigate("player") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable("player") {
                PlayerScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}