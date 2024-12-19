import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.polyhome.R

data class User(val userLogin: String)

class UserAdapter(
    private val context: Context,
    private val users: MutableList<User>,
    private val onDeleteUser: (User) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = users.size

    override fun getItem(position: Int): Any = users[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false)
        val user = users[position]

        val txtUserName = view.findViewById<TextView>(R.id.txtUserName)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        txtUserName.text = user.userLogin

        btnDelete.setOnClickListener {
            onDeleteUser(user)
        }

        return view
    }
}

