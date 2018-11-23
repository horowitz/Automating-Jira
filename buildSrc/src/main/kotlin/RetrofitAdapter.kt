import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class RetrofitAdapter {

    fun getRetrofit() = retrofit

    companion object {
        private var instance: RetrofitAdapter? = null

        lateinit var retrofit: Retrofit


        fun instance(): RetrofitAdapter {
            if (instance == null) {
                instance = RetrofitAdapter()
                init()
            }
            return instance!!
        }

        private fun init() {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val user = "your-jira-user@example.com"
            val password = "your-jira-password"
            val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(BasicAuthInterceptor(user, password))
                    .build()

            val url = "your-jira-url"
            this.retrofit = Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
        }
    }

}

class BasicAuthInterceptor(user: String, password: String) : Interceptor {

    private val credentials: String = Credentials.basic(user, password)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials).build()
        return chain.proceed(authenticatedRequest)
    }

}