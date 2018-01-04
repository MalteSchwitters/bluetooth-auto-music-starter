package software.kloud.bluetoothmusicstarter

import android.os.Bundle
import android.app.Fragment
import android.provider.Settings
import android.view.*
import kotlinx.android.synthetic.main.fragment_bluetooth_device.*
import software.kloud.fridge_android.app.settings.SettingsManager
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BluetoothDeviceFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BluetoothDeviceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BluetoothDeviceFragment : Fragment(), Observer {

    private var address: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bluetooth_device, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!address.isBlank()) {
            refreshUi()
            // actions
            btnRemove.setOnTouchListener({_, e -> removeSpeaker(e)})
            txtVolume.setOnEditorActionListener { _, _, _ -> updateVolume() }
            SettingsManager.addObserver(this)
        }
    }

    override fun update(o: Observable?, arg: Any?) {
        if (arg == SettingsManager.ACTION_REFRESH_SPEAKERS) {
            refreshUi()
        }
    }

    fun refreshUi() {
        val speaker = SettingsManager.getSpeaker(address)
        lblDeviceName.text = speaker!!.name
        txtVolume.setText(speaker!!.volume.toString())
    }

    fun removeSpeaker(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !address.isBlank()) {
            SettingsManager.removeSpeaker(address)
            val fragTransaction = fragmentManager.beginTransaction()
            fragTransaction.remove(this)
            fragTransaction.commitAllowingStateLoss()
        }
        return false
    }

    private fun updateVolume(): Boolean {
        if (!address.isBlank()) {
            val volume = txtVolume.text.toString().toInt()
            SettingsManager.updateSpeakerVolume(address, volume)
        }
        return false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BluetoothDeviceFragment.
         */
        fun newInstance(address: String): BluetoothDeviceFragment {
            val fragment = BluetoothDeviceFragment()
            fragment.address = address
            return fragment
        }
    }
}
