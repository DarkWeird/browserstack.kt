package me.darkweird.browserstack

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.darkweird.browserstack.har.HAR

const val baseUrl = "https://api.browserstack.com"

/**
 * Provide api methods for browserstack's API
 */
class BrowserStackApi private constructor(
    private val client: HttpClient
) {

    companion object {
        fun create(username: String, accessKey: String): BrowserStackApi =
            BrowserStackApi(HttpClient { installNeeded(username, accessKey) })

        fun create(username: String, accessKey: String, httpConfig: HttpClientConfig<*>.() -> Unit): BrowserStackApi =
            BrowserStackApi(HttpClient {
                httpConfig(this)
                installNeeded(username, accessKey)
            })

        fun <T : HttpClientEngineConfig> create(
            username: String,
            accessKey: String,
            httpEngine: HttpClientEngineFactory<T>,
            httpConfig: HttpClientConfig<T>.() -> Unit
        ): BrowserStackApi =
            BrowserStackApi(HttpClient(httpEngine) {
                httpConfig(this)
                installNeeded(username, accessKey)
            })

        private fun <T : HttpClientEngineConfig> HttpClientConfig<T>.installNeeded(
            username: String,
            accessKey: String
        ) {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(
                            username, accessKey
                        )
                    }
                    sendWithoutRequest {
                        it.url.host == "api.browserstack.com"
                    }
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Using for custom keys in HAR
                })
            }
        }
    }


    val project by lazy { ProjectObject(client) }
    val build by lazy { BuildObject(client) }
    val session by lazy { SessionObject(client) }

    /**
     * Get plan detail
     */
    suspend fun getPlanDetails(): Plan {
        return client.get("$baseUrl/automate/plan.json").body()
    }

    /**
     * Get browser list available at browser stack
     */
    suspend fun getBrowsers(): List<Browser> {
        return client.get("$baseUrl/automate/browsers.json").body()
    }

}

class SessionObject(private val client: HttpClient) {
    suspend fun list(
        buildId: String, limit: Int? = null, offset: Int? = null, status: Status? = null
    ): List<SessionFullWrapper> = client.get("$baseUrl/automate/builds/$buildId/sessions.json") {
        parameter("limit", limit)
        parameter("offset", offset)
        parameter("status", status)
    }.body()

    suspend fun getSessionDetails(sessionId: String): SessionFullWrapper =
        client.get("$baseUrl/automate/sessions/$sessionId.json").body()

    suspend fun getSessionLogs(sessionsId: String): String =
        client.get("$baseUrl/automate/sessions/$sessionsId/logs").body()


    suspend fun getNetworkLogs(sessionsId: String): HAR? = kotlin.runCatching {
        client.get("$baseUrl/automate/sessions/$sessionsId/networklogs")
    }.map { it.body<HAR>() }.getOrNull()


    suspend fun getConsoleLogs(sessionsId: String): String? = kotlin.runCatching {
        client.get("$baseUrl/automate/sessions/$sessionsId/consolelogs")
    }.map { it.body<String>() }.getOrNull()

    suspend fun getSeleniumLogs(sessionsId: String): String? = kotlin.runCatching {
        client.get("$baseUrl/automate/sessions/$sessionsId/seleniumlogs")
    }.map { it.body<String>() }.getOrNull()


    suspend fun getAppiumLogs(sessionsId: String): String? = kotlin.runCatching {
        client.get("$baseUrl/automate/sessions/$sessionsId/appiumlogs")
    }.map { it.body<String>() }.getOrNull()

    suspend fun setTestStatus(sessionsId: String, data: StatusRequest): SessionWrapper =
        client.put("$baseUrl/automate/sessions/$sessionsId.json") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.body()

    suspend fun setSessionName(sessionsId: String, data: UpdateNameRequest): SessionWrapper =
        client.put("$baseUrl/automate/sessions/$sessionsId.json") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.body()

    suspend fun deleteSession(sessionsId: String): DeleteResponse =
        client.delete("$baseUrl/automate/sessions/$sessionsId.json").body()

    suspend fun deleteSessions(id: List<String>): DeleteResponse = client.delete("$baseUrl/automate/sessions") {
        id.forEach {
            parameter("buildId", it)
        }
    }.body()
}


@Serializable
data class StatusRequest(
    val status: Status, val reason: String
)

@Serializable
data class SessionFullWrapper(@SerialName("automation_session") val automationSession: SessionFull)

@Serializable
data class SessionWrapper(@SerialName("automation_session") val automationSession: Session)


@Serializable
data class SessionFull(
    val name: String,
    val duration: Int?,
    val os: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("browser_version") val browserVersion: String?,
    val browser: String?,
    val device: String?,
    val status: Status,
    @SerialName("hashed_id") val hashedId: String,
    @SerialName("build_hashed_id") val buildHashedId: String,
    val reason: String?,
    @SerialName("build_name") val buildName: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("test_priority") val testPriority: Int? = null,
    val logs: String,
    @SerialName("browserstack_status") val browserstackStatus: Status,
    @SerialName("created_at") val createdAt: String,// TODO kotlinx.time
    @SerialName("browser_url") val browserUrl: String,
    @SerialName("public_url") val publicUrl: String,
    @SerialName("appium_logs_url") val appiumLogsUrl: String? = null,
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("browser_console_logs_url") val browserConsoleLogsUrl: String? = null,
    @SerialName("har_logs_url") val harLogsUrl: String? = null,
    @SerialName("selenium_logs_url") val seleniumLogsUrl: String? = null,
    @SerialName("selenium_telemetry_logs_url") val seleniumTelemetryLogsUrl: String? = null,
)


@Serializable
data class Session(
    val name: String,
    val duration: Int?,
    val os: String,
    @SerialName("os_version") val osVersion: String,
    @SerialName("browser_version") val browserVersion: String?,
    val browser: String?,
    val device: String?,
    val status: Status,
    @SerialName("hashed_id") val hashedId: String,
    @SerialName("build_hashed_id") val buildHashedId: String,
    val reason: String?,
    @SerialName("build_name") val buildName: String,
    @SerialName("project_name") val projectName: String,
    @SerialName("test_priority") val testPriority: Int? = null,
)

class ProjectObject(private val client: HttpClient) {

    suspend fun list(): List<Project> = client.get("$baseUrl/automate/projects.json").body()

    suspend fun getProjectDetail(id: Int): ProjectDetailsResponse =
        client.get("$baseUrl/automate/projects/$id.json").body()

    suspend fun getStatusBadge(id: Int): String = client.get("$baseUrl/automate/projects/$id/badge_key").body()

    suspend fun updateProjectDetails(id: Int, request: UpdateNameRequest): Project =
        client.put("$baseUrl/automate/projects/$id.json") {
            contentType(ContentType.parse("application/json"))
            setBody(request)
        }.body()

    suspend fun deleteProject(id: Int): DeleteResponse = client.delete("$baseUrl/automate/projects/$id.json").body()
}

class BuildObject(private val client: HttpClient) {
    suspend fun list(limit: Int? = null, offset: Int? = null, status: Status? = null): List<BuildWrapper> =
        client.get("$baseUrl/automate/builds.json") {
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("status", status)
        }.body()

    suspend fun updateBuild(id: String, request: UpdateNameRequest): BuildWrapper =
        client.put("$baseUrl/automate/builds/$id.json") {
            contentType(ContentType.parse("application/json"))
            setBody(request)
        }.body()

    suspend fun deleteBuild(id: String): DeleteResponse = client.delete("$baseUrl/automate/builds/$id.json").body()

    suspend fun deleteBuilds(id: List<String>): DeleteResponse = client.delete("$baseUrl/automate/builds") {
        id.forEach {
            parameter("buildId", it)
        }
    }.body()
}

@Serializable
enum class Status {
    @SerialName("running")
    RUNNING,

    @SerialName("done")
    DONE,

    @SerialName("timeout")
    TIMEOUT,

    @SerialName("failed")
    FAILED,

    @SerialName("passed")
    PASSED;

    override fun toString(): String {
        return when (this) {
            RUNNING -> "running"
            DONE -> "done"
            TIMEOUT -> "timeout"
            FAILED -> "failed"
            PASSED -> "passed"
        }
    }
}

@Serializable
data class DeleteResponse(
    val status: String, val message: String, val deleteRequestId: String?
)

@Serializable
data class UpdateNameRequest(
    val name: String
)

@Serializable
data class ProjectDetailsResponse(
    val project: Project
)

@Serializable
data class Project(
    val id: Int,
    val name: String,
    @SerialName("group_id") val groupId: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("created_at") val createdAt: String, // TODO kotlinx.time
    @SerialName("updated_at") val updatedAt: String, // TODO kotlinx.time
    @SerialName("sub_group_id") val subGroupId: Int,
    val builds: List<BuildFull>? = null
)

@Serializable
data class BuildWrapper(@SerialName("automation_build") val automationBuild: Build)

@Serializable
data class Build(
    val name: String,
    val duration: Int?,//TODO convert to duration
    val status: Status,
    @SerialName("hashed_id") val hashedId: String,
    @SerialName("build_tag") val buildTag: String?,
)

@Serializable
data class BuildFull(
    val id: Int,
    val name: String,
    val duration: Int?, //TODO convert to duration
    val status: Status,
    val tags: List<String>?,
    @SerialName("group_id") val groupId: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("automation_project_id") val automationProjectId: Int,
    @SerialName("created_at") val createdAt: LocalDateTime,
    @SerialName("updated_at") val updatedAt: LocalDateTime,
    @SerialName("hashed_id") val hashedId: String, // TODO determinate hash type
    val delta: Boolean,
    @SerialName("sub_group_id") val subGroupId: Int,
    val framework: String,
    @SerialName("test_data") val testData: Map<String, String>?
)

@Serializable
data class Browser(
    val os: String,
    @SerialName("os_version") val osVersion: String,
    val browser: String,
    val device: String?,
    @SerialName("browser_version") val browserVersion: String?,
    @SerialName("real_mobile") val realMobile: Boolean?
)

@Serializable
data class Plan(
    @SerialName("automate_plan") val automatePlan: String,
    @SerialName("parallel_sessions_running") val parallelSessionsRunning: Int,
    @SerialName("team_parallel_sessions_max_allowed") val teamParallelSessionsMaxAllowed: Int,
    @SerialName("parallel_sessions_max_allowed") val parallelSessionsMaxAllowed: Int,
    @SerialName("queued_sessions") val queuedSessions: Int,
    @SerialName("queued_sessions_max_allowed") val queuedSessionsMaxAllowed: Int
)