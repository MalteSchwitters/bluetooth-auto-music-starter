package software.kloud.fridge_android.app.settings

import android.content.SharedPreferences
import android.util.Log
import software.kloud.bluetoothmusicstarter.BluetoothSpeaker
import java.util.*

/**
 * Created by malte on 11/5/17.
 */


object SettingsManager : Observable() {

    const val PROPERTY_PLAYLIST = "playlist"
    const val PROPERTY_DEVICES = "devices"
    const val ACTION_REFRESH_SPEAKERS = "refresh speakers"
    const val ACTION_ADD_SPEAKER = "add speakers"

    private lateinit var preferences: SharedPreferences
    var playlist: String = ""
    var connectedDevice: BluetoothSpeaker? = null
    var devices = ArrayList<BluetoothSpeaker>()

    fun init(preferences: SharedPreferences) {
        this.preferences = preferences
    }

    fun saveSettings() {
        Log.d(javaClass.simpleName, "Saving app settings")
        val set = HashSet<String>()
        for(device in devices) {
            set.add(device.toString())
        }
        preferences.edit()
                .putString(PROPERTY_PLAYLIST, playlist)
                .putStringSet(PROPERTY_DEVICES, set)
                .apply()
    }

    fun loadSettings() {
        Log.d(javaClass.simpleName, "Loading app settings")
        playlist = preferences.getString(PROPERTY_PLAYLIST, "")
        val set = preferences.getStringSet(PROPERTY_DEVICES, HashSet<String>())
        devices.clear()
        for(device in set) {
            devices.add(BluetoothSpeaker.fromString(device))
        }
    }

    fun getSpeaker(address: String): BluetoothSpeaker? {
        val find = BluetoothSpeaker()
        find.address = address
        val index = devices.indexOf(find)
        if (index >= 0) {
            return devices[devices.indexOf(find)]
        }
        return null
    }

    fun saveConnectedSpeaker() : Boolean {
        if (!devices.contains(connectedDevice)) {
            devices.add(connectedDevice!!)
            setChanged()
            notifyObservers(ACTION_ADD_SPEAKER)
            return true
        }
        return false
    }

    fun removeSpeaker(address: String) : Boolean {
        // triggered by ui, dont notify observers
        val speaker = BluetoothSpeaker()
        speaker.address = address
        return devices.remove(speaker)
    }

    fun updateSpeakerVolume(address: String, volume: Int) {
        for(device in devices) {
            if (device.address == address) {
                device.volume = volume
                setChanged()
                notifyObservers(ACTION_REFRESH_SPEAKERS)
                return
            }
        }
    }
}