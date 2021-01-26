 package com.vdemelo.whatsclone.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.vdemelo.whatsclone.InicioActivity
import com.vdemelo.whatsclone.MainActivity
import com.vdemelo.whatsclone.R
import com.vdemelo.whatsclone.VisualizarImagemActivity
import kotlinx.android.synthetic.main.activity_cadastro.*
import kotlinx.android.synthetic.main.bottom_sheet_alterar_foto.view.*
import kotlinx.android.synthetic.main.bottom_sheet_edit_name.view.*
import kotlinx.android.synthetic.main.bottom_sheet_edit_name.view.btnSalvar
import kotlinx.android.synthetic.main.bottom_sheet_edit_name.view.btnCancelar
import kotlinx.android.synthetic.main.bottom_sheet_edit_password.view.*
import kotlinx.android.synthetic.main.fragment_perfil.*
import kotlinx.android.synthetic.main.fragment_perfil.view.*

 class PerfilFragment : Fragment() {

     val REQUISICAO_FOTO_GALERIA = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewPerfil = inflater.inflate(R.layout.fragment_perfil, container, false)

        carregaImagemUsuario(viewPerfil)
        carregaInfoUsuario(viewPerfil)

        (activity as InicioActivity).db.collection("usuarios")
            .document((activity as InicioActivity).auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->

                carregaImagemUsuario(viewPerfil)
                carregaInfoUsuario(viewPerfil)

            }

        viewPerfil.cvVisualizarFoto.setOnClickListener {
            visualizarFoto()
        }

        viewPerfil.imgBtnEditarNome.setOnClickListener {
            editarNomeUsuario()
        }

        viewPerfil.imgBtnEditarSenha.setOnClickListener {
            editarSenhaUsuario()
        }

        viewPerfil.cvAlterarFoto.setOnClickListener {
            alterarFoto()
        }

        viewPerfil.btnSair.setOnClickListener {
            sair()
        }

        viewPerfil.btnExcluir.setOnClickListener {
            excluirConta()
        }


        return viewPerfil
    }

     private fun alterarFoto() {

         val btnSheetLayout = layoutInflater.inflate(R.layout.bottom_sheet_alterar_foto, null)
         val dialog = BottomSheetDialog(this.requireContext(), R.style.BottomSheetStyle)

         dialog.setContentView(btnSheetLayout)

         btnSheetLayout.imgBtnGaleria.setOnClickListener {

             val intent = Intent()
             intent.type = "image/*"
             intent.action = Intent.ACTION_GET_CONTENT
             startActivityForResult(intent, REQUISICAO_FOTO_GALERIA)
             dialog.dismiss()

         }

         dialog.show()

     }

     private fun editarSenhaUsuario() {

         val btnSheetLayout = layoutInflater.inflate(R.layout.bottom_sheet_edit_password, null)
         val dialog = BottomSheetDialog(this.requireContext(), R.style.BottomSheetStyle)

         dialog.setContentView(btnSheetLayout)

         btnSheetLayout.btnCancelar.setOnClickListener {
             dialog.cancel()
         }

         btnSheetLayout.btnSalvar.setOnClickListener{
             salvarSenhaUsuario(btnSheetLayout, dialog)
         }

         dialog.show()

     }

     private fun salvarSenhaUsuario(btnSheetLayout: View?, dialog: BottomSheetDialog) {

         if (validaSenha(btnSheetLayout)) {

             carregandoAlterarSenha(btnSheetLayout, true)

             val credencial = EmailAuthProvider.getCredential(
                 (activity as InicioActivity).auth.currentUser?.email.toString(),
                 btnSheetLayout?.edtSenhaAtual?.text.toString()
             )

             (activity as InicioActivity).auth.currentUser?.reauthenticate(credencial)?.
             addOnCompleteListener { reautenticacao ->
                 if (reautenticacao.isSuccessful){

                     (activity as InicioActivity).auth.currentUser?.
                         updatePassword(btnSheetLayout?.edtSenhaNova?.text.toString())
                         ?.addOnCompleteListener { atualizacaoSenha ->

                             if (atualizacaoSenha.isSuccessful) {
                                 Toast.makeText(activity, "Senha atualizada com sucesso.", Toast.LENGTH_LONG).show()

                                 dialog.dismiss()

                             } else {
                                 Toast.makeText(activity, "Erro ao atualizar senha."
                                         + atualizacaoSenha.exception?.message, Toast.LENGTH_LONG).show()
                                 carregandoAlterarSenha(btnSheetLayout, false)
                             }

                         }

                 } else {
                     Toast.makeText(activity, "Senha atual incorreta.", Toast.LENGTH_LONG).show()
                     carregandoAlterarSenha(btnSheetLayout, false)
                 }
             }

         } else {
             Toast.makeText(activity, "Verifique os campos incorretos.", Toast.LENGTH_LONG).show()
         }

     }

     private fun carregandoAlterarSenha(btnSheetLayout: View?, flag: Boolean) {

         btnSheetLayout?.edtSenhaAtual?.isEnabled = !flag
         btnSheetLayout?.edtSenhaNova?.isEnabled = !flag
         btnSheetLayout?.edtSenhaNovaConfimacao?.isEnabled = !flag

         btnSheetLayout?.btnSalvar?.isEnabled = !flag
         btnSheetLayout?.btnCancelar?.isEnabled = !flag

     }

     private fun validaSenha(btnSheetLayout: View?): Boolean {

         val edtSenhaNova = btnSheetLayout?.edtSenhaNova
         val senhaNova = edtSenhaNova?.text.toString()

         if (senhaNova.isBlank() || senhaNova.isEmpty()){
             edtSenhaNova?.error = "Senha de acesso inválida!"
             edtSenhaNova?.requestFocus()
             return false
         }

         if (senhaNova.length < 6) {
             edtSenhaNova?.error = "Senha de acesso deve ter no mínimo 6 caracteres!"
             edtSenhaNova?.requestFocus()
             return false
         }

         val edtSenhaNovaConfirmacao = btnSheetLayout?.edtSenhaNovaConfimacao

         if (edtSenhaNovaConfirmacao?.text.toString() != senhaNova) {
             edtSenhaNovaConfirmacao?.error = "A confirmação da nova senha falhou, digite novamente."
         }

         return true

     }

     private fun editarNomeUsuario() {

         val btnSheetLayout = layoutInflater.inflate(R.layout.bottom_sheet_edit_name, null)
         val dialog = BottomSheetDialog(this.requireContext(), R.style.BottomSheetStyle)

         dialog.setContentView(btnSheetLayout)

         btnSheetLayout.edtNome.setText((activity as InicioActivity).usuarioLogado.getString("nome"))
         btnSheetLayout.edtNome.selectAll()

         btnSheetLayout.btnCancelar.setOnClickListener {
             dialog.cancel()
         }

         btnSheetLayout.btnSalvar.setOnClickListener{
             salvarNomeUsuario(btnSheetLayout, dialog)
         }

         dialog.show()

     }

     private fun salvarNomeUsuario(btnSheetDialog : View, dialog: BottomSheetDialog) {
         btnSheetDialog.edtNome.isEnabled = false
         btnSheetDialog.btnSalvar.isEnabled = false
         btnSheetDialog.btnCancelar.isEnabled = false

         (activity as InicioActivity).db.collection("usuarios")
             .document((activity as InicioActivity).auth.currentUser!!.uid)
             .update("nome", btnSheetDialog.edtNome.text.toString())
             .addOnCompleteListener { update ->

                 if (update.isSuccessful) {
                     dialog.dismiss()
                 } else {
                     Toast.makeText(activity, "Falha ao atualizar o nome do usuário. "
                             + update.exception?.message, Toast.LENGTH_LONG).show()

                     btnSheetDialog.edtNome.isEnabled = true
                     btnSheetDialog.btnSalvar.isEnabled = true
                     btnSheetDialog.btnCancelar.isEnabled = true
                 }

             }

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
