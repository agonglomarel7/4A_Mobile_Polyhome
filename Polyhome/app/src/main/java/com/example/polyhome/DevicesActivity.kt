package com.example.polyhome

import DeviceAdapter
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class DevicesActivity : AppCompatActivity() {

    // Liste locale des appareils
    private val devices = mutableListOf<Device>()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices) // Assurez-vous que ce fichier existe

        // Référencement de la ListView
        val listView = findViewById<ListView>(R.id.listView)

        // Initialisation de l'adaptateur
        deviceAdapter = DeviceAdapter(this, devices) { _, _ ->
            // Pour l'instant, les commandes ne sont pas encore utilisées
        }

        // Associez l'adaptateur à la ListView
        listView.adapter = deviceAdapter

        // Récupération du token
        val sharedPreferences = getSharedPreferences("PolyHomePrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token != null) {
            getHouseId(token) { houseId ->
                if (houseId != -1) {
                    getDevices(houseId, token)
                } else {
                    Toast.makeText(this, "House ID not found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getHouseId(token: String, callback: (Int) -> Unit) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val inputStream = connection.inputStream.bufferedReader().use { it.readText() }
                    val houses = JSONArray(inputStream)

                    for (i in 0 until houses.length()) {
                        val house = houses.getJSONObject(i)
                        val isOwner = house.getBoolean("owner")
                        if (isOwner) {
                            val houseId = house.getInt("houseId")
                            runOnUiThread { callback(houseId) }
                            return@Thread
                        }
                    }

                    runOnUiThread { callback(-1) }
                } else {
                    Log.e("getHouseId", "Error: $responseCode")
                    runOnUiThread { callback(-1) }
                }
            } catch (e: Exception) {
                Log.e("getHouseId", "Exception: ${e.message}")
                runOnUiThread { callback(-1) }
            }
        }.start()
    }

    private fun getDevices(houseId: Int, token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = org.json.JSONObject(response) // On parse d'abord l'objet JSON

                    // Récupérer le tableau des appareils à partir de la clé "devices"
                    val jsonArray = jsonObject.getJSONArray("devices")

                    val fetchedDevices = mutableListOf<Device>()
                    for (i in 0 until jsonArray.length()) {
                        val jsonDevice = jsonArray.getJSONObject(i)
                        val id = jsonDevice.getString("id")
                        val type = jsonDevice.getString("type")
                        val availableCommands = mutableListOf<String>()

                        val commandsArray = jsonDevice.getJSONArray("availableCommands")
                        for (j in 0 until commandsArray.length()) {
                            availableCommands.add(commandsArray.getString(j))
                        }

                        fetchedDevices.add(Device(id, type, availableCommands))
                    }

                    // Mettre à jour l'adaptateur avec les données
                    runOnUiThread {
                        devices.clear()
                        devices.addAll(fetchedDevices)
                        deviceAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.e("getDevices", "Erreur : Code $responseCode")
                    runOnUiThread {
                        Toast.makeText(this, "Erreur lors du chargement : $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("getDevices", "Exception : ${e.message}")
                runOnUiThread {
                    Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

}
