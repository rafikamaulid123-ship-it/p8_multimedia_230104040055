package id.antasari.p8_multimedia_230104040055.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.antasari.p8_multimedia_230104040055.ui.gallery.CameraGalleryScreen
import id.antasari.p8_multimedia_230104040055.ui.home.HomeScreen
import id.antasari.p8_multimedia_230104040055.ui.player.AudioPlayerScreen
import id.antasari.p8_multimedia_230104040055.ui.recorder.AudioRecorderScreen
import id.antasari.p8_multimedia_230104040055.ui.video.VideoPlayerScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGallery = {
                    navController.navigate(Screen.Gallery.route)
                },
                onNavigateToAudioPlayer = {
                    navController.navigate(Screen.AudioPlayer.route)
                },
                onNavigateToAudioRecorder = {
                    navController.navigate(Screen.AudioRecorder.route)
                },
                onNavigateToVideoPlayer = {
                    navController.navigate(Screen.VideoPlayer.route)
                }
            )
        }
        
        composable(Screen.Gallery.route) {
            CameraGalleryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AudioPlayer.route) {
            AudioPlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AudioRecorder.route) {
            AudioRecorderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.VideoPlayer.route) {
            VideoPlayerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
