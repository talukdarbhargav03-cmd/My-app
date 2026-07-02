package com.example.api

import android.util.Log
import com.example.data.SavedProductEntity
import com.example.data.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit

object ProductApiService {
    private const val TAG = "ProductApiService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if a category is clothing, accessories, or skincare.
     */
    private fun isClothingOrAccessoryOrSkincare(category: String): Boolean {
        val cat = category.lowercase()
        
        // Explicitly exclude non-personal categories to prevent laptops matching "top", 
        // phone cases matching "accessory", etc.
        if (cat.contains("kitchen") || 
            cat.contains("mobile") || 
            cat.contains("sports") || 
            cat.contains("home") || 
            cat.contains("furniture") || 
            cat.contains("groceries") || 
            cat.contains("laptop") || 
            cat.contains("tablet") || 
            cat.contains("smartphone") || 
            cat.contains("vehicle") || 
            cat.contains("automotive") || 
            cat.contains("motorcycle") || 
            cat.contains("lighting")) {
            return false
        }

        return cat == "beauty" ||
               cat == "fragrance" ||
               cat == "skin-care" ||
               cat == "mens-shirts" ||
               cat == "mens-shoes" ||
               cat == "mens-watches" ||
               cat == "womens-bags" ||
               cat == "womens-dresses" ||
               cat == "womens-jewellery" ||
               cat == "womens-shoes" ||
               cat == "womens-watches" ||
               cat == "sunglasses" ||
               cat == "tops" ||
               (cat.contains("shirt") && !cat.contains("device")) ||
               cat.contains("shoe") ||
               cat.contains("dress") ||
               cat.contains("watch") ||
               cat.contains("bag") ||
               cat.contains("jewel") ||
               cat.contains("sunglasses") ||
               cat.contains("pant") ||
               cat.contains("coat") ||
               cat.contains("suit") ||
               cat.contains("skirt") ||
               cat.contains("apparel") ||
               (cat.contains("wear") && !cat.contains("hardware")) ||
               cat.contains("cloth") ||
               cat.contains("skincare") ||
               cat.contains("skin-care") ||
               (cat.contains("accessory") && !cat.contains("auto")) ||
               (cat.contains("accessories") && !cat.contains("auto")) ||
               cat.startsWith("mens-") ||
               cat.startsWith("womens-")
    }

    /**
     * Determines which collection a product belongs to:
     * - Fashion: Clothing, shoes, apparel
     * - Skincare: All products related to skincare, fragrance, beauty
     * - Accessories: Jewelry, bags, glasses, specs, shoes (shoes are mapped dynamically to both)
     */
    private fun determineCollection(category: String, title: String): String {
        val cat = category.lowercase()
        val t = title.lowercase()

        // 1. Skincare (all products related to skincare)
        if (cat.contains("beauty") || cat.contains("care") || cat.contains("skin") || cat.contains("fragrance") || cat.contains("perfume") ||
            t.contains("serum") || t.contains("cream") || t.contains("spf") || t.contains("sunscreen") || t.contains("skin") || t.contains("barrier")) {
            return "Skincare"
        }

        // 2. Accessories (jewelry, shoes, bags, specs, etc.)
        if (cat.contains("jewel") || cat.contains("bag") || cat.contains("sunglasses") || cat.contains("watch") || cat.contains("specs") || cat.contains("spectacles") ||
            t.contains("bag") || t.contains("glasses") || t.contains("jewel") || t.contains("watch") || t.contains("specs") || t.contains("spectacles") || t.contains("sunglasses")) {
            return "Accessories"
        }

        // 3. Shoes (user listed shoes in both Fashion and Accessories; map some to Fashion and some to Accessories)
        if (cat.contains("shoe") || t.contains("shoe") || cat.contains("sneaker") || t.contains("sneaker") || cat.contains("boot") || t.contains("boot")) {
            return if (t.hashCode() % 2 == 0) "Fashion" else "Accessories"
        }

        // 4. Fashion (all clothes, coats, pants, general outerwear)
        return "Fashion"
    }

    /**
     * Pulls products from the live external DummyJSON shopping API,
     * filters to strictly clothing/accessories/skincare,
     * and expands them into a massive catalog of thousands of generic entries,
     * dynamically personalized using the User's Profile Context.
     */
    suspend fun fetchMassiveProductCatalog(userProfile: UserProfileEntity?): List<SavedProductEntity> = withContext(Dispatchers.IO) {
        try {
            val url = "https://dummyjson.com/products?limit=200"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to pull live products. Response code: ${response.code}")
                return@withContext generateFallbackMassiveCatalog(userProfile)
            }

            val responseBody = response.body?.string() ?: return@withContext generateFallbackMassiveCatalog(userProfile)
            val jsonObject = JSONObject(responseBody)
            val jsonArray = jsonObject.optJSONArray("products") ?: return@withContext generateFallbackMassiveCatalog(userProfile)

            val baseProducts = mutableListOf<SavedProductEntity>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val category = item.optString("category", "General")
                
                // STRICT FILTERING FOR CLOTHING, ACCESSORIES, AND SKINCARE ONLY
                if (!isClothingOrAccessoryOrSkincare(category)) {
                    continue
                }

                val id = item.optInt("id", i).toString()
                val title = item.optString("title", "Product $id")
                val description = item.optString("description", "A premium, finely designed product.")
                val price = item.optDouble("price", 45.0)
                val rating = item.optDouble("rating", 4.5)
                val thumbnail = item.optString("thumbnail", "")

                // Tailor platform names dynamically based on user location context
                val platforms = getRegionalPlatforms(userProfile?.location, category)
                val pricesJson = buildPricesJson(platforms, price)

                baseProducts.add(
                    SavedProductEntity(
                        id = "api_$id",
                        title = title,
                        mainPrice = price,
                        pricesJson = pricesJson,
                        rating = rating,
                        reviewsCount = (10..450).random(),
                        imageUrl = thumbnail,
                        description = description,
                        collection = determineCollection(category, title)
                    )
                )
            }

            // If we have very few filtered products, seed with high-quality default clothes & skincare
            if (baseProducts.size < 8) {
                val defaultBasesRaw = listOf(
                    Triple("api_def_1", "Linen Wrap Tunic Set" to 118.0, Triple("img_ootd_minimalist", "Sophisticated draped linen wrap top and trouser pairing. Ultra-breathable and lightweight.", "Fashion")),
                    Triple("api_def_2", "Hydrating Cloud Serum" to 45.0, Triple("img_product_serum", "Deep nourishing hyaluronic acid and sea silt formulation. Keeps active hydration barrier secure.", "Skincare")),
                    Triple("api_def_3", "Matte Protection SPF 50" to 32.0, Triple("img_product_spf", "Broad-spectrum oil-free screen lotion. Dries down to a clean powdery matte velvet finish.", "Skincare")),
                    Triple("api_def_4", "Relaxed Organic Wool Overcoat" to 310.0, Triple("img_ootd_cozy_wool", "Ethically sourced heavy organic virgin wool overcoat with unstructured drop shoulders.", "Fashion")),
                    Triple("api_def_5", "Matte Barrier Calming Cream" to 54.0, Triple("img_product_cream", "Ceramide-rich rich barrier formulation that shields delicate facial layers from dry, cold environmental conditions.", "Skincare")),
                    Triple("api_def_6", "Tailored Suede Aviator Jacket" to 450.0, Triple("img_ootd_male_cold", "Premium buttery split-suede aviator-style shearling jacket. Beautiful warmth under crisp morning conditions.", "Fashion")),
                    Triple("api_def_7", "Fluid Linen Wide-Leg Trouser" to 85.0, Triple("img_ootd_neutral_hot", "Relaxed high-waisted wide-leg trousers crafted from the finest organic Belgian flax linen.", "Fashion"))
                )
                val defaultBases = defaultBasesRaw.map { (id, pair1, triple) ->
                    val (title, price) = pair1
                    val (img, desc, coll) = triple
                    val platforms = getRegionalPlatforms(userProfile?.location, coll)
                    val pricesJson = buildPricesJson(platforms, price)
                    SavedProductEntity(id, title, price, pricesJson, 4.8, (10..450).random(), img, desc, coll)
                }
                baseProducts.addAll(defaultBases)
            }

            Log.d(TAG, "Pulled ${baseProducts.size} base products. Now generating thousands of generic entries dynamically mapped to user context...")
            return@withContext expandCatalogToMassiveSize(baseProducts, userProfile)

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching live product catalog, falling back to fully generated catalog", e)
            return@withContext generateFallbackMassiveCatalog(userProfile)
        }
    }

    private fun formatCategoryToCollection(category: String): String {
        return category.replace("-", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    /**
     * Determines local boutique / luxury platforms depending on user location.
     */
    private fun getRegionalPlatforms(location: String?, category: String): List<String> {
        val loc = (location ?: "London").lowercase()
        val isSkincare = category.contains("beauty", true) || category.contains("fragrance", true) || category.contains("care", true) || category.contains("skin", true)

        return when {
            loc.contains("india") || loc.contains("mumbai") || loc.contains("delhi") || loc.contains("inr") || loc.contains("dibrugarh") || loc.contains("assam") -> {
                if (isSkincare) listOf("Nykaa", "Amazon Beauty", "Flipkart", "Meesho", "Tira Beauty", "Myntra Beauty")
                else listOf("Myntra", "Amazon India", "Flipkart", "Meesho", "Ajio")
            }
            loc.contains("london") || loc.contains("uk") || loc.contains("united kingdom") || loc.contains("gbp") -> {
                if (isSkincare) listOf("Boots UK", "Amazon UK", "Space NK", "Superdrug")
                else listOf("ASOS", "Amazon UK", "Zalando", "eBay UK", "Selfridges")
            }
            loc.contains("paris") || loc.contains("milan") || loc.contains("europe") || loc.contains("france") || loc.contains("italy") || loc.contains("spain") || loc.contains("germany") || loc.contains("eur") -> {
                if (isSkincare) listOf("Sephora Paris", "Amazon EU", "Douglas", "Nocibé")
                else listOf("Zalando", "Amazon EU", "Vinted", "About You", "Yoox")
            }
            loc.contains("tokyo") || loc.contains("japan") || loc.contains("jpy") -> {
                if (isSkincare) listOf("Cosme Tokyo", "Amazon JP", "Matsumoto Kiyoshi", "Sasa")
                else listOf("ZozoTown", "Amazon JP", "Rakuten", "Uniqlo", "Beams")
            }
            else -> {
                if (isSkincare) listOf("Sephora US", "Amazon Beauty", "Ulta", "Target")
                else listOf("Amazon Fashion", "Nordstrom", "ASOS US", "Macy's", "Shein")
            }
        }
    }

    private fun buildPricesJson(platforms: List<String>, basePrice: Double): String {
        val jsonArray = JSONArray()
        platforms.forEachIndexed { idx, p ->
            val factor = when (idx) {
                0 -> 0.98
                1 -> 1.05
                2 -> 1.00
                3 -> 0.92
                4 -> 1.02
                else -> 1.0
            }
            try {
                val pObj = JSONObject()
                pObj.put("platform", p)
                pObj.put("price", String.format(java.util.Locale.US, "%.2f", basePrice * factor).toDouble())
                jsonArray.put(pObj)
            } catch (e: Exception) {
                // ignore
            }
        }
        return jsonArray.toString()
    }

    /**
     * Helper to generate a unique, highly realistic product variation.
     */
    private fun generateProductAtIndex(i: Int, userProfile: UserProfileEntity?, idPrefix: String): SavedProductEntity {
        val colors = listOf("Onyx", "Ivory", "Emerald", "Sapphire", "Crimson", "Amber", "Slate", "Teal", "Blush", "Sage", "Alabaster", "Sand", "Midnight", "Olive", "Dusty Rose")
        val styles = listOf("Minimal Luxury", "Classic Fit", "Eco-Conscious Choice", "Essential Silhouette", "Modern Avant-Garde", "Deluxe Edition", "Signature Line", "Heritage Craft")

        // Fashion Components
        val fashionMaterials = listOf("Belgian Flax Linen", "Organic Cotton", "Raw Mulberry Silk", "Extra-Fine Merino Wool", "Soft Cashmere Blend", "Brushed Suede", "Vegan Cactus Leather", "Japanese Selvedge Denim", "Heavyweight Fleece", "Recycled Nylon", "Structured Poplin", "Bohemian Crochet")
        val fashionSilhouettes = listOf("Wrap Tunic", "Wide-Leg Trouser", "Unstructured Overcoat", "Aviator Jacket", "Oversized Knit Sweater", "Asymmetrical Midi Dress", "Tailored Pleated Blazer", "Cropped Utility Cargo", "Ribbed Mock-Neck", "Slouchy Lounge Pant", "Classic Oxford Shirt", "Sleek Trench Coat")

        // Skincare Components
        val skincareIngredients = listOf("Hyaluronic Acid", "Ceramide-3", "Niacinamide 10%", "Bakuchiol", "Centella Asiatica", "Vitamin C", "Squalane", "Beta-Glucan", "Mandelic Acid", "Prebiotics", "Peptides", "Salicylic Acid")
        val skincareFormulations = listOf("Cloud Serum", "Barrier Calming Cream", "Melting Gel Cleanser", "Daily Matte SPF 50", "Overnight Recovery Mask", "Exfoliating Pore Toner", "Nourishing Face Oil", "Revitalizing Eye Gel", "Hydration Mist", "Detoxifying Clay Masque")

        // Accessories Components
        val accessoryMaterials = listOf("Sterling Silver", "18K Gold-Plated", "Full-Grain Italian Leather", "Handcrafted Acetate", "Stainless Steel", "Brushed Titanium", "Freshwater Pearl", "Eco-Acetate", "Suede Trim")
        val accessoryTypes = listOf("Chronograph Watch", "Polarized Sunglasses", "Minimalist Cardholder", "Structured Tote Bag", "Crescent Shoulder Bag", "Statement Signet Ring", "Dainty Chain Necklace", "Braided Leather Belt", "Everyday Canvas Backpack", "Combat Leather Boots", "Comfort-Sole Sneakers", "Strap Heeled Sandals")

        val collection = when (i % 3) {
            0 -> "Fashion"
            1 -> "Skincare"
            else -> "Accessories"
        }

        val color = colors[i % colors.size]
        val style = styles[i % styles.size]
        val rating = (4.0 + (i % 11) * 0.1).coerceAtMost(5.0)
        val reviewsCount = (15..500).random()

        val title: String
        val price: Double
        val description: String
        val imageUrl: String

        when (collection) {
            "Fashion" -> {
                val material = fashionMaterials[(i / 3) % fashionMaterials.size]
                val silhouette = fashionSilhouettes[(i / 3) % fashionSilhouettes.size]
                title = "$color $material $silhouette ($style)"
                price = 45.0 + (i % 41) * 10.0
                
                val contextHighlight = if (userProfile != null) {
                    "Perfect fit for ${userProfile.gender} silhouettes (${userProfile.bodyShape}), size ${userProfile.clothingSize}."
                } else {
                    "Premium silhouette designed for contemporary, elegant aesthetics."
                }
                description = "An expertly tailored $material $silhouette designed for exceptional comfort, breathable drape, and premium styling. $contextHighlight"
                
                imageUrl = when {
                    silhouette.contains("coat", true) || material.contains("wool", true) -> "img_ootd_cozy_wool"
                    silhouette.contains("jacket", true) || material.contains("suede", true) -> "img_ootd_male_cold"
                    silhouette.contains("sweater", true) || material.contains("cashmere", true) -> "img_ootd_male_mild"
                    silhouette.contains("tunic", true) || silhouette.contains("dress", true) || silhouette.contains("wrap", true) -> "img_ootd_minimalist"
                    silhouette.contains("trouser", true) || silhouette.contains("pant", true) -> "img_ootd_neutral_hot"
                    silhouette.contains("shirt", true) || silhouette.contains("blazer", true) -> "img_ootd_smart_poplin"
                    else -> "img_ootd_male_hot"
                }
            }
            "Skincare" -> {
                val ingredient = skincareIngredients[(i / 3) % skincareIngredients.size]
                val formulation = skincareFormulations[(i / 3) % skincareFormulations.size]
                title = "$color $ingredient $formulation ($style)"
                price = 15.0 + (i % 22) * 5.0
                description = "Deeply nourishing formula featuring potent $ingredient in a refined $formulation suspension. Carefully crafted to protect delicate facial layers, balance your natural complexion, and restore cellular elasticity."
                
                imageUrl = when {
                    formulation.contains("serum", true) -> "img_product_serum"
                    formulation.contains("cream", true) || formulation.contains("cleanser", true) || formulation.contains("mask", true) -> "img_product_cream"
                    formulation.contains("spf", true) || formulation.contains("sunscreen", true) -> "img_product_spf"
                    else -> "img_product_serum"
                }
            }
            else -> {
                val material = accessoryMaterials[(i / 3) % accessoryMaterials.size]
                val type = accessoryTypes[(i / 3) % accessoryTypes.size]
                title = "$color $material $type ($style)"
                price = 25.0 + (i % 33) * 10.0
                description = "Elegant luxury $material $type meticulously handcrafted to perfect your look. Accentuate your daily style with this statement companion, featuring durable hardware and timeless aesthetic appeal."
                
                imageUrl = when {
                    type.contains("boots", true) || type.contains("sneakers", true) || type.contains("shoes", true) -> "img_ootd_male_mild"
                    type.contains("glasses", true) || type.contains("sunglasses", true) -> "img_ootd_male_hot"
                    type.contains("watch", true) || type.contains("belt", true) || type.contains("bag", true) -> "img_ootd_neutral_hot"
                    else -> "img_ootd_minimalist"
                }
            }
        }

        val platforms = getRegionalPlatforms(userProfile?.location, collection)
        val pricesJson = buildPricesJson(platforms, price)

        return SavedProductEntity(
            id = "${idPrefix}_$i",
            title = title,
            mainPrice = price,
            pricesJson = pricesJson,
            rating = rating,
            reviewsCount = reviewsCount,
            imageUrl = imageUrl,
            description = description,
            collection = collection
        )
    }

    /**
     * Takes the base high-quality live entries and expands them to 1,200+ products,
     * personalizing style tags, description highlights, and custom collections based on user profile.
     */
    private fun expandCatalogToMassiveSize(base: List<SavedProductEntity>, userProfile: UserProfileEntity?): List<SavedProductEntity> {
        val massiveList = mutableListOf<SavedProductEntity>()
        massiveList.addAll(base)

        var i = base.size
        while (massiveList.size < 1250) {
            massiveList.add(generateProductAtIndex(i, userProfile, "api_gen"))
            i++
        }

        return massiveList
    }

    /**
     * Fallback catalog generator in case of network unavailability.
     */
    private fun generateFallbackMassiveCatalog(userProfile: UserProfileEntity?): List<SavedProductEntity> {
        val list = mutableListOf<SavedProductEntity>()
        for (i in 0 until 1250) {
            list.add(generateProductAtIndex(i, userProfile, "fallback_gen"))
        }
        return list
    }
}
