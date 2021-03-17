package com.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import com.social.AuthenticationInterface
import com.social.SocialLoginModel
import org.json.JSONObject


class SocialLoginHelper(
        private val mActivity: Activity,
        private val mCallback: AuthenticationInterface
) {
    private var extraField = "fields"
    private lateinit var mCallbackManager: CallbackManager
    private var mLoginType = -1
    lateinit var mGso: GoogleSignInOptions
    lateinit var mClient: GoogleSignInClient
    fun init() {
        FacebookSdk.fullyInitialize()
        mGso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mClient = GoogleSignIn.getClient(mActivity, mGso);
    }
    
    fun signInWithFB() {
        mLoginType = LOGIN_FACEBOOK
        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().apply {
            logInWithReadPermissions(mActivity, listOf("email"))
            registerCallback(mCallbackManager, MyFacebookCallbacks())
        }
    }

    fun signInWithGoogle() {
        mLoginType = LOGIN_GOOGLE
        val signInIntent = mClient.signInIntent
        mActivity.startActivityForResult(signInIntent, REQ_LOGIN_SOCIAL)
    }

    private fun onGoogleResponse(requestCode: Int, intent: Intent?) {
        try {
            if (requestCode == REQ_LOGIN_SOCIAL) {

                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                Log.d("SocialLoginHelper", "onGoogleResponse  " + task.exception?.message)
                Log.d("SocialLoginHelper", "onGoogleResponse  exception " + task.exception?.localizedMessage)
                val account = task.getResult(ApiException::class.java)
                Log.d("SocialLoginHelper", "onGoogleResponse  " + account?.displayName)
                mCallback.okLogin(
                        SocialLoginModel(
                                id = "" + account?.id,
                                name = "" + account?.displayName,
                                email = "" + account?.email,
                                imgUrl = "" + account?.photoUrl,
                                loginType = "google"
                        )
                )
                mClient.signOut()
            }
        } catch (ex: Exception) {
            Log.e("response", "" + ex.message)
            mCallback.failedLogin("Something went wrong")
        }
    }

    private fun onFbResponse(requestCode: Int, responseCode: Int, intent: Intent?) {
        mCallbackManager.onActivityResult(requestCode, responseCode, intent)
    }

    fun getResult(requestCode: Int, responseCode: Int, intent: Intent?) {
        if (mLoginType == LOGIN_GOOGLE) {
            onGoogleResponse(requestCode, intent)
        }
        if (mLoginType == LOGIN_FACEBOOK) {
            onFbResponse(requestCode, responseCode, intent)
        }
    }

    private inner class MyFacebookCallbacks :
        FacebookCallback<LoginResult?> {
        override fun onSuccess(loginResult: LoginResult?) {
            val request =
                GraphRequest.newMeRequest(loginResult?.accessToken) { data: JSONObject?, _: GraphResponse ->
                    try {
                        val (id, name, email) = Gson().fromJson(
                                data?.toString(),
                                SocialLoginModel::class.java
                        )
                        val imgUrl =
                            data?.getJSONObject("picture")?.getJSONObject("data")?.getString("url")
                        val model = SocialLoginModel(id, name, email, imgUrl, "facebook")
                        mCallback.okLogin(model)
                        LoginManager.getInstance().logOut();

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            val parameters = Bundle()
            parameters.putString(extraField, "id,name,email,picture.width(200)")
            request.parameters = parameters
            request.executeAsync()
        }

        override fun onCancel() {
            mCallback.failedLogin("user cancel.")
        }

        override fun onError(error: FacebookException?) {
            mCallback.failedLogin("Something went wrong")
        }
    }

    companion object {
        private const val LOGIN_GOOGLE = 0
        private const val LOGIN_FACEBOOK = 1
        const val REQ_LOGIN_SOCIAL = 1000
    }
}

