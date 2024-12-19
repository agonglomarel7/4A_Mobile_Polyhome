import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.BaseAdapter
import android.widget.LinearLayout
import com.example.polyhome.Device
import com.example.polyhome.R

class DeviceAdapter(
    private val context: Context,
    private val devices: List<Device>,
    private val onCommandClicked: (String, String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = devices.size

    override fun getItem(position: Int): Any = devices[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_devices, parent, false)

        val device = devices[position]
        val deviceName = view.findViewById<TextView>(R.id.deviceName)
        val commandButtons = view.findViewById<LinearLayout>(R.id.commandButtons)

        deviceName.text = device.id

        commandButtons.removeAllViews()

        // Dynamically add buttons for each command
        for (command in device.availableCommands) {
            val button = Button(context).apply {
                text = command
                setOnClickListener {
                    onCommandClicked(device.id, command)
                }
            }
            commandButtons.addView(button)
        }

        return view
    }
}
