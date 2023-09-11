import android.util.Log
import androidx.annotation.Keep
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
data class GeniusHit(val index: String,
                     val result: GeniusResult,
                     val type: String)
@Keep
data class GeniusResult(
    val annotation_count: Int,
    val api_path: String,
    val artist_names: String,
    val featured_artists: List<FeaturedArtist>,
    val full_title: String,
    val header_image_thumbnail_url: String,
    val header_image_url: String,
    val id: Int,
    val lyrics_owner_id: Int,
    val lyrics_state: String,
    val path: String,
    val primary_artist: PrimaryArtist,
    val pyongs_count: Int?,
    val relationships_index_url: String,
    val release_date_components: ReleaseDateComponents,
    val release_date_for_display: String,
    val release_date_with_abbreviated_month_for_display: String,
    val song_art_image_thumbnail_url: String,
    val song_art_image_url: String,
    val stats: Stats,
    val title: String,
    val title_with_featured: String,
    val url: String
)

@Keep
data class PrimaryArtist(
    val api_path: String,
    val header_image_url: String,
    val id: Int,
    val image_url: String,
    val is_meme_verified: Boolean,
    val is_verified: Boolean,
    val name: String,
    val url: String
)

@Keep
data class FeaturedArtist(
    val api_path: String,
    val header_image_url: String,
    val id: Int,
    val image_url: String,
    val is_meme_verified: Boolean,
    val is_verified: Boolean,
    val name: String,
    val url: String
)

@Keep
data class Stats(
    val hot: Boolean,
    val pageviews: Int?,
    val unreviewed_annotations: Int
)

@Keep
data class ReleaseDateComponents(
    val day: Int,
    val month: Int,
    val year: Int
)

fun cleanSongTitle(input: String): String {
    val regex = Regex("\\\\([^)]*\\\\)|\\(.*?\\)|\\[.*?]|\\\\[[^]]*]|\\#\\S+|Original|Movie|Mix|Official|_|\\|")
    Log.d("test", regex.replace(input, "").trim())
    return regex.replace(input, "").trim()
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
@Keep
suspend fun fetchGeniusResponse(artist: String,title: String):GeniusResponse?{
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
                return@withContext result?.response
            }
        } catch (e: Exception) {
            // Handle Retrofit exception here
            e.printStackTrace()
        }
        null
    }
}

@Keep
suspend fun fetchLyrics(geniusResponse: GeniusResponse, index: Int):String?{
    val hit = geniusResponse.hits.get(index)
    val songUrl = hit.result.url

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
        return lyricsDivs.joinToString("\n") { div ->
            div.childNodes().joinToString(separator = "") { extractText(it) }
        }
    } catch (e: Exception) {
        // Handle Jsoup exception here
        e.printStackTrace()
    }
    return null
}
