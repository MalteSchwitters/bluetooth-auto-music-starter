package software.kloud.bluetoothmusicstarter

const val DEVIDER = " | "

class BluetoothSpeaker {

    var name: String? = null
    var address: String? = null
    var volume: Int = 0

    override fun toString(): String {
        return address + DEVIDER + name + DEVIDER + volume
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        return address == (other as BluetoothSpeaker).address
    }

    override fun hashCode(): Int {
        return address?.hashCode() ?: 0
    }


    companion object {
        fun fromString(string: String): BluetoothSpeaker {
            val speaker = BluetoothSpeaker()
            val fields = string.split(DEVIDER)
            speaker.address = fields[0]
            speaker.name = fields[1]
            speaker.volume = fields[2].toInt()
            return speaker
        }
    }
}