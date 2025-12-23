package com.example.shopsphere.fragments.settings

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.shopsphere.data.User
import com.example.shopsphere.databinding.FragmentUserAccountBinding
import com.example.shopsphere.dialog.setupBottomSheetDialog
import com.example.shopsphere.util.Resource
import com.example.shopsphere.viewmodel.UserAccountViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserAccountFragment : Fragment() {
    private lateinit var binding: FragmentUserAccountBinding
    private val viewModel by viewModels<UserAccountViewModel>()
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserAccountBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSave.setOnClickListener {
            binding.apply {
                val firstName = edFirstName.text.trim().toString()
                val lastName = edLastName.text.trim().toString()
                val email = edEmail.text.trim().toString()
                val user = User(firstName, lastName, email)

                viewModel.updateUser(user, imageUri)
            }
        }

        binding.imageEdit.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageActivityResultLauncher.launch(intent)
        }

        binding.tvUpdatePassword.setOnClickListener {
            setupBottomSheetDialog { email ->
                viewModel.resetPassword(email)
            }
        }

        binding.imageCloseUserAccount.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            showUserLoading()
                        }

                        is Resource.Success -> {
                            hideUserLoading()
                            showUserInformation(it.data!!)
                        }

                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(), it.message.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateInfo.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonSave.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonSave.revertAnimation()
                            findNavController().navigateUp()
                        }

                        is Resource.Error -> {
                            Toast.makeText(
                                requireContext(), it.message.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.resetPassword.collect {
                    when (it) {
                        is Resource.Loading -> {

                        }

                        is Resource.Success -> {
                            Snackbar.make(
                                requireView(),
                                "Reset link was sent to your email",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Error -> {
                            Snackbar.make(
                                requireView(), "Error: ${it.message}", Snackbar.LENGTH_LONG
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                imageUri = it.data?.data
                Glide.with(this).load(imageUri).into(binding.imageUser)
            }
    }

    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@UserAccountFragment).load(data.imagePath)
                .error(ColorDrawable(Color.BLACK)).into(imageUser)
            edFirstName.setText(data.firstName)
            edLastName.setText(data.lastName)
            edEmail.setText(data.email)
        }
    }

    private fun hideUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.GONE
            imageUser.visibility = View.VISIBLE
            imageEdit.visibility = View.VISIBLE
            edFirstName.visibility = View.VISIBLE
            edLastName.visibility = View.VISIBLE
            edEmail.visibility = View.VISIBLE
            tvUpdatePassword.visibility = View.VISIBLE
            buttonSave.visibility = View.VISIBLE
        }
    }

    private fun showUserLoading() {
        binding.apply {
            progressbarAccount.visibility = View.VISIBLE
            imageUser.visibility = View.INVISIBLE
            imageEdit.visibility = View.INVISIBLE
            edFirstName.visibility = View.INVISIBLE
            edLastName.visibility = View.INVISIBLE
            edEmail.visibility = View.INVISIBLE
            tvUpdatePassword.visibility = View.INVISIBLE
            buttonSave.visibility = View.INVISIBLE
        }
    }
}