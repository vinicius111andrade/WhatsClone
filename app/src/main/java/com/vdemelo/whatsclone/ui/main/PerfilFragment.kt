 package com.vdemelo.whatsclone.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
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
import java.io.ByteArrayOutputStream

 class PerfilFragment : Fragment() {

     val REQUISICAO_FOTO_GALERIA = 101
     val REQUISICAO_FOTO_CAMERA = 102

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
             startActivityForResult(
                 Intent.createChooser(intent, "Selecionar foto"),
                 REQUISICAO_FOTO_GALERIA
             )
             dialog.dismiss()

         }

         btnSheetLayout.imgBtnCamera.setOnClickListener {

             Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                 takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                     startActivityForResult(takePictureIntent, REQUISICAO_FOTO_CAMERA)
                 }
             }

         }

         dialog.show()

     }

     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)

         if (requestCode == REQUISICAO_FOTO_GALERIA || requestCode == REQUISICAO_FOTO_CAMERA) {

             val referenciaStorage = FirebaseStorage.getInstance().reference

             val referenciaArquivo = referenciaStorage.child (
                 "imagens.perfil/"
                         + (activity as InicioActivity).auth.currentUser?.uid
                         + "/"
                         + System.currentTimeMillis().toString()
             )

             when (requestCode) {

                 REQUISICAO_FOTO_CAMERA -> {

                     if (resultCode == RESULT_OK) {

                         val imagemBitmap = data?.extras?.get("data") as Bitmap
                         val baos = ByteArrayOutputStream()
                         imagemBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                         val arquivo = baos.toByteArray()

                         val tarefaUploadArquivo = referenciaArquivo.putBytes(arquivo)
                             tarefaUploadArquivo.addOnCompleteListener { upload ->
                                 trataUploadFoto(upload, referenciaArquivo)
                             }
                     }
                 }

                 REQUISICAO_FOTO_GALERIA -> {

                     if (data?.data != null) {

                         val arquivo = data.data
                         val tarefaUploadAquivo = referenciaArquivo.putFile(arquivo!!)

                         tarefaUploadAquivo.addOnCompleteListener { upload ->
                             trataUploadFoto(upload, referenciaArquivo)
                             }
                     }
                 }

             }

         }

     }

     private fun trataUploadFoto(
         upload: Task<UploadTask.TaskSnapshot>,
         referenciaArquivo: StorageReference
     ) {

         if (upload.isSuccessful) {

             referenciaArquivo.downloadUrl.addOnCompleteListener { uri ->

                 updateImagemPerfil(uri.toString())

             }.addOnFailureListener { excecao ->

                 Toast.makeText(
                     activity,
                     "Falha ao realizar operação. Motivo: " + excecao.message,
                     Toast.LENGTH_LONG
                 ).show()

             }

         }

     }

     private fun updateImagemPerfil(foto: String) {

         (activity as InicioActivity).db.collection("usuarios")
             .document((activity as InicioActivity).auth.currentUser!!.uid)
             .update("foto", foto)
             .addOnFailureListener { excecao ->

                 Toast.makeText(
                     activity,
                     "Falha ao realizar operação. Motivo: " + excecao.message,
                     Toast.LENGTH_LONG
                 ).show()

             }

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
