package me.darkweird.browserstack.har

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


/**
 * Root of HAR format. Version 1.2
 * See: https://w3c.github.io/web-performance/specs/HAR/Overview.html
 * TODO handle custom fields (ignore if fields starts with underscore
 */
@Serializable
data class HAR(
    val log: Log
)

@Serializable
data class Log(
    val version: String,
    val creator: Creator,
    val browser: Browser? = null,
    val pages: List<Page>? = null,
    val entries: List<Entry>,
    val comment: String? = null,
)

@Serializable
data class Creator(
    val name: String,
    val version: String,
    val comment: String? = null
)

@Serializable
data class Browser(
    val name: String,
    val version: String,
    val comment: String? = null
)

@Serializable
data class Page(
    val startedDateTime: String, // TODO datetime
    val id: String,
    val title: String,
    val pageTimings: PageTimings,
    val comment: String? = null
)

@Serializable
data class PageTimings(
    val onContentLoad: Int = -1,
    val onLoad: Int = -1,
    val comment: String? = null
)

@Serializable
data class Entry(
    val pageref: String? = null,
    val startedDateTime: String, // TODO datetime
    val time: Int,
    val request: EntryRequest,
    val response: EntryResponse,
    val cache: Cache,
    val timings: Timing,
    val serverIPAddress: String? = null, // TODO provide IP address or String is enough
    val connection: String? = null,
    val comment: String? = null
)


@Serializable
data class EntryRequest(
    val method: String, // TODO create enum
    val url: String,
    val httpVersion: String, // TODO enum?
    val cookies: List<Cookie>,
    val headers: List<Header>,
    val queryString: List<QueryString>,
    val postData: PostData? = null,
    val headersSize: Int = -1,
    val bodySize: Int = -1,
    val comment: String? = null
)

@Serializable
data class EntryResponse(
    val status: UInt,
    val statusText: String,
    val httpVersion: String, // TODO enum?
    val cookies: List<Cookie>,
    val headers: List<Header>,
    val content: Content,
    val redirectURL: String, // TODO why not optional?
    val headersSize: Int = -1,
    val bodySize: Int = -1,
    val comment: String?
)

@Serializable
data class Cookie(
    val name: String,
    val value: String,
    val path: String? = null,
    val domain: String? = null,
    val expires: LocalDateTime? = null,
    val httpOnly: Boolean? = null,
    val secure: Boolean? = null,
    val comment: String
)

@Serializable
data class Header(
    val name: String,
    val value: String,
    val comment: String? = null
)

@Serializable
data class QueryString(
    val name: String,
    val value: String,
    val comment: String? = null
)

@Serializable
data class PostData(
    val mimeType: String,
    val params: List<PostDataParam>,
    val text: String,
    val comment: String? = null
)

@Serializable
data class PostDataParam(
    val name: String,
    val value: String? = null,
    val fileName: String? = null,
    val contentType: String? = null,
    val comment: String? = null,
)

@Serializable
data class Content(
    val size: UInt,
    val compression: UInt? = null,
    val mimeType: String? = null,
    val encoding: String? = null,
    val comment: String? = null,
)

@Serializable
data class Cache(
    val beforeRequest: CacheEntry? = null,
    val afterRequest: CacheEntry? = null,
    val comment: String? = null,
)

@Serializable
data class CacheEntry(
    val expires: LocalDateTime? = null, // TODO check format
    val lastAccess: LocalDateTime,
    val eTag: String,
    val hitCount: UInt,
    val comment: String? = null,
)

@Serializable
data class Timing(
    val blocked: Int = -1,
    val dns: Int = -1,
    val connect: Int = -1,
    val send: Int,
    val wait: Int,
    val receive: Int,
    val ssl: Int = -1,
    val comment: String? = null,
)
