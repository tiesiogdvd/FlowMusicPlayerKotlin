import android.util.Log
import androidx.annotation.Keep
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tiesiogdvd.composetest.BuildConfig
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.get
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType.Application.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.apache.commons.text.similarity.JaroWinklerDistance
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode

@Keep
interface GeniusApiService {
    @Headers("Authorization: Bearer ${BuildConfig.API_KEY}")
    @GET("search")
    fun searchSong(@Query("q") query: String): Call<GeniusSearchResult>
}

@Keep
data class GeniusSearchResult(val response: GeniusResponse)
@Keep
data class GeniusResponse(val hits: List<GeniusHit>)
@Keep
data class GeniusHit(val result: GeniusResult)
@Keep
data class GeniusResult(val url: String)

fun cleanSongTitle(input: String): String {
    val regex = Regex("\\\\([^)]*\\\\)|\\(.*?\\)|\\[.*?]|\\\\[[^]]*]|\\#\\S+|Original|Movie|Mix|Official|_|\\|")
    Log.d("test", regex.replace(input, "").trim())
    return regex.replace(input, "").trim()
}

fun b(input: String): Boolean {
    val regex = Regex(pattern = "Glass Animals \\| It's All So Incredibly Loud \\([^)]*\\)  \\{[^}]*\\} ! ~ \\| \\[[^\\]]*\\] #", options = setOf(RegexOption.IGNORE_CASE))
    return regex.matches(input)
}

fun a(input: String):String{
    val regex = Regex(pattern = "\\|.*\\([^)]*\\).*\\{[^}]*\\}.*!.*~.*\\|.*\\[[^\\]]*\\].*#")
    Log.d("test", regex.replace(input, "").trim())
    return regex.replace(input, "").trim()
}

fun splitByHyphen(input: String): Pair<String, String> {
    val hyphenIndex = input.indexOf('-')

    return if (hyphenIndex != -1) {
        val part1 = input.substring(0, hyphenIndex).trim()
        val part2 = input.substring(hyphenIndex + 1).trim()
        Pair(part1, part2)
    } else {
        Pair(input, "")
    }
}

suspend fun fetchLyrics(artist: String, title: String): String? {
    return withContext(Dispatchers.IO) {
        val query = "$artist $title"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.genius.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GeniusApiService::class.java)
        val response = service.searchSong(query).execute()

        if (response.isSuccessful) {
            val result = response.body()
            if (result != null && result.response.hits.isNotEmpty()) {
                val songUrl = result.response.hits[0].result.url

                val document = Jsoup.connect(songUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.39")
                    .get()
                val lyricsDiv = document.selectFirst("div[class*=Lyrics__Container-]")

                return@withContext lyricsDiv?.text()
            }
        }

        null
    }
}

suspend fun fetchLyrics2(artist: String, title: String): String? {
    return withContext(Dispatchers.IO) {
        val query = "$artist $title"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.genius.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GeniusApiService::class.java)
        val response = service.searchSong(query).execute()

        if (response.isSuccessful) {
            val result = response.body()
            if (result != null && result.response.hits.isNotEmpty()) {
                val songUrl = result.response.hits[0].result.url

                val document = Jsoup.connect(songUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.39")
                    .get()
                val lyricsDiv = document.selectFirst("div[class*=Lyrics__Container-]")

                // Get the lyrics with HTML formatting and replace <br> tags with newline characters
                val lyricsHtml = lyricsDiv?.html()
                val lyrics = lyricsHtml?.replace("<br>", "\n", ignoreCase = true)

                return@withContext lyrics
            }
        }

        null
    }
}

suspend fun fetchLyrics3(artist: String, title: String): String? {
    return withContext(Dispatchers.IO) {
        val query = "$artist $title"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.genius.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GeniusApiService::class.java)
        val response = service.searchSong(query).execute()

        if (response.isSuccessful) {
            val result = response.body()
            if (result != null && result.response.hits.isNotEmpty()) {
                val songUrl = result.response.hits[0].result.url

                val document = Jsoup.connect(songUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.39")
                    .get()
                val lyricsDiv = document.selectFirst("div[class*=Lyrics__Container-]")

                // Remove all unwanted HTML elements
                lyricsDiv?.select("a")?.remove()
                lyricsDiv?.select("span")?.remove()

                // Get the lyrics with HTML formatting and replace <br> tags with newline characters
                val lyricsHtml = lyricsDiv?.html()
                val lyrics = lyricsHtml?.replace("<br>", "\n", ignoreCase = true)?.replace("&nbsp;", " ")

                return@withContext lyrics
            }
        }

        null
    }
}

@Keep
suspend fun fetchLyrics4(artist: String, title: String): String? {
    return withContext(Dispatchers.IO) {
        var query = ""
        if(artist!=""){
            query = "$artist  ${cleanSongTitle(title)}"
        }else{
            val (artistName,songTitle) = splitByHyphen(title)
            query = "$artistName  ${cleanSongTitle(songTitle)}"
        }
        Log.d("testing lyrics", query)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.genius.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

        val service = retrofit.create(GeniusApiService::class.java)
        try {
            val response = service.searchSong(query).execute()
            Log.d("testing lyrics", response.code().toString())

            if (response.isSuccessful) {
                val result = response.body()
                if (result != null && result.response.hits.isNotEmpty()) {
                    val similarity = JaroWinklerDistance()
                    val songUrl = result.response.hits
                        .maxByOrNull { hit -> similarity.apply(query.lowercase(), hit.result.url.lowercase()) }
                        ?.result
                        ?.url

                    try {
                        val document = Jsoup.connect(songUrl)
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.39")
                            .get()
                        val lyricsDivs = document.select("div[class*=Lyrics__Container-]")

                        fun extractText(node: Node): String {
                            return when (node) {
                                is TextNode -> node.text()
                                is Element -> {
                                    if (node.tagName() == "br") "\n"
                                    else {
                                        val children = node.childNodes()
                                        if (children.isNotEmpty()) {
                                            children.joinToString(separator = "") { extractText(it) }
                                        } else {
                                            node.text()
                                        }
                                    }
                                }
                                else -> ""
                            }
                        }

                        // Join text nodes and replace <br> tags with newlines
                        return@withContext lyricsDivs.joinToString("\n") { div ->
                            div.childNodes().joinToString(separator = "") { extractText(it) }
                        }
                    } catch (e: Exception) {
                        // Handle Jsoup exception here
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            // Handle Retrofit exception here
            e.printStackTrace()
        }

        null
    }
}










suspend fun fetchLyricsWithKtor(songUrl: String): String? {
    val client = HttpClient(Android) {
        UserAgent("")
        BrowserUserAgent()
        expectSuccess = false
    }

    val response: HttpResponse = client.get(songUrl)

    if (response.status.value == 403) {
        print("no success")
        // Handle 403 error
        return null
    }

    val htmlContent = response.readText()
    val document = Jsoup.parse(htmlContent)

    // Extract lyrics
    val lyricsDiv = document.selectFirst("div[class^=Lyrics__Container]")
    val lyrics = lyricsDiv?.wholeText()?.trim()

    print(lyrics)

    return lyrics
}


suspend fun fetchLyricsWithKtor2(artist: String, title: String): String? {
    val client = HttpClient(Android) {
        UserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.39")
        //BrowserUserAgent()
        expectSuccess = false
    }

    val geniusApiUrl = "https://api.genius.com"
    val apiKey = BuildConfig.API_KEY

    // Search for the song
    val searchUrl = "$geniusApiUrl/search?q=${artist}+${title}&access_token=$apiKey"
    val searchResponse: HttpResponse = client.get(searchUrl)

    if (searchResponse.status.value != 200) {
        // Handle unsuccessful response
        return null
    }

    val jsonResponse = searchResponse.readText()
    val json = Json { ignoreUnknownKeys = true }
    val jsonObject = json.parseToJsonElement(jsonResponse).jsonObject

    // Get song URL
    val songUrl = jsonObject["response"]
        ?.jsonObject?.get("hits")
        ?.jsonArray?.get(0)
        ?.jsonObject?.get("result")
        ?.jsonObject?.get("url")
        ?.toString()
        ?.replace("\"", "")

    println(songUrl)

    if (songUrl == null) {
        // Handle no song URL found
        return null
    }

    // Fetch lyrics
    val lyricsResponse: HttpResponse = client.get(songUrl)

    if (lyricsResponse.status.value != 200) {
        println(lyricsResponse.status.value)
        // Handle unsuccessful response
        return null
    }



    val htmlContent = lyricsResponse.readText()
    val document = Jsoup.parse(htmlContent)

    // Extract lyrics
    val lyricsDiv = document.selectFirst("div[class^=Lyrics__Container]")
    val lyrics = lyricsDiv?.wholeText()?.trim()

    println(lyricsDiv)

    return lyrics
}
