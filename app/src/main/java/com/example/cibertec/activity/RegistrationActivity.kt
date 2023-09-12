package com.example.cibertec.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.cibertec.R
import com.example.cibertec.utility.ConnectionManager
import org.json.JSONObject

class RegistrationActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPwd: EditText
    private lateinit var etConfirmPwd: EditText
    private lateinit var btnRegister: Button
    private var emailPattern = Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    lateinit var sharedPrefs: SharedPreferences
    private lateinit var toolBar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        etName = findViewById(R.id.etRegName)
        etEmail = findViewById(R.id.etRegEmail)
        etPhone = findViewById(R.id.etRegPhone)
        etAddress = findViewById(R.id.etRegAddress)
        etPwd = findViewById(R.id.etRegPwd)
        etConfirmPwd = findViewById(R.id.etRegConfirmPwd)
        btnRegister = findViewById(R.id.btnRegister)
        toolBar = findViewById(R.id.toolbarRegistration)

        sharedPrefs = getSharedPreferences(
            getString(R.string.login_preference_file_name),
            Context.MODE_PRIVATE
        )
        setUpToolbar()
        btnRegister.setOnClickListener {

            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val phn = "9"+etPhone.text.toString()
            var altpnh = etPhone.text.toString()
            val address = etAddress.text.toString()
            val pwd = etPwd.text.toString()
            val confirmPwd = etConfirmPwd.text.toString()
            var key = 1

            if ((name == "") || (email == "") || (phn == "") || (address == "") || (pwd == "") || (confirmPwd == "")) {
                Toast.makeText(
                    this@RegistrationActivity,
                    "todos los campos son requeridos",
                    Toast.LENGTH_SHORT
                ).show()
                key = 0
            }
            if (phn.length != 10) {
                key = 0
                Toast.makeText(
                    this@RegistrationActivity,
                    "telefono de nueve digitos please",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (name.length < 3) {
                key = 0
                Toast.makeText(
                    this@RegistrationActivity,
                    "3 caracteres minimos",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (pwd.length < 4 || confirmPwd.length < 4) {
                key = 0
                Toast.makeText(
                    this@RegistrationActivity,
                    "4 caracteres minimos",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (pwd != confirmPwd) {
                key = 0
                Toast.makeText(
                    this@RegistrationActivity,
                    "por favor que la contraseña y el comfimar contraseña coincidan",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (!email.trim().matches(emailPattern)) {
                key = 0
                Toast.makeText(
                    this@RegistrationActivity,
                    "Email invalido",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (key == 1) {
                val queue = Volley.newRequestQueue(this)
                val url = "http://13.235.250.119/v2/register/fetch_result"
                val jsonParams = JSONObject()
                jsonParams.put("name", name)
                jsonParams.put("mobile_number", phn)
                jsonParams.put("password", pwd)
                jsonParams.put("address", address)
                jsonParams.put("email", email)

                if (ConnectionManager().checkConnectivity(this)) {
                    val jsonRequest = object : JsonObjectRequest(
                        Method.POST,
                        url,
                        jsonParams,
                        Response.Listener {
                            try {
                                val item = it.getJSONObject("data")
                                val success = item.getBoolean("success")
                                if (success) {

                                    val userInfoJSONObject = item.getJSONObject("data")

                                    altpnh = userInfoJSONObject.getString("mobile_number");
                                    sharedPrefs.edit().putString(
                                        "user_id",
                                        userInfoJSONObject.getString("user_id")
                                    ).apply()
                                    sharedPrefs.edit()
                                        .putString(
                                            "user_name",
                                            userInfoJSONObject.getString("name")
                                        )
                                        .apply()
                                    sharedPrefs.edit().putString(
                                        "user_email",
                                        userInfoJSONObject.getString("email")
                                    ).apply()
                                    sharedPrefs.edit().putString(
                                        "user_mobile_number",
                                        userInfoJSONObject.getString("mobile_number")
                                    ).apply()
                                    sharedPrefs.edit().putString(
                                        "user_address",
                                        userInfoJSONObject.getString("address")
                                    ).apply()

                                    startActivity(
                                        Intent(
                                            this@RegistrationActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Ha ocurrido un error al enviar ",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this,
                                    "Ha ocurrido un error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        Response.ErrorListener {
                            Toast.makeText(
                                this,
                                "Ha ocurrido un error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "a959329b58247a"
                            return headers
                        }
                    }
                    queue.add(jsonRequest)
                } else {
                    val dialog = AlertDialog.Builder(this)
                    dialog.setTitle("Error")
                    dialog.setMessage("Errod con el internet")
                    dialog.setPositiveButton("Close") { _, _ ->
                        ActivityCompat.finishAffinity(this)
                    }
                    dialog.create()
                    dialog.show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolBar)
        supportActionBar?.title = "Registrate"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}