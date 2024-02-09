package it.appventurers.taskflow.ui.elements.welcome;

import static it.appventurers.taskflow.util.Constant.EMAIL_ADDRESS;
import static it.appventurers.taskflow.util.Constant.ENCRYPTED_SHARED_PREFERENCES_FILE;
import static it.appventurers.taskflow.util.Constant.PASSWORD;
import static it.appventurers.taskflow.util.Constant.TOKEN;
import static it.appventurers.taskflow.util.Constant.WEB_CLIENT;

import android.app.Activity;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.security.GeneralSecurityException;

import it.appventurers.taskflow.R;
import it.appventurers.taskflow.databinding.FragmentLoginBinding;
import it.appventurers.taskflow.model.Result;
import it.appventurers.taskflow.model.User;
import it.appventurers.taskflow.ui.viewmodel.UserViewModel;
import it.appventurers.taskflow.util.ClassBuilder;
import it.appventurers.taskflow.util.EncryptedSharedPreferencesUtil;

/**
 * Login fragment class
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private EncryptedSharedPreferencesUtil encryptedSharedPreferences;
    private UserViewModel userViewModel;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private ActivityResultContracts.StartIntentSenderForResult startIntentSenderForResult;


    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        encryptedSharedPreferences = new EncryptedSharedPreferencesUtil(requireContext());
        userViewModel = new UserViewModel(
                ClassBuilder.getClassBuilder().getUserRepository(requireActivity().getApplication()));
        oneTapClient = Identity.getSignInClient(requireActivity());
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(WEB_CLIENT)
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .build();

        startIntentSenderForResult = new ActivityResultContracts.StartIntentSenderForResult();
        activityResultLauncher = registerForActivityResult(startIntentSenderForResult, activityResult -> {
            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(activityResult.getData());
                    String token = credential.getGoogleIdToken();
                    if (token != null) {
                        userViewModel.signInWithGoogle(token);
                        userViewModel.getUserData().observe(getViewLifecycleOwner(), result -> {
                            if (result.isSuccess()) {
                                User user = ((Result.UserSuccess) result).getUser();
                                try {
                                    encryptedSharedPreferences.saveCredentialInformationEncrypted(
                                            ENCRYPTED_SHARED_PREFERENCES_FILE, EMAIL_ADDRESS, user.getEmail());
                                    encryptedSharedPreferences.saveCredentialInformationEncrypted(
                                            ENCRYPTED_SHARED_PREFERENCES_FILE, PASSWORD, null);
                                    encryptedSharedPreferences.saveCredentialInformationEncrypted(
                                            ENCRYPTED_SHARED_PREFERENCES_FILE, TOKEN, user.getuId());
                                } catch (GeneralSecurityException | IOException e) {
                                    Snackbar.make(view,
                                            "Unable to retrieve credentials",
                                            Snackbar.LENGTH_SHORT).show();
                                    binding.loginButton.setVisibility(View.VISIBLE);
                                    binding.googleLoginButton.setVisibility(View.VISIBLE);
                                    binding.loginProgress.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                Snackbar.make(view,
                                        "Unable to retrieve credentials",
                                        Snackbar.LENGTH_SHORT).show();
                                binding.loginButton.setVisibility(View.VISIBLE);
                                binding.googleLoginButton.setVisibility(View.VISIBLE);
                                binding.loginProgress.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });



        binding.loginButton.setOnClickListener(view1 -> {
            binding.emailTextLayout.setError(null);
            binding.passwordTextLayout.setError(null);
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            if (!email.isEmpty() && !password.isEmpty()) {
                binding.loginButton.setVisibility(View.INVISIBLE);
                binding.googleLoginButton.setVisibility(View.INVISIBLE);
                binding.loginProgress.setVisibility(View.VISIBLE);
                userViewModel.signIn(email, password);
                userViewModel.getUserData().observe(getViewLifecycleOwner(), result -> {
                    if (result.isSuccess()) {
                        User user = ((Result.UserSuccess) result).getUser();
                        try {
                            encryptedSharedPreferences.saveCredentialInformationEncrypted(
                                    ENCRYPTED_SHARED_PREFERENCES_FILE, EMAIL_ADDRESS, email);
                            encryptedSharedPreferences.saveCredentialInformationEncrypted(
                                    ENCRYPTED_SHARED_PREFERENCES_FILE, PASSWORD, password);
                            encryptedSharedPreferences.saveCredentialInformationEncrypted(
                                    ENCRYPTED_SHARED_PREFERENCES_FILE, TOKEN, user.getuId());
                        } catch (GeneralSecurityException | IOException e) {
                            Snackbar.make(view,
                                    "Unable to retrieve credentials",
                                    Snackbar.LENGTH_SHORT).show();
                            binding.loginButton.setVisibility(View.VISIBLE);
                            binding.googleLoginButton.setVisibility(View.VISIBLE);
                            binding.loginProgress.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        String error = ((Result.Fail) result).getError();
                        Snackbar.make(view,
                                error,
                                Snackbar.LENGTH_SHORT).show();
                        binding.loginButton.setVisibility(View.VISIBLE);
                        binding.googleLoginButton.setVisibility(View.VISIBLE);
                        binding.loginProgress.setVisibility(View.INVISIBLE);
                    }
                });
                try {
                    String savedEmail = encryptedSharedPreferences.readCredentialInformationEncrypted(
                            ENCRYPTED_SHARED_PREFERENCES_FILE, EMAIL_ADDRESS);
                    String savedPassword = encryptedSharedPreferences.readCredentialInformationEncrypted(
                            ENCRYPTED_SHARED_PREFERENCES_FILE, PASSWORD);
                    if (savedEmail != null && savedPassword != null) {
                        if (savedEmail.equals(email) && savedPassword.equals(password)) {
                            navController.navigate(R.id.action_loginFragment_to_mainActivity);
                            requireActivity().finish();
                        } else {
                            Snackbar.make(view, "Email or password are wrong", Snackbar.LENGTH_SHORT).show();
                            binding.loginButton.setVisibility(View.VISIBLE);
                            binding.googleLoginButton.setVisibility(View.VISIBLE);
                            binding.loginProgress.setVisibility(View.INVISIBLE);
                            binding.emailEditText.setText(null);
                            binding.passwordEditText.setText(null);
                        }
                    } else {
                        Snackbar.make(view, "Email or password are wrong", Snackbar.LENGTH_SHORT).show();
                        binding.loginButton.setVisibility(View.VISIBLE);
                        binding.googleLoginButton.setVisibility(View.VISIBLE);
                        binding.loginProgress.setVisibility(View.INVISIBLE);
                        binding.emailEditText.setText(null);
                        binding.passwordEditText.setText(null);
                    }
                } catch (GeneralSecurityException | IOException e) {
                    Snackbar.make(view, "Unable to retrieve credentials", Snackbar.LENGTH_SHORT).show();
                    binding.loginButton.setVisibility(View.VISIBLE);
                    binding.googleLoginButton.setVisibility(View.VISIBLE);
                    binding.loginProgress.setVisibility(View.INVISIBLE);
                    binding.emailEditText.setText(null);
                    binding.passwordEditText.setText(null);
                }
            } else {
                binding.emailTextLayout.setError("Insert credentials");
                binding.passwordTextLayout.setError("Insert credentials");
            }
        });

        binding.googleLoginButton.setOnClickListener(view1 -> {
            oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener(beginSignInResult -> {
                        navController.navigate(R.id.action_loginFragment_to_mainActivity);
                        requireActivity().finish();
            })
                    .addOnFailureListener(e -> {
                        Snackbar.make(view, "Unable to sign in with Google", Snackbar.LENGTH_SHORT).show();
            });
        });

        binding.forgotPasswordButton.setOnClickListener(view1 ->
                navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment));

        binding.registerButton.setOnClickListener(view1 ->
                navController.navigate(R.id.action_loginFragment_to_registerFragment));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}