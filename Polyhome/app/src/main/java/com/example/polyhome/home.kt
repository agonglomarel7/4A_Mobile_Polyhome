package com.example.polyhome

import User
import UserAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class home : AppCompatActivity() {

    private val listhome : ArrayList<ListHomeData> =ArrayList()

    private lateinit var listHomeAdapter: ArrayAdapter<ListHomeData>

    private val users: ArrayList<addUserData> = ArrayList()

    private lateinit var spinHome : Spinner

    private lateinit var userAdapter: UserAdapter
    private lateinit var listViewUsers: ListView
    private lateinit var txtUserInput:EditText
    private lateinit var btnAddUser:Button
    private val Users = mutableListOf<User>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        spinHome = findViewById(R.id.spinHome)
        txtUserInput = findViewById(R.id.lblAddUser)
        btnAddUser = findViewById(R.id.btnaddUser)
        listViewUsers = findViewById(R.id.listViewUsers)


        // Appliquer les marges pour les barres système
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Trouver le bouton
        val btnDevices = findViewById<Button>(R.id.btnGoToDevices)

        // Configuration de la Toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        // Gérer le clic
        btnDevices.setOnClickListener {
            val intent =  Intent(this, DevicesActivity::class.java)
            startActivity(intent)
        }

        listHomeAdapter = ArrayAdapter(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            listhome
        )

        // Initialize UserAdapter
        userAdapter = UserAdapter(this, Users) { user ->
           deleteUserFromHouse(user)
        }
        listViewUsers.adapter = userAdapter

        // Charger la liste des utilisateurs au démarrage
        val sharedPreferences = getSharedPreferences("PolyHomePrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token != null) {
           getUsersFromHouse(126, token) // Remplacez 126 par l'ID approprié de la maison
        }


        // Récupérer le nom d'utilisateur
        val username = intent.getStringExtra("USERNAME") ?: "Utilisateur inconnu"
        Log.d("HomeActivity", "Nom d'utilisateur reçu : $username")

        // Mettre à jour le TextView
        val listUserNameTextView = findViewById<TextView>(R.id.textUsername)
        listUserNameTextView.text = username

        // Configuration du bouton et champ texte
        val btnAddUser = findViewById<Button>(R.id.btnaddUser)
        val txtUserInput = findViewById<EditText>(R.id.lblAddUser)

        btnAddUser.setOnClickListener {
            val userLogin = txtUserInput.text.toString().trim()
            if (userLogin.isNotBlank()) {
                val sharedPreferences = getSharedPreferences("PolyHomePrefs", MODE_PRIVATE)
                val token = sharedPreferences.getString("TOKEN", null)

                if (token != null) {
                    // Appel pour récupérer dynamiquement le houseId
                    getHouseId(token) { houseId ->
                        if (houseId != -1) {
                            addUserToHouse(houseId, token, userLogin)
                        } else {
                            Toast.makeText(this, "Aucune maison trouvée pour cet utilisateur.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Token manquant, veuillez vous reconnecter.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Veuillez entrer un nom d'utilisateur.", Toast.LENGTH_SHORT).show()
            }
        }


        // appel des fun d'API
        initializeSpinners()
        loadHome()
    }

    // Charger le menu dans la Toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Gérer les clics des éléments du menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                handleLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initializeSpinners() {
        spinHome.adapter = listHomeAdapter
    }

    private fun handleLogout() {
        // Supprimez les préférences utilisateur
        val sharedPreferences = getSharedPreferences("PolyHomePrefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Redirigez l'utilisateur vers l'écran de connexion
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Message de confirmation
        Toast.makeText(this, "Vous êtes déconnecté.", Toast.LENGTH_SHORT).show()
    }

    private fun loadHome() {

        val sharedPreferences = getSharedPreferences("PolyHomePrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(
                this,
                "Erreur : Token introuvable. Veuillez vous reconnecter.",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            Log.d("HomeActivity", "Token utilisé pour l'API : $token")
            Api().get<List<ListHomeData>>(
                "https://polyhome.lesmoulinsdudev.com/api/houses",
                onSuccess = { responseCode, loadedHomes ->
                    if (responseCode == 200 && loadedHomes != null) {
                        loadHomeSuccess(responseCode, loadedHomes)
                        Log.d("HomeActivity", "Maisons reçues de l'API : $loadedHomes")
                    } else {
                        handleError(responseCode)
                    }
                },
                token
            )
        }
    }


    private fun loadHomeSuccess (responseCode: Int, loadedHome:List<ListHomeData>?){
        runOnUiThread {
            if (responseCode == 200 && loadedHome != null) {

                listhome.addAll(loadedHome)
                listHomeAdapter.notifyDataSetChanged() // Notifie l'adaptateur des nouvelles données
            }
        }
    }

    private fun handleError(responseCode: Int) {
        val message = when (responseCode) {
            400 -> "Requête incorrecte."
            403 -> "Accès interdit. Veuillez vous reconnecter."
            500 -> "Erreur serveur. Réessayez plus tard."
            else -> "Erreur inconnue. Code : $responseCode"
        }

        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e("HomeActivity", "Erreur API : Code $responseCode")
        }
    }

    private fun getHouseId(token: String, callback: (Int) -> Unit) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses"

        Thread {
            try {
                // Appel GET vers l'API
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
                    val houses = JSONArray(inputStream)

                    // Parcours des maisons pour trouver celle où "owner" = true
                    for (i in 0 until houses.length()) {
                        val house = houses.getJSONObject(i)
                        val isOwner = house.getBoolean("owner")
                        if (isOwner) {
                            val houseId = house.getInt("houseId")

                            // Sauvegarde dans SharedPreferences pour réutilisation
                            val sharedPreferences = getSharedPreferences("PolyHomePrefs", MODE_PRIVATE)
                            with(sharedPreferences.edit()) {
                                putInt("HOUSE_ID", houseId)
                                apply()
                            }

                            // Retourne le houseId via le callback
                            runOnUiThread {
                                callback(houseId)
                            }
                            return@Thread
                        }
                    }
                    // Aucun houseId trouvé
                    runOnUiThread { callback(-1) }

                } else {
                    Log.e("getHouseId", "Erreur API : $responseCode")
                    runOnUiThread { callback(-1) }
                }
            } catch (e: Exception) {
                Log.e("getHouseId", "Exception : ${e.message}")
                runOnUiThread { callback(-1) }
            }
        }.start()
    }


    private fun addUserToHouse(houseId: Int, token: String, userLogin: String) {

        if (users.any { it.userLogin == userLogin }) {
            Toast.makeText(this, "Utilisateur déjà ajouté : $userLogin", Toast.LENGTH_SHORT).show()
            Log.d("MainActivity", "Tentative d'ajout d'un utilisateur existant : $userLogin")
            return
        }

        Api().post(
            "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
            addUserData(userLogin),
            onSuccess = { responseCode ->
                runOnUiThread {
                    when (responseCode) {
                        200 -> {
                            users.add(addUserData(userLogin))

                            val txtUserInput = findViewById<EditText>(R.id.lblAddUser)
                            txtUserInput.text.clear()

                            Toast.makeText(this, "Utilisateur ajouté avec succès : $userLogin", Toast.LENGTH_SHORT).show()
                            Log.d("MainActivity", "Utilisateur ajouté : $userLogin")

                            getUsersFromHouse(houseId, token)
                        }
                        403 -> {
                            Toast.makeText(this, "Accès interdit : Token invalide ou non propriétaire.", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "Erreur 403 : Accès interdit.")
                        }
                        500 -> {
                            Toast.makeText(this, "Erreur serveur. Veuillez réessayer plus tard.", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "Erreur 500 : Erreur serveur.")
                        }
                        else -> {
                            Toast.makeText(this, "Erreur inconnue. Code : $responseCode", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "Erreur inconnue. Code : $responseCode")
                        }
                    }
                }
            },
            securityToken = token
        )
    }


    private fun getUsersFromHouse(houseId: Int, token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users"

        Api().get<List<User>>(
            url,
            onSuccess = { responseCode, users ->
                runOnUiThread {
                    if (responseCode == 200 && users != null) {
                        // Rafraîchit la liste locale
                        Users.clear()
                        Users.addAll(users)

                        // Met à jour l'adaptateur de la ListView
                        userAdapter.notifyDataSetChanged()

                        Toast.makeText(this, "Liste des utilisateurs mise à jour.", Toast.LENGTH_SHORT).show()
                    } else {
                        handleApiError(responseCode)
                    }
                }
            },
            securityToken = token
        )
    }

    private fun handleApiError(responseCode: Int) {
        val errorMessage = when (responseCode) {
            400 -> "Données incorrectes."
            403 -> "Accès interdit : Token invalide ou non autorisé."
            500 -> "Erreur serveur. Veuillez réessayer plus tard."
            else -> "Erreur inconnue : Code $responseCode"
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        Log.e("HomeActivity", "Erreur API : $responseCode")
    }

    private fun deleteUserFromHouse(user: User) {
        val houseId = 126
        val sharedPreferences = getSharedPreferences("PolyHomePrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token != null) {
            // Supprimer localement l'utilisateur avant l'appel API
            Users.remove(user)
            userAdapter.notifyDataSetChanged()

            Api().delete(
                "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/users",
                onSuccess = { responseCode ->
                    runOnUiThread {
                        if(responseCode ==200 ){
                            Toast.makeText(this, "Utilisateur supprimé : ${user.userLogin}", Toast.LENGTH_SHORT).show()

                            getUsersFromHouse(houseId, token)
                        }
                        else if(responseCode == 403 ){
                            Toast.makeText(this, "Accès interdit : Token invalide ou non propriétaire.", Toast.LENGTH_SHORT).show()
                        }
                        else if(responseCode == 500 ){
                            Toast.makeText(this, "Erreur serveur. Veuillez réessayer plus tard.", Toast.LENGTH_SHORT).show()
                        }
                        else  {
                                Toast.makeText(this, "Erreur inconnue : $responseCode", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                securityToken = token
            )
        } else {
            Toast.makeText(this, "Token manquant. Impossible de supprimer.", Toast.LENGTH_SHORT).show()
        }
    }

}