package com.vdemelo.whatsclone

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.vdemelo.whatsclone.ui.main.SectionsPagerAdapter
import kotlinx.android.synthetic.main.activity_inicio.*

class InicioActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    lateinit var usuarioLogado : DocumentSnapshot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.hide()
        setContentView(R.layout.activity_inicio)

        loadUsuario()

        fab.setOnClickListener { view ->
            iniciarConversa()
        }
    }

    private fun iniciarInterface() {

        pgInicio.visibility = View.INVISIBLE
        fab.visibility = View.VISIBLE

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

    }

    private fun iniciarConversa() {
        TODO("Not yet implemented")
    }

    private fun loadUsuario() {

        this.db.collection("usuarios")
            .document(this.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { resultado ->

                this.usuarioLogado = resultado
                iniciarInterface()

            }
            .addOnFailureListener { excecao ->

                Toast.makeText(
                    this,
                    "Falha ao realizar a operação. Motivo: " + excecao.message,
                    Toast.LENGTH_LONG
                ).show()

            }

    }
}
