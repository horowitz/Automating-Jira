import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class JiraTransitionBody(val transition: JiraTransition)
data class JiraTransition(val id: String)
data class JiraUpdateBody(val update: JiraUpdate)
data class JiraUpdate(val fixVersions: List<JiraSetCommand>)
data class JiraSetCommand(val set: List<SetCommand>)
data class SetCommand(val name: String)

interface JiraApi {
    @POST("/rest/api/3/issue/{id}/transitions")
    fun transitionIssue(@Path("id") id: String, @Body transition: JiraTransitionBody): Call<String>

    @PUT("/rest/api/3/issue/{id}")
    fun editIssue(@Path("id") id: String, @Body update: JiraUpdateBody): Call<String>
}