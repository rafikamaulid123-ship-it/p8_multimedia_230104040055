package id.antasari.p8_multimedia_230104040055.ui

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Multimedia App")
    object Gallery : Screen("gallery", "Gallery & Camera")
    object AudioPlayer : Screen("audio_player", "Audio Player")
    object AudioRecorder : Screen("audio_recorder", "Audio Recorder")
    object VideoPlayer : Screen("video_player", "Video Player")
}
