package com.example.find_my_phone

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log

import androidx.core.app.NotificationCompat
import com.example.find_my_phone.R
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.concurrent.scheduleAtFixedRate


var TAG = "services"
const val INTENT_COMMAND = "Command"
const val INTENT_COMMAND_EXIT = "Exit"


private const val NOTIFICATION_CHANNEL_GENERAL = "Checking"
private const val CODE_FOREGROUND_SERVICE = 1
private lateinit var mediaPlayer: MediaPlayer

var isDone:Boolean = false

var modelPath = "ml_model.tflite"
// defining the minimum threshold
var probabilityThreshold: Float = 0.8f
class services : Service() {

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val command = intent.getStringExtra(INTENT_COMMAND)
        Log.d(TAG, "onStartCommand: started ")
        if (command == INTENT_COMMAND_EXIT) {
            stopService()
            return START_NOT_STICKY
        }
        showNotification()
        val classifier = AudioClassifier.createFromFile(this, modelPath)
        //  Creating an audio recorder
        val tensor = classifier.createInputTensorAudio()
        //  showing the audio recorder specification
        val format = classifier.requiredTensorAudioFormat
        val record = classifier.createAudioRecord()
        record.startRecording()
        Timer().scheduleAtFixedRate(1, 500)
        {
//            loading  latest data  from audiorecord

            val numberOfSamples = tensor.load(record)
            val output = classifier.classify(tensor)
            val filteredModelOutput = output[0].categories.filter {
                (it.score > probabilityThreshold)&&(it.label != "0 Background Noise")
            }
            val outputStr =
                filteredModelOutput.sortedBy { -it.score }
                    .joinToString(separator = "\n") { "${it.label} -> ${it.score} " }

            if (isDone == true){
                mediaPlayer = MediaPlayer()
                mediaPlayer.setOnCompletionListener { mp ->
                    mp.stop()
                    mp.reset()
                    mp.release()
                }
                try {
                    mediaPlayer = MediaPlayer.create(this@services, R.raw.beep_04)
                    mediaPlayer.start()
                    isDone = false
                }
                catch (e:IOException) {
                    Log.e(TAG, "error reading from file while preparing MediaPlayer$e")
                }
                catch (e: IllegalArgumentException) {
                    Log.e(TAG, "illegal argument given $e")
                }
            }
            if (outputStr.isNotEmpty()){
                isDone = true
            }
        }
        return START_NOT_STICKY
    }
    private fun stopService() {
        this.stopForeground(true)
        this.stopSelf()
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun showNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        "ForeGround Service Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(true)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    manager.createNotificationChannel(this)
                }
            } catch (e: Exception) {
                Log.d("Error", "showNotification: ${e.localizedMessage}")
            }
        }

        with(
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_GENERAL)
        ) {

            setAutoCancel(true)
            setOngoing(true)
            setWhen(System.currentTimeMillis())
            priority = NotificationManager.IMPORTANCE_HIGH
            startForeground(CODE_FOREGROUND_SERVICE, build())

        }
    }
}
