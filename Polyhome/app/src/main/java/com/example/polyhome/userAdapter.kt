import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.polyhome.R

data class User(val userLogin: String,val owner: Boolean)

class UserAdapter(
    private val context: Context,
    private val users: List<User>,
    private val onDeleteClick: (User) -> Unit
) : ArrayAdapter<User>(context, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false)

        val user = users[position]

        val txtUserName = view.findViewById<TextView>(R.id.txtUserName)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        txtUserName.text = user.userLogin

        if (user.owner) {
            btnDelete.isEnabled = false
            btnDelete.text = "Owner"
            btnDelete.textSize = 12f
        } else {
            btnDelete.isEnabled = true
            btnDelete.text = "-"
            btnDelete.setOnClickListener { onDeleteClick(user) }
        }

        return view
    }
}

