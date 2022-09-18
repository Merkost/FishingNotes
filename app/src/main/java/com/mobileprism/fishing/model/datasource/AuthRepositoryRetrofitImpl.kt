package com.mobileprism.fishing.model.datasource

import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.mobileprism.fishing.domain.entity.auth.EmailPassword
import com.mobileprism.fishing.domain.entity.auth.UsernamePassword
import com.mobileprism.fishing.domain.repository.AuthRepository
import com.mobileprism.fishing.model.api.GoogleAuthRequest
import com.mobileprism.fishing.model.api.AuthApiService
import com.mobileprism.fishing.model.entity.user.UserData
import com.mobileprism.fishing.model.entity.user.UserResponse
import com.mobileprism.fishing.model.utils.safeApiCall
import com.mobileprism.fishing.utils.Constants.API_URL
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepositoryRetrofitImpl(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AuthRepository {

    private fun getService(): AuthApiService {
        return createRetrofit().create(AuthApiService::class.java)
    }

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    override suspend fun registerNewUser(emailPassword: EmailPassword): Result<UserResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("register_new_user", null)

            getService().registerNewUser(
                body = emailPassword
            )

            /*UserResponse(
                token = "123",
                UserData(email = emailPassword.email, login = "Anonymous")
            )*/
        }

    override suspend fun loginUser(emailPassword: EmailPassword): Result<UserResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("login_user", null)

//            getService().loginWithEmail(
//                body = emailPassword
//            )

            UserResponse(
                token = "123",
                UserData(email = emailPassword.email, login = "Anonymous")
            )
        }

    override suspend fun loginUser(usernamePassword: UsernamePassword): Result<UserResponse> =
        safeApiCall(dispatcher) {

            firebaseAnalytics.logEvent("login_user", null)

            /*getService().loginWithUsername(
                body = usernamePassword
            )*/

            UserResponse(
                token = "123",
                UserData(login = usernamePassword.username, email = "fromServer@email.ru")
            )
        }

    override suspend fun loginUserWithGoogle(
        email: String,
        userId: String
    ): Result<UserResponse> = safeApiCall(dispatcher) {

        firebaseAnalytics.logEvent("login_with_google", null)

        getService().loginUserWithGoogle(
            GoogleAuthRequest(
                email = email,
                googleAuthId = userId
            )
        )

        /*UserResponse(
            token = "123",
            UserData(email = email, login = "Anonymous")
        )*/
    }
}