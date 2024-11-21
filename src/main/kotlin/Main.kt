
package org.example

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import okhttp3.OkHttpClient
import okhttp3.Request

fun main() {
    println("Liste des super héros compatibles : https://www.superheroapi.com/ids.html")
    println("Donnez le nom d'un super héros :")
    val hero1 = readlnOrNull()?.trim() ?: ""
    println("Donnez le nom d'un 2ème super héros :")
    val hero2 = readlnOrNull()?.trim() ?: ""

    if (hero1.length < 4 || hero2.length < 4) {
        println("Les noms doivent contenir au moins 4 caractères non espacés.")
        return
    }

    val score1 = fetchHeroIntelligence(hero1).also {
        println("url : https://www.superheroapi.com/api.php/10224979642372552/search/${hero1.replace(" ", "%20")}")
    }
    val score2 = fetchHeroIntelligence(hero2).also {
        println("url : https://www.superheroapi.com/api.php/10224979642372552/search/${hero2.replace(" ", "%20")}")
    }

    when {
        score1 > score2 -> println("$hero1 (${score1}) - $hero2 (${score2})\n$hero1 est plus intelligent")
        score2 > score1 -> println("$hero1 (${score1}) - $hero2 (${score2})\n$hero2 est plus intelligent")
        else -> println("$hero1 (${score1}) - $hero2 (${score2})\nLes deux héros ont la même intelligence")
    }
}

fun fetchHeroIntelligence(heroName: String): Double {
    val apiKey = "10224979642372552"
    val url = "https://www.superheroapi.com/api.php/$apiKey/search/${heroName.replace(" ", "%20")}"

    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    return try {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Erreur réseau : ${response.code}")

            val responseBody = response.body?.string()
            val gson = Gson()
            val json = gson.fromJson(responseBody, JsonObject::class.java)

            val results: JsonArray = json.getAsJsonArray("results")
            val scores = mutableListOf<Int>()

            for (result in results) {
                val hero = result.asJsonObject
                val powerstats = hero.getAsJsonObject("powerstats")
                val intelligence = powerstats.get("intelligence").asString

                if (intelligence != "null") {
                    scores.add(intelligence.toInt())
                }
            }

            if (scores.isEmpty()) 0.0 else scores.average()
        }
    } catch (e: Exception) {
        println("Erreur lors de l'appel à l'API pour $heroName : ${e.message}")
        0.0
    }
}
