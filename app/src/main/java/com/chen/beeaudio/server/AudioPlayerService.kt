package com.chen.beeaudio.server

import snow.player.PlayerService
import snow.player.annotation.PersistenceId

@PersistenceId("AudioPlayerService")
class AudioPlayerService : PlayerService() {
}