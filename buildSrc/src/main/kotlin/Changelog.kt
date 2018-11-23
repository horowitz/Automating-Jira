import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

const val prefix = "YOUR_JIRA_PREFIX"
const val transitionId = "YOUR_JIRA_TRANSITION_ID"

open class ChangelogTask : DefaultTask() {

    val jiraApi = RetrofitAdapter.instance().getRetrofit().create(JiraApi::class.java)

    lateinit var versionName: String
    lateinit var oldTag: String
    lateinit var newTag: String

    @TaskAction
    fun sendMessage() {
        val tickets = fetchJiraTickets(oldTag,newTag)
        tickets.forEach { ticket ->
            println("processing $ticket...")
            val hasTransitioned = if (transitionIssue(ticket, transitionId).isSuccessful) "OK ✅" else "FAIL ❌"
            val setFixVersion = if (setFixVersion(ticket, "Android-$versionName").isSuccessful) "OK ✅" else "FAIL ❌"
            println("$ticket transition $hasTransitioned")
            println("$ticket set fix version to $versionName $setFixVersion")
        }
    }

    private fun transitionIssue(id: String, transitionId: String) =
            jiraApi.transitionIssue(id, JiraTransitionBody(JiraTransition(transitionId))).execute()

    private fun setFixVersion(id: String, version: String) =
            jiraApi.editIssue(id, JiraUpdateBody(JiraUpdate(listOf(JiraSetCommand(listOf(SetCommand(version))))))).execute()

}


private fun fetchJiraTickets(oldTag: String, newTag: String): List<String> {
    val gitCommand = "git log $oldTag...$newTag"
    val bufferedReader = gitCommand.runCommand()
    val reader = requireNotNull(bufferedReader)
    return parseTickets(reader)

}

private fun parseTickets(bufferedReader: BufferedReader): List<String> {
    val s = "$prefix-\\d{1,4}"
    val pattern = s.toRegex()
    return bufferedReader.lines().filter { it.contains(prefix) }
            .map {
                pattern.find(it)?.value
            }.toList()
            .filterNotNull()
            .distinct()
}


private fun String.runCommand(): BufferedReader? = try {
    val parts = this.split("\\s".toRegex())
    val process = ProcessBuilder(*parts.toTypedArray())
            .start()
    process.waitFor(10, TimeUnit.SECONDS)
    process.inputStream.bufferedReader()
} catch (e: IOException) {
    e.printStackTrace()
    null
}