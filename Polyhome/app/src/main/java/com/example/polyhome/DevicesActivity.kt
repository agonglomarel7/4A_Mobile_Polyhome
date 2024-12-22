package com.example.polyhome

import DeviceAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class DevicesActivity : AppCompatActivity() {
    private val devices = mutableListOf<Device>()
    private lateinit var deviceAdapter: DeviceAdapter

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        val listView = findViewById<ListView>(R.id.listView)

        deviceAdapter = DeviceAdapter(this, devices) { device, command ->
            Toast.makeText(this, "Commande '$command' pour l'appareil ", Toast.LENGTH_SHORT).show()

            val sharedPreferences = getSharedPreferences("PolyKeyPrefs", MODE_PRIVATE)
            val token = sharedPreferences.getString("Key_Token", null)

            if (token != null) {
                fetchHouseId(token) { houseId ->
                    if (houseId != -1) {

                        sendCommandToDevice(houseId, device.id, command, token)

                    } else {
                        Toast.makeText(this, "ID de la maison introuvable.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Token manquant.", Toast.LENGTH_SHORT).show()
            }
        }

        listView.adapter = deviceAdapter

        val sharedPreferences = getSharedPreferences("PolyKeyPrefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("Key_Token", null)

        if (token != null) {
            fetchHouseId(token) { houseId ->
                if (houseId != -1) {
                    fetchHouseDevices(houseId, token)
                } else {
                    Toast.makeText(this, "House ID not found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchHouseId(token: String, callback: (Int) -> Unit) {
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
                    Log.e("fetchHouseId", "Error: $responseCode")
                    runOnUiThread { callback(-1) }
                }
            } catch (e: Exception) {
                Log.e("fetchHouseId", "Exception: ${e.message}")
                runOnUiThread { callback(-1) }
            }
        }.start()
    }

    private fun fetchHouseDevices(houseId: Int, token: String) {
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
                    val jsonObject = org.json.JSONObject(response)

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
                    runOnUiThread {
                        devices.clear()
                        devices.addAll(fetchedDevices)
                        deviceAdapter.notifyDataSetChanged()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Erreur lors du chargement : $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun sendCommandToDevice(houseId: Int, deviceId: String, command: String, token: String) {
        val url = "https://polyhome.lesmoulinsdudev.com/api/houses/$houseId/devices/$deviceId/command"

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonBody = """{ "command": "$command" }"""
                connection.outputStream.write(jsonBody.toByteArray())

                val responseCode = connection.responseCode
                runOnUiThread {
                    when (responseCode) {
                        200 -> Toast.makeText(this, "Commande exécutée avec succès.", Toast.LENGTH_SHORT).show()
                        403 -> Toast.makeText(this, "Accès interdit : Vous n'êtes pas autorisé.", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(this, "Erreur serveur. Veuillez réessayer plus tard.", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(this, "Erreur inconnue : $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Erreur lors de l'envoi : ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

}
