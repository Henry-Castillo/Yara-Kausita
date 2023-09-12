package com.example.cibertec.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.cibertec.R
import com.example.cibertec.utility.ConnectionManager
import com.example.cibertec.utility.Validations
import org.json.JSONObject

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etForgotMobile: EditText
    private lateinit var etForgotEmail: EditText
    private lateinit var btnForgotNext: Button
    lateinit var rlContentMain: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        etForgotMobile = findViewById(R.id.etForgotMobileNumber)
        etForgotEmail = findViewById(R.id.etForgotEmail)
        btnForgotNext = findViewById(R.id.btnForgotNext)
        rlContentMain = findViewById(R.id.rlForgot)
        rlContentMain.visibility = View.VISIBLE

        btnForgotNext.setOnClickListener {
            val forgotMobileNumber = etForgotMobile.text.toString()
            if (Validations.validateMobile(forgotMobileNumber)) {
                etForgotMobile.error = null
                if (Validations.validateEmail(etForgotEmail.text.toString())) {
                    if (ConnectionManager().checkConnectivity(this@ForgotPasswordActivity)) {
                        rlContentMain.visibility = View.GONE
                        sendOTP(etForgotMobile.text.toString(), etForgotEmail.text.toString())
                    } else {
                        rlContentMain.visibility = View.VISIBLE
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "Sin coneccion a internet",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    rlContentMain.visibility = View.VISIBLE
                    etForgotEmail.error = "Email Invalido "
                }
            } else {
                rlContentMain.visibility = View.VISIBLE
                etForgotMobile.error = "Numero de telefono Invalido"
            }
        }
    }

    private fun sendOTP(mobileNumber: String, email: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

        val jsonParams = JSONObject()
        jsonParams.put("mobile_number", "9$mobileNumber")
        jsonParams.put("email", email)
        val jsonObjectRequest =
            object : JsonObjectRequest(Method.POST, url, jsonParams, Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val firstTry = data.getBoolean("first_try")
                        if (firstTry) {
                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Information")
                            builder.setMessage( "Por favor revise su correo electrónico registrado para la atenticacion.")
                            builder.setCancelable(false)
                            builder.setPositiveButton("Ok") { _, _ ->
                                val intent = Intent(
                                    this@ForgotPasswordActivity,
                                    ResetPassword::class.java
                                )
                                intent.putExtra("mobileNumber", mobileNumber)
                                startActivity(intent)
                            }
                            builder.create().show()
                        } else {
                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Information")
                            builder.setMessage("Consulte el correo electrónico anterior para la OTP")
                            builder.setCancelable(false)
                            builder.setPositiveButton("Ok") { _, _ ->
                                val intent = Intent(
                                    this@ForgotPasswordActivity,
                                    ResetPassword::class.java
                                )
                                intent.putExtra("mobileNumber", mobileNumber)
                                startActivity(intent)
                            }
                            builder.create().show()
                        }
                    } else {
                        rlContentMain.visibility = View.VISIBLE
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            "\n" +
                                    "¡Número de móvil no registrado!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    rlContentMain.visibility = View.VISIBLE
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "¡Error de respuesta incorrecta!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }, Response.ErrorListener {
                rlContentMain.visibility = View.VISIBLE
                VolleyLog.e("Error::::", "/post request fallo Error: ${it.message}")
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    "Ha ocurrido un error",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "a959329b58247a"
                    return headers
                }
            }
        queue.add(jsonObjectRequest)
    }

    override fun onBackPressed() {
        startActivity(
            Intent(
                this@ForgotPasswordActivity,
                LogIn::class.java
            )
        )
        super.onBackPressed()
    }
}