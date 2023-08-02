package com.example.mixnchat.ui.login


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.mixnchat.ui.mainpage.MainActivity
import com.example.mixnchat.R
import com.example.mixnchat.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase



class LoginFragment : Fragment() {

    private var _binding : FragmentLoginBinding ?= null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore : FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var launcher : ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
        init()
        return binding.root
    }

    private fun init() {
        firestore = Firebase.firestore
        auth = Firebase.auth
        val currentUser = auth.currentUser

        if(currentUser != null){
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(),gso)
        registerLauncher()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.forgetPassword.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
            findNavController().navigate(action)
        }

        binding.signUpText.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }

        binding.loginButton.setOnClickListener {
            login()
        }

        binding.googleSignIn.setOnClickListener {
            loginWithGoogle()
        }
    }

    private fun login() {
        val email = binding.mailEdit.text.toString()
        val password = binding.passwordEdit.text.toString()


        if(email == "" || password == ""){
            binding.mailEdit.error = "Fill this field!"
            binding.passwordEdit.error = "Fill this field!"
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                    if (auth.currentUser!!.isEmailVerified){
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }else{
                        Toast.makeText(requireContext(),"Please verify your mail address", Toast.LENGTH_LONG).show()
                    }
            }.addOnFailureListener {
                    Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loginWithGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private fun registerLauncher() {
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == Activity.RESULT_OK){
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if(task.isSuccessful){
                    val account : GoogleSignInAccount ?= task.result
                    if (account != null){
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        auth.signInWithCredential(credential).addOnSuccessListener {
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            val user = hashMapOf<String,Any>()
                            user["userUid"] = auth.currentUser!!.uid
                            user["username"] = account.displayName.toString()
                            user["profileUrl"] = account.photoUrl.toString()
                            user["country"] = ""
                            user["gender"] = ""
                            user["biography"] = ""
                            user["phone"] = ""
                            user["speech"] = ""
                            firestore.collection("Users").document(auth.currentUser!!.uid).set(user).addOnSuccessListener {
                                startActivity(intent)
                                requireActivity().finish()
                            }.addOnFailureListener {
                                Toast.makeText(requireContext(),it.localizedMessage,Toast.LENGTH_LONG).show()
                            }


                        }.addOnFailureListener {
                            Toast.makeText(requireActivity(),it.localizedMessage,Toast.LENGTH_LONG).show()
                        }
                    }
                }else{
                    Toast.makeText(requireActivity(),task.exception.toString(),Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}