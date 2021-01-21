 package com.vdemelo.whatsclone.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.vdemelo.whatsclone.InicioActivity
import com.vdemelo.whatsclone.MainActivity
import com.vdemelo.whatsclone.R
import com.vdemelo.whatsclone.VisualizarImagemActivity
import kotlinx.android.synthetic.main.fragment_perfil.*
import kotlinx.android.synthetic.main.fragment_perfil.view.*

 class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewPerfil = inflater.inflate(R.layout.fragment_perfil, container, false)

        carregaImagemUsuario(viewPerfil)
        carregaInfoUsuario(viewPerfil)

        viewPerfil.cvVisualizarFoto.setOnClickListener {
            visualizarFoto()
        }

        viewPerfil.btnSair.setOnClickListener {
            sair()
        }

        viewPerfil.btnExcluir.setOnClickListener {
            excluirConta()
        }


        return viewPerfil
    }

     private fun excluirConta() {

     }

     private fun sair() {
         FirebaseAuth.getInstance().signOut()
         activity?.startActivity(Intent(activity, MainActivity::class.java))
         activity?.finish()
     }

     private fun visualizarFoto() {

         val intent = Intent(activity, VisualizarImagemActivity::class.java)
         intent.putExtra("TITLE", "Foto de perfil")
         intent.putExtra("URL", (activity as InicioActivity).usuarioLogado.getString("foto"))

         activity?.startActivity(intent)

     }

     private fun carregaImagemUsuario(view: View?) {

         Picasso.get()
             .load((activity as InicioActivity).usuarioLogado.getString("foto"))
             .placeholder(R.drawable.profile_default)
             .error(R.drawable.profile_default)
             .into(view?.imgPerfil)

     }

     private fun carregaInfoUsuario(view : View?) {

        view?.tvNome?.text = (activity as InicioActivity).usuarioLogado.getString("nome")
        view?.tvEmail?.text = (activity as InicioActivity).usuarioLogado.getString("email")

    }

    companion object {
        @JvmStatic
        fun newInstance() = PerfilFragment()
    }
}
