package com.example.cibertec.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.example.cibertec.R
import com.example.cibertec.adapter.RestaurantDetailsRecyclerAdapter
import com.example.cibertec.fragments.*
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var toolbar: Toolbar
    private lateinit var frameLayout: FrameLayout
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private var previousMenuItem: MenuItem? = null
    private lateinit var navPhone: TextView
    private lateinit var navName: TextView

    companion object {
        var key: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frame)
        sharedPreferences =
            getSharedPreferences(
                getString(R.string.login_preference_file_name),
                Context.MODE_PRIVATE
            )
        navigationView = findViewById(R.id.navigationView)
        val headerView =
            navigationView.inflateHeaderView(R.layout.drawer_header)
        headerView.findViewById<View>(R.id.navigationHeader)
        navName = headerView.findViewById(R.id.txtNavigationName)
        navPhone = headerView.findViewById(R.id.txtNavigationPhone)
        navName.text = sharedPreferences.getString("user_name", "user_name")
        navPhone.text = sharedPreferences.getString("user_mobile_number", "user_mobile_number")

        setUpToolbar()
        openHome()

        when (supportFragmentManager.findFragmentById(R.id.frame)) {
            !is RestaurantDetailsFragment -> {
                val actionBarDrawerToggle = ActionBarDrawerToggle(
                    this@MainActivity, drawerLayout,
                    R.string.open_drawer,
                    R.string.close_drawer
                )
                drawerLayout.addDrawerListener(actionBarDrawerToggle)
                actionBarDrawerToggle.syncState()
            }
        }

        navigationView.setNavigationItemSelectedListener {

            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it
            when (it.itemId) {

                R.id.home -> {
                    openHome()
                }
                R.id.myProfile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            MyProfileFragment()
                        )
                        .commit()
                    supportActionBar?.title = "Mi perfil"
                    drawerLayout.closeDrawers()
                }
                R.id.favouriteRestaurants -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FavouriteRestaurantsFragment()
                        )
                        .commit()
                    supportActionBar?.title = "Restaurantes favoritos"
                    drawerLayout.closeDrawers()
                }
                R.id.orderHistory -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            OrderHistoryFragment()
                        )
                        .commit()
                    supportActionBar?.title = "Mis pedidos anteriores"
                    drawerLayout.closeDrawers()
                }
                R.id.faq -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FAQFragment()
                        )
                        .commit()
                    supportActionBar?.title = "Frequently Asked Questions"
                    drawerLayout.closeDrawers()
                }
                R.id.logOut -> {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmacion")
                    builder.setMessage("¿Estás segura de que quieres salir?")
                    builder.setCancelable(false)
                    builder.setPositiveButton("Yes") { _, _ ->
                        sharedPreferences.edit().clear().apply()
                        startActivity(Intent(this@MainActivity, LogIn::class.java))
                        ActivityCompat.finishAffinity(this@MainActivity)
                    }
                    builder.setNegativeButton("No") { _, _ ->
                        openHome()
                    }
                    builder.create().show()
                    drawerLayout.closeDrawers()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        when (supportFragmentManager.findFragmentById(R.id.frame)) {
            is RestaurantDetailsFragment -> supportActionBar?.setIcon(R.drawable.ic_back)
        }
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            when (supportFragmentManager.findFragmentById(R.id.frame)) {
                is RestaurantDetailsFragment -> {
                    onBackPressed()
                }
                else -> drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openHome() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                HomeFragment()
            )
            .commit()
        key = 0
        supportActionBar?.title = "Todos los restaurantes"
        navigationView.setCheckedItem(R.id.home)
        drawerLayout.closeDrawers()
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frame)) {
            is RestaurantDetailsFragment -> {
                if (
                    !RestaurantDetailsRecyclerAdapter.isCartEmpty) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmación")
                    builder.setMessage("Volver atrás restablecerá los artículos del carrito. ¿Todavía quieres continuar?")
                    builder.setCancelable(false)
                    builder.setPositiveButton("Si") { _, _ ->
                        toolbar.title = "Todos los restaurantes"
                        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                        val transaction =
                            (this as FragmentActivity).supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frame, HomeFragment())
                        RestaurantDetailsRecyclerAdapter.isCartEmpty = true
                        transaction.commit()
                    }
                    builder.setNegativeButton("No") { _, _ ->
                    }
                    builder.create().show()
                } else {
                    openHome()
                    supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
                }
            }
            !is HomeFragment -> openHome()
            is HomeFragment -> {
                if (key == 1) {
                    key = 0
                    openHome()
                } else {
                    super.onBackPressed()
                }
            }
            else -> super.onBackPressed()
        }
    }
}