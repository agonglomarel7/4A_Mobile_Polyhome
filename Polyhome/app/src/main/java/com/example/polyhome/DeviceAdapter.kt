import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.graphics.Color
import android.widget.ArrayAdapter
import com.example.polyhome.Device
import com.example.polyhome.R

class DeviceAdapter(
    private val context: Context,
    private val devices: List<Device>,
    private val onCommandClick: (Device, String) -> Unit
) : ArrayAdapter<Device>(context, 0, devices) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_devices, parent, false)

        val device = getItem(position)!!
        val deviceNameTextView = view.findViewById<TextView>(R.id.deviceName)
        val commandButtonsLayout = view.findViewById<LinearLayout>(R.id.commandButtons)

        deviceNameTextView.text = device.id

        commandButtonsLayout.removeAllViews()
        for (command in device.availableCommands) {
            val button = Button(context).apply {
                when (command.lowercase()) {
                    "open", "turn on" -> setBackgroundColor(Color.GREEN) // Vert
                    "close", "turn off" -> setBackgroundColor(Color.RED) // Rouge
                    "stop" -> setBackgroundColor(Color.parseColor("#FFA500")) // Orange
                    else -> setBackgroundColor(Color.GRAY) // Couleur par défaut
                }
            }

            button.text = command

            button.setOnClickListener {
                onCommandClick(device, command)
            }
            commandButtonsLayout.addView(button)
        }
        return view
    }
}
