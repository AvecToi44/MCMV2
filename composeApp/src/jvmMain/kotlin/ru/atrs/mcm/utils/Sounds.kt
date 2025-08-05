package ru.atrs.mcm.utils

import ru.atrs.mcm.enums.StateExperiments
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioSystem


fun sound_On() {
    if (SOUND_ENABLED == 0) {
        return
    }
    //return
    val lol = File("raw/tesla_autopilot_on.wav")
//    val a = useResource("raw/tesla_autopilot_on.wav",{
//        lol =
//    })


    try {
        val clip = AudioSystem.getClip()
        clip.open(AudioSystem.getAudioInputStream(
            if (SOUND_ENABLED == 1)
                Dir0Configs_Analysis
            else if (SOUND_ENABLED == 2) Dir0Configs_Run
            else Dir0Configs_Analysis
        ))
        clip.start()
        CoroutineScope(Dispatchers.Unconfined).launch {
            do {
                if (!clip.isRunning) {
                    logGarbage("audio again!")
                    clip.start()
                }
                delay(1000)
            } while (STATE_EXPERIMENT.value != StateExperiments.NONE)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun soundUniversal(file: File) {
    if (SOUND_ENABLED == 0) {
        return
    }
    try {
        val clip = AudioSystem.getClip()
        clip.open(AudioSystem.getAudioInputStream(file))
        clip.start()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun sound_Error() {
    if (SOUND_ENABLED == 0) {
        return
    }
    //val lol = File(Dir0Configs_End)

    try {
        val clip = AudioSystem.getClip()
        clip.open(AudioSystem.getAudioInputStream(Dir0Configs_End))
        clip.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}