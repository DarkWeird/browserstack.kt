package me.darkweird.browserstack

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

private const val user = "dummyuser"
private const val access_key = "dummyaccess"

private const val DUMMY_SESSION = """{"automation_session":{"name":"NewName",
                                    "duration":25,"os":"OS X",
                                    "os_version":"Sierra",
                                    "browser_version":"97.0",
                                    "browser":"firefox",
                                    "device":null,"status":"failed",
                                    "hashed_id":"...",
                                    "reason":"good",
                                    "build_name":"New Name",
                                    "project_name":"Untitled Project",
                                    "build_hashed_id":"...",
                                    "test_priority":null,"logs":"https://automate.browserstack.com/builds/.../logs",
                                    "browserstack_status":"done",
                                    "created_at":"2022-02-22T11:12:15.000Z",
                                    "browser_url":"https://automate.browserstack.com/builds/.../...",
                                    "public_url":"https://automate.browserstack.com/builds/.../...",
                                    "video_url":"https://automate.browserstack.com/sessions/...",
                                    "browser_console_logs_url":"https://automate.browserstack.com/...",
                                    "har_logs_url":"https://automate.browserstack.com/...",
                                    "selenium_logs_url":"https://automate.browserstack.com/...",
                                    "selenium_telemetry_logs_url":"https://automate.browserstack.com/..."}}
                                    """

private const val EXAMPLE_NETWORK_LOG = """
{
    "log": {
        "version": "1.2",
        "creator": {
            "name": "BrowserMob Proxy",
            "version": "2.1.5",
            "comment": ""
        },
        "pages": [
            {
                "id": "Page 0",
                "startedDateTime": "2020-09-08T14:10:24.211+05:30",
                "title": "Page 0",
                "pageTimings": {
                    "comment": ""
                },
                "comment": ""
            }
        ],
        "entries": [
            {
                "pageref": "Page 0",
                "startedDateTime": "2020-09-08T14:10:25.924+05:30",
                "request": {
                    "method": "CONNECT",
                    "url": "https://configuration.apple.com",
                    "httpVersion": "HTTP/1.1",
                    "cookies": [],
                    "headers": [],
                    "queryString": [],
                    "headersSize": 0,
                    "bodySize": 0,
                    "comment": ""
                },
                "response": {
                    "status": 0,
                    "statusText": "",
                    "httpVersion": "unknown",
                    "cookies": [],
                    "headers": [],
                    "content": {
                        "size": 0,
                        "mimeType": "",
                        "comment": ""
                    },
                    "redirectURL": "",
                    "headersSize": -1,
                    "bodySize": -1,
                    "comment": "",
                    "_error": "Unable to connect to host"
                },
                "cache": {},
                "timings": {
                    "comment": "",
                    "connect": 472,
                    "receive": 0,
                    "blocked": -1,
                    "send": 1,
                    "ssl": -1,
                    "wait": -9860836,
                    "dns": -1
                },
                "comment": "",
                "time": 474
            },
            {
                "pageref": "Page 0",
                "startedDateTime": "2020-09-08T14:10:26.415+05:30",
                "request": {
                    "method": "CONNECT",
                    "url": "https://configuration.apple.com",
                    "httpVersion": "HTTP/1.1",
                    "cookies": [],
                    "headers": [],
                    "queryString": [],
                    "headersSize": 0,
                    "bodySize": 0,
                    "comment": ""
                },
                "response": {
                    "status": 0,
                    "statusText": "",
                    "httpVersion": "unknown",
                    "cookies": [],
                    "headers": [],
                    "content": {
                        "size": 0,
                        "mimeType": "",
                        "comment": ""
                    },
                    "redirectURL": "",
                    "headersSize": -1,
                    "bodySize": -1,
                    "comment": "",
                    "_error": "Unable to connect to host"
                },
                "cache": {},
                "timings": {
                    "comment": "",
                    "connect": 299,
                    "receive": 0,
                    "blocked": -1,
                    "send": 0,
                    "ssl": -1,
                    "wait": -9861281,
                    "dns": -1
                },
                "comment": "",
                "time": 299
            },
            {
                "pageref": "Page 0",
                "startedDateTime": "2020-09-08T14:10:26.717+05:30",
                "request": {
                    "method": "CONNECT",
                    "url": "https://configuration.apple.com",
                    "httpVersion": "HTTP/1.1",
                    "cookies": [],
                    "headers": [],
                    "queryString": [],
                    "headersSize": 0,
                    "bodySize": 0,
                    "comment": ""
                },
                "response": {
                    "status": 0,
                    "statusText": "",
                    "httpVersion": "unknown",
                    "cookies": [],
                    "headers": [],
                    "content": {
                        "size": 0,
                        "mimeType": "",
                        "comment": ""
                    },
                    "redirectURL": "",
                    "headersSize": -1,
                    "bodySize": -1,
                    "comment": "",
                    "_error": "Unable to connect to host"
                },
                "cache": {},
                "timings": {
                    "comment": "",
                    "connect": 305,
                    "receive": 0,
                    "blocked": -1,
                    "send": 0,
                    "ssl": -1,
                    "wait": -9861583,
                    "dns": -1
                },
                "comment": "",
                "time": 305
            }
        ],
        "comment": ""
    }
}
"""

class Tests {
    @Test
    fun get_plan_details() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/plan.json",
                            """
                          {"automate_plan":"Automate Mobile",
                            "parallel_sessions_running":0,
                            "team_parallel_sessions_max_allowed":5,
                            "parallel_sessions_max_allowed":5,
                            "queued_sessions":0,
                            "queued_sessions_max_allowed":5}
                    """.trimIndent()
                        )
                    )
                }
            }
            api.getPlanDetails()
        }


    @Test
    fun get_browsers() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/browsers.json",
                            """
                                [
                                    {
                                        "os": "Windows",
                                        "os_version": "10",
                                        "browser": "chrome",
                                        "device": null,
                                        "browser_version": "81.0",
                                        "real_mobile": null
                                    },
                                    {
                                        "os":"ios",
                                        "os_version":"14",
                                        "browser":"iphone",
                                        "device":"iPhone 11",
                                        "browser_version":null,
                                        "real_mobile":true
                                    },
                                    {
                                        "os":"OS X",
                                        "os_version":"Catalina",
                                        "browser":"chrome",
                                        "device":null,
                                        "browser_version":"39.0",
                                        "real_mobile":null
                                    }
                                ]

                            """.trimIndent()
                        )
                    )
                }
            }
            api.getBrowsers()
        }

    @Test
    fun get_project_list() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/projects.json",
                            """
                            [{"id":1337,"name":"New Name","group_id":1337,"user_id":1337,"created_at":"2022-02-17T00:56:40.000Z",
                            "updated_at":"2022-02-25T08:43:48.000Z","sub_group_id":0},
                            {"id":1337,"name":"Untitled Project","group_id":1337,"user_id":1337,"created_at":"2022-02-22T00:11:25.000Z",
                            "updated_at":"2022-02-22T00:12:15.000Z","sub_group_id":0}]
                        """.trimIndent()
                        )
                    )
                }
            }
            api.project.list()
        }

    @Test
    fun update_project_name() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Put,
                            "/automate/projects/1337.json",
                            """
                            {"id":1337,"name":"New Name","group_id":1337,"user_id":1337,"created_at":"2022-02-17T00:56:40.000Z",
                            "updated_at":"2022-02-25T08:43:48.000Z","sub_group_id":0}
                        """.trimIndent(),
                            requestBody = """
                            {"name":"New Name"}
                        """.trimIndent()
                        )
                    )
                }
            }
            api.project.updateProjectDetails(1337, UpdateNameRequest("New Name"))
        }


    @Test
    fun get_project_details() = runTest {
        val api = BrowserStackApi.create(user, access_key, MockEngine) {
            engine {
                addHandler(
                    handleRequest(
                        HttpMethod.Get,
                        "/automate/projects/1337.json",
                        """
                        {"project":  {"id":1337,"name":"New Name","group_id":1337,"user_id":1337,"created_at":"2022-02-17T00:56:40.000Z",
                            "updated_at":"2022-02-25T08:43:48.000Z","sub_group_id":0}}
                    """.trimIndent()
                    )
                )
            }
        }
        api.project.getProjectDetail(1337)
    }


    @Test
    fun get_status_badge() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/projects/1337/badge_key",
                            "T3hzUTJ0R1dTQ1dYN25VSmFMbGhQTlp5R3JvTjJGTDNGNGxkVEdfgregwefwefqwdwfreMTys1by84TE9BZ2c9PQ==--0a88223223242532532412bc2d28eaad8866c8c1"
                        )
                    )
                }
            }
            api.project.getStatusBadge(1337)
        }

    @Test
    fun get_build_list() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/builds.json",
                            """
                            [{"automation_build":{"name":"New Name","duration":null,
                            "status":"failed","hashed_id":"somehashid","build_tag":null}}]
                        """.trimIndent()
                        )
                    )
                }
            }
            api.build.list()
        }

    @Test
    fun update_build_name() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Put,
                            "/automate/builds/somehashid.json",
                            """
                            {"automation_build":{"name":"New Name","duration":null,"status":"failed",
                            "hashed_id":"somehashid","build_tag":null}}
                        """.trimIndent(),
                            requestBody = """
                            {"name":"New Name"}
                        """.trimIndent()
                        )
                    )
                }
            }
            api.build.updateBuild("somehashid", UpdateNameRequest("New Name"))
        }


    @Test
    fun get_sessions() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/builds/somehashid/sessions.json",
                            "[$DUMMY_SESSION]"
                        )
                    )
                }
            }
            api.session.list("somehashid")
        }


    @Test
    fun get_session_details() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/sessions/1337.json",
                            DUMMY_SESSION
                        )
                    )
                }
            }
            api.session.getSessionDetails("1337")
        }


    @Test
    fun get_session_logs() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/sessions/somehashid/logs",
                            "This is log ;)"
                        )
                    )
                }
            }
            api.session.getSessionLogs("somehashid")
        }


    @Test
    fun get_network_logs() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/sessions/somehashid/networklogs",
                            EXAMPLE_NETWORK_LOG
                        )
                    )
                }
            }
            api.session.getNetworkLogs("somehashid")
        }

    @Test
    fun get_console_logs() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/sessions/somehashid/consolelogs",
                            "This is log ;)"
                        )
                    )
                }
            }
            api.session.getConsoleLogs("somehashid")
        }

    @Test
    fun get_selenium_logs() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/sessions/somehashid/seleniumlogs",
                            "This is log ;)"
                        )
                    )
                }
            }
            api.session.getSeleniumLogs("somehashid")
        }

    @Test
    fun get_appium_logs() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Get,
                            "/automate/sessions/somehashid/appiumlogs",
                            "This is log ;)"
                        )
                    )
                }
            }
            api.session.getAppiumLogs("somehashid")
        }

    @Test
    fun set_test_status() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Put,
                            "/automate/sessions/somehashid.json",
                            DUMMY_SESSION,
                            requestBody = """
                            {"status":"failed","reason":"good"}
                            """.trimIndent()
                        )
                    )
                }
            }
            api.session.setTestStatus(
                "somehashid",
                StatusRequest(Status.FAILED, "good")
            )
        }

    @Test
    fun set_session_name() =
        runTest {
            val api = BrowserStackApi.create(user, access_key, MockEngine) {
                engine {
                    addHandler(
                        handleRequest(
                            HttpMethod.Put,
                            "/automate/sessions/somehashid.json",
                            DUMMY_SESSION,
                            requestBody = """
                            {"name":"NewName"}
                            """.trimIndent()
                        )
                    )
                }
            }
            api.session.setSessionName(
                "somehashid",
                UpdateNameRequest("NewName")
            )
        }

}

fun handleRequest(
    httpMethod: HttpMethod,
    path: String,
    content: String,
    contentType: ContentType = ContentType.Application.Json,
    requestBody: String? = null
): MockRequestHandler =
    { request ->
        if (request.method != httpMethod) {
            error("Invalid http method: ${request.method}")
        }
        if (request.url.encodedPath != path) {
            error("Invalid http path: ${request.url.encodedPath}")
        }
        if (requestBody != null) {
            if (!request.body.toByteArray().contentEquals(requestBody.toByteArray(Charset.forName("UTF-8")))) {
                error("Invalid request body: ${request.body}")
            }
        }
        respond(
            content,
            headers = headersOf(HttpHeaders.ContentType, contentType.toString())
        )
    }