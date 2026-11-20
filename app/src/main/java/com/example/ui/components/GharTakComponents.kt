package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CityApprovalRequest
import com.example.data.model.GharTakConstants
import com.example.ui.theme.GharTakAmber
import com.example.ui.theme.GharTakCoralError
import com.example.ui.theme.GharTakEmerald
import com.example.ui.theme.GharTakPink
import com.example.ui.theme.GharTakPinkDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private val BUNDLED_IMAGE_DRAWABLES: Map<String, Int> = mapOf(
    "res://img_hero_food" to R.drawable.img_hero_food,
    "res://img_hero_grocery" to R.drawable.img_hero_grocery,
    "res://img_banner_deals" to R.drawable.img_banner_deals,
    "res://img_empty_cart" to R.drawable.img_empty_cart,
    "res://img_groc_fruits" to R.drawable.img_groc_fruits,
    "res://img_groc_vegetables" to R.drawable.img_groc_vegetables,
    "res://img_groc_milk" to R.drawable.img_groc_milk,
    "res://img_groc_dairy" to R.drawable.img_groc_dairy,
    "res://img_groc_meat" to R.drawable.img_groc_meat,
    "res://img_groc_chicken" to R.drawable.img_groc_chicken,
    "res://img_groc_rice" to R.drawable.img_groc_rice,
    "res://img_groc_flour" to R.drawable.img_groc_flour,
    "res://img_groc_oil" to R.drawable.img_groc_oil,
    "res://img_groc_ghee" to R.drawable.img_groc_ghee,
    "res://img_groc_spices" to R.drawable.img_groc_spices,
    "res://img_groc_bakery" to R.drawable.img_groc_bakery,
    "res://img_groc_beverages" to R.drawable.img_groc_beverages,
    "res://img_groc_snacks" to R.drawable.img_groc_snacks,
    "res://img_groc_frozen" to R.drawable.img_groc_frozen,
    "res://img_groc_personal_care" to R.drawable.img_groc_personal_care,
    "res://img_groc_household" to R.drawable.img_groc_household,
    "res://img_food_pizza" to R.drawable.img_food_pizza,
    "res://img_food_burger" to R.drawable.img_food_burger,
    "res://img_food_biryani" to R.drawable.img_food_biryani,
    "res://img_food_bbq" to R.drawable.img_food_bbq,
    "res://img_food_chinese" to R.drawable.img_food_chinese,
    "res://img_food_fastfood" to R.drawable.img_food_fastfood,
    "res://img_food_pakistani" to R.drawable.img_food_pakistani,
    "res://img_food_shawarma" to R.drawable.img_food_shawarma,
    "res://img_food_desserts" to R.drawable.img_food_desserts,
    "res://img_food_drinks" to R.drawable.img_food_drinks,
    "res://img_rest_royal_biryani" to R.drawable.img_rest_royal_biryani,
    "res://img_rest_baghdad_grill" to R.drawable.img_rest_baghdad_grill,
    "res://img_rest_sadiq_spice" to R.drawable.img_rest_sadiq_spice,
    "res://img_rest_noor_mahal" to R.drawable.img_rest_noor_mahal,
    "res://img_store_almadina" to R.drawable.img_store_almadina,
    "res://img_store_victoria" to R.drawable.img_store_victoria,
    "res://img_store_mega_mart" to R.drawable.img_store_mega_mart,
    "res://img_store_green_basket" to R.drawable.img_store_green_basket,
    "res://img_store_chenab" to R.drawable.img_store_chenab,
    "res://img_hero_medical" to R.drawable.img_hero_medical,
    "res://img_store_pharmacy" to R.drawable.img_store_pharmacy,
    "res://img_med_medicines" to R.drawable.img_med_medicines,
    "res://img_med_firstaid" to R.drawable.img_med_firstaid,
    "res://img_med_vitamins" to R.drawable.img_med_vitamins
)

private val RESTAURANT_GALLERY_URIS = listOf(
    "res://img_rest_royal_biryani",
    "res://img_rest_baghdad_grill",
    "res://img_rest_sadiq_spice",
    "res://img_rest_noor_mahal"
)

private val GROCERY_STORE_GALLERY_URIS = listOf(
    "res://img_store_almadina",
    "res://img_store_victoria",
    "res://img_store_mega_mart",
    "res://img_store_green_basket",
    "res://img_store_chenab"
)

fun restaurantImageUri(name: String, rawImageUrl: String? = null): String {
    if (!rawImageUrl.isNullOrBlank() && rawImageUrl != "res://img_hero_food") {
        return rawImageUrl
    }
    val lower = name.trim().lowercase()
    return when {
        lower.contains("biryani") || lower.contains("bbq") || lower.contains("tikka") -> "res://img_rest_royal_biryani"
        lower.contains("pizza") || lower.contains("burger") || lower.contains("grill") || lower.contains("baghdad") -> "res://img_rest_baghdad_grill"
        lower.contains("spice") || lower.contains("sadiq") || lower.contains("lahori") || lower.contains("desi") -> "res://img_rest_sadiq_spice"
        lower.contains("mahal") || lower.contains("cafe") || lower.contains("karahi") -> "res://img_rest_noor_mahal"
        else -> {
            val idx = kotlin.math.abs(lower.hashCode()) % RESTAURANT_GALLERY_URIS.size
            RESTAURANT_GALLERY_URIS[idx]
        }
    }
}

fun restaurantDrawableRes(name: String, rawImageUrl: String? = null): Int {
    val uri = restaurantImageUri(name, rawImageUrl)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_rest_royal_biryani
}

fun groceryStoreImageUri(name: String, rawImageUrl: String? = null): String {
    if (!rawImageUrl.isNullOrBlank() && rawImageUrl != "res://img_hero_grocery") {
        return rawImageUrl
    }
    val lower = name.trim().lowercase()
    return when {
        lower.contains("madina") || lower.contains("fresh") -> "res://img_store_almadina"
        lower.contains("victoria") || lower.contains("bazaar") || lower.contains("bazar") -> "res://img_store_victoria"
        lower.contains("mega") || lower.contains("super") || lower.contains("hyper") -> "res://img_store_mega_mart"
        lower.contains("green") || lower.contains("organic") || lower.contains("basket") -> "res://img_store_green_basket"
        lower.contains("chenab") || lower.contains("cash") || lower.contains("carry") -> "res://img_store_chenab"
        else -> {
            val idx = kotlin.math.abs(lower.hashCode()) % GROCERY_STORE_GALLERY_URIS.size
            GROCERY_STORE_GALLERY_URIS[idx]
        }
    }
}

fun groceryStoreDrawableRes(name: String, rawImageUrl: String? = null): Int {
    val uri = groceryStoreImageUri(name, rawImageUrl)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_store_almadina
}

fun foodCategoryImageUri(categoryName: String, itemName: String = ""): String {
    val cat = categoryName.trim().lowercase()
    val item = itemName.trim().lowercase()
    return when {
        cat.contains("pizza") -> "res://img_food_pizza"
        cat.contains("burger") -> "res://img_food_burger"
        cat.contains("biryani") -> "res://img_food_biryani"
        cat.contains("bbq") || cat.contains("barbecue") || cat.contains("grill") -> "res://img_food_bbq"
        cat.contains("chinese") || cat.contains("asian") -> "res://img_food_chinese"
        cat.contains("fast") -> "res://img_food_fastfood"
        cat.contains("pakistani") || cat.contains("desi") || cat.contains("karahi") -> "res://img_food_pakistani"
        cat.contains("shawarma") || cat.contains("wrap") || cat.contains("roll") -> "res://img_food_shawarma"
        cat.contains("dessert") || cat.contains("sweet") || cat.contains("mithai") -> "res://img_food_desserts"
        cat.contains("drink") || cat.contains("beverage") || cat.contains("shake") || cat.contains("juice") -> "res://img_food_drinks"
        item.contains("pizza") -> "res://img_food_pizza"
        item.contains("burger") || item.contains("zinger") -> "res://img_food_burger"
        item.contains("biryani") -> "res://img_food_biryani"
        item.contains("shawarma") -> "res://img_food_shawarma"
        item.contains("kabab") || item.contains("bbq") || item.contains("tikka") -> "res://img_food_bbq"
        item.contains("karahi") || item.contains("nihari") || item.contains("haleem") -> "res://img_food_pakistani"
        item.contains("manchurian") || item.contains("chow mein") || item.contains("noodle") -> "res://img_food_chinese"
        item.contains("fries") || item.contains("nugget") || item.contains("broast") -> "res://img_food_fastfood"
        item.contains("gulab") || item.contains("kheer") || item.contains("cake") || item.contains("ice cream") -> "res://img_food_desserts"
        item.contains("lassi") || item.contains("margarita") || item.contains("cola") || item.contains("juice") -> "res://img_food_drinks"
        else -> "res://img_hero_food"
    }
}

fun foodCategoryDrawableRes(categoryName: String, itemName: String = ""): Int {
    val uri = foodCategoryImageUri(categoryName, itemName)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_hero_food
}

fun groceryCategoryImageUri(categoryName: String, productName: String = ""): String {
    val cat = categoryName.trim().lowercase()
    val prod = productName.trim().lowercase()
    return when {
        cat.contains("fruit") -> "res://img_groc_fruits"
        cat.contains("vegetable") -> "res://img_groc_vegetables"
        cat.contains("milk") || cat.contains("egg") -> "res://img_groc_milk"
        cat.contains("dairy") -> "res://img_groc_dairy"
        cat.contains("chicken") -> "res://img_groc_chicken"
        cat.contains("meat") -> "res://img_groc_meat"
        cat.contains("fish") -> "res://img_groc_frozen"
        prod.contains("atta") || prod.contains("flour") || prod.contains("maida") || prod.contains("besan") -> "res://img_groc_flour"
        cat.contains("rice") -> "res://img_groc_rice"
        cat.contains("flour") -> "res://img_groc_flour"
        prod.contains("ghee") -> "res://img_groc_ghee"
        cat.contains("oil") -> "res://img_groc_oil"
        cat.contains("ghee") -> "res://img_groc_ghee"
        cat.contains("spice") || cat.contains("masala") -> "res://img_groc_spices"
        cat.contains("bakery") || cat.contains("bread") || cat.contains("breakfast") -> "res://img_groc_bakery"
        cat.contains("beverage") || cat.contains("drink") -> "res://img_groc_beverages"
        cat.contains("snack") -> "res://img_groc_snacks"
        cat.contains("frozen") -> "res://img_groc_frozen"
        cat.contains("personal") || cat.contains("baby") -> "res://img_groc_personal_care"
        cat.contains("household") || cat.contains("clean") -> "res://img_groc_household"
        else -> "res://img_hero_grocery"
    }
}

fun groceryCategoryDrawableRes(categoryName: String, productName: String = ""): Int {
    val uri = groceryCategoryImageUri(categoryName, productName)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_hero_grocery
}

fun medicalStoreImageUri(name: String, rawImageUrl: String? = null): String {
    if (!rawImageUrl.isNullOrBlank() && rawImageUrl != "res://img_hero_medical") {
        return rawImageUrl
    }
    return "res://img_store_pharmacy"
}

fun medicalStoreDrawableRes(name: String, rawImageUrl: String? = null): Int {
    val uri = medicalStoreImageUri(name, rawImageUrl)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_store_pharmacy
}

fun medicalCategoryImageUri(categoryName: String, productName: String = ""): String {
    val cat = categoryName.trim().lowercase()
    val prod = productName.trim().lowercase()
    return when {
        cat.contains("first aid") || cat.contains("surgical") || cat.contains("device") ||
            prod.contains("bandage") || prod.contains("dettol") || prod.contains("thermometer") ||
            prod.contains("monitor") || prod.contains("gauze") -> "res://img_med_firstaid"
        cat.contains("vitamin") || cat.contains("supplement") || cat.contains("baby") ||
            prod.contains("vitamin") || prod.contains("calcium") || prod.contains("gripe") ||
            prod.contains("centrum") || prod.contains("ors") -> "res://img_med_vitamins"
        cat.contains("tablet") || cat.contains("syrup") || cat.contains("pain") ||
            cat.contains("diabetes") || cat.contains("heart") || cat.contains("skin") -> "res://img_med_medicines"
        else -> "res://img_hero_medical"
    }
}

fun medicalCategoryDrawableRes(categoryName: String, productName: String = ""): Int {
    val uri = medicalCategoryImageUri(categoryName, productName)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_med_medicines
}

fun multanItemImageUri(brandName: String, categoryName: String = "", itemName: String = ""): String {
    val brand = brandName.trim().lowercase()
    val cat = categoryName.trim().lowercase()
    val item = itemName.trim().lowercase()
    return when {
        brand.contains("crumble") || cat.contains("cookie") || item.contains("cookie") -> "res://img_food_desserts"
        brand.contains("kfc") || cat.contains("fast") || item.contains("zinger") || item.contains("twister") || item.contains("burger") -> "res://img_food_fastfood"
        brand.contains("hafiz") || brand.contains("halwa") || brand.contains("rewari") || cat.contains("sweet") -> "res://img_food_desserts"
        brand.contains("zanzibar") || cat.contains("bbq") || item.contains("steak") || item.contains("platter") -> "res://img_food_bbq"
        cat.contains("drink") || cat.contains("cafe") || item.contains("coffee") || item.contains("shake") -> "res://img_food_drinks"
        else -> "res://img_banner_deals"
    }
}

fun multanItemDrawableRes(brandName: String, categoryName: String = "", itemName: String = ""): Int {
    val uri = multanItemImageUri(brandName, categoryName, itemName)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_banner_deals
}

fun celebrationItemImageUri(categoryName: String, itemName: String = ""): String {
    val cat = categoryName.trim().lowercase()
    val item = itemName.trim().lowercase()
    return when {
        cat.contains("cake") || item.contains("cake") || item.contains("cupcake") -> "res://img_food_desserts"
        cat.contains("bakery") || item.contains("chocolate") -> "res://img_groc_bakery"
        cat.contains("combo") || cat.contains("party") -> "res://img_food_fastfood"
        cat.contains("flower") || cat.contains("bouquet") || cat.contains("gift") || cat.contains("balloon") -> "res://img_banner_deals"
        else -> "res://img_food_desserts"
    }
}

fun celebrationItemDrawableRes(categoryName: String, itemName: String = ""): Int {
    val uri = celebrationItemImageUri(categoryName, itemName)
    return BUNDLED_IMAGE_DRAWABLES[uri] ?: R.drawable.img_food_desserts
}

@Composable
fun GharTakImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    isGroceryFallback: Boolean = false,
    isMedicalFallback: Boolean = false
) {
    val bundledResId = imageUrl?.let { BUNDLED_IMAGE_DRAWABLES[it] }
    when {
        bundledResId != null -> Image(
            painter = painterResource(id = bundledResId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        imageUrl != null && imageUrl.startsWith("data:image/") -> {
            val bitmap = remember(imageUrl) {
                runCatching {
                    val base64Part = imageUrl.substringAfter("base64,", "")
                    val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }.getOrNull()
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                FallbackImageBox(contentDescription, modifier, isGroceryFallback, isMedicalFallback)
            }
        }
        !imageUrl.isNullOrBlank() && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) -> {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
        else -> FallbackImageBox(contentDescription, modifier, isGroceryFallback, isMedicalFallback)
    }
}

@Composable
private fun FallbackImageBox(
    contentDescription: String,
    modifier: Modifier = Modifier,
    isGrocery: Boolean,
    isMedical: Boolean = false
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                when {
                    isMedical -> listOf(Color(0xFF00796B), Color(0xFF004D40))
                    isGrocery -> listOf(Color(0xFF13884B), Color(0xFF0A4D2A))
                    else -> listOf(GharTakPink, GharTakPinkDark)
                }
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when {
                isMedical -> Icons.Filled.LocalPharmacy
                isGrocery -> Icons.Filled.ShoppingBasket
                else -> Icons.Filled.Fastfood
            },
            contentDescription = contentDescription,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(32.dp)
        )
    }
}

private val SUPPORTED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
private val SUPPORTED_FILE_MIMES = arrayOf("image/jpeg", "image/png", "image/webp")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoPickerUploadRow(
    imageUrl: String,
    onImageChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Product Image (Gallery / Files / Camera)",
    isGroceryFallback: Boolean = false,
    isMedicalFallback: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var uploadSuccessMsg by remember { mutableStateOf<String?>(null) }

    fun handlePickedUri(uri: Uri?) {
        if (uri == null) return
        isProcessing = true
        uploadProgress = 0.15f
        uploadError = null
        uploadSuccessMsg = null
        scope.launch {
            uploadProgress = 0.45f
            val result = encodeUriToCompressedDataUri(context, uri, maxBytes = 105_000, maxDim = 420)
            uploadProgress = 0.85f
            delay(80)
            isProcessing = false
            uploadProgress = 1f
            result.onSuccess { encoded ->
                onImageChanged(encoded)
                uploadSuccessMsg = "Image validated & ready (JPG/PNG/WEBP)"
            }.onFailure { err ->
                uploadError = err.message ?: "Unsupported file. Please choose JPG, JPEG, PNG, or WEBP."
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> handlePickedUri(uri) }

    val filesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> handlePickedUri(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            isProcessing = true
            uploadProgress = 0.3f
            uploadError = null
            uploadSuccessMsg = null
            scope.launch {
                uploadProgress = 0.7f
                val encoded = encodeBitmapToCompressedDataUri(bitmap, maxBytes = 105_000, maxDim = 420)
                isProcessing = false
                uploadProgress = 1f
                if (encoded != null) {
                    onImageChanged(encoded)
                    uploadSuccessMsg = "Camera photo captured & optimized"
                } else {
                    uploadError = "Could not compress captured camera image."
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text("JPG, PNG, WEBP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                ) {
                    GharTakImage(
                        imageUrl = imageUrl.ifBlank { null },
                        contentDescription = "Selected product image preview",
                        modifier = Modifier.fillMaxSize(),
                        isGroceryFallback = isGroceryFallback,
                        isMedicalFallback = isMedicalFallback
                    )
                    if (isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.52f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 2.5.dp)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.testTag("pick_image_button")
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Pick from Gallery", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (imageUrl.isBlank()) "Gallery" else "Replace")
                        }
                        OutlinedButton(
                            onClick = { filesLauncher.launch(SUPPORTED_FILE_MIMES) },
                            modifier = Modifier.testTag("pick_file_image_button")
                        ) {
                            Icon(Icons.Filled.FolderOpen, contentDescription = "Browse Computer or Device Files", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Files")
                        }
                        OutlinedButton(
                            onClick = {
                                runCatching { cameraLauncher.launch(null) }
                                    .onFailure { uploadError = "Camera unavailable on this device. Use Gallery or Files." }
                            },
                            modifier = Modifier.testTag("capture_camera_image_button")
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Capture Camera Photo", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Camera")
                        }
                        if (imageUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    onImageChanged("")
                                    uploadSuccessMsg = null
                                    uploadError = null
                                },
                                modifier = Modifier.testTag("delete_primary_image_button")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete image", tint = GharTakCoralError, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delete", color = GharTakCoralError)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = if (imageUrl.startsWith("data:image/")) "[Uploaded Image Ready]" else imageUrl,
                        onValueChange = {
                            if (!it.startsWith("[Uploaded")) {
                                uploadError = null
                                onImageChanged(it)
                            }
                        },
                        label = { Text("Or paste HTTPS / res:// image URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (isProcessing) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                    )
                    Text(
                        "Validating & optimizing image... ${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uploadError != null) {
                Text(uploadError!!, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
            } else if (uploadSuccessMsg != null && imageUrl.isNotBlank()) {
                Text(uploadSuccessMsg!!, color = GharTakEmerald, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdditionalImagesUploadSection(
    images: List<String>,
    onImagesChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    maxImages: Int = 4,
    isGroceryFallback: Boolean = false,
    isMedicalFallback: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun processMultipleUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val remainingSlots = (maxImages - images.size).coerceAtLeast(0)
        if (remainingSlots == 0) {
            errorMsg = "Maximum of $maxImages additional images reached."
            return
        }
        val toProcess = uris.take(remainingSlots)
        isProcessing = true
        progress = 0.1f
        errorMsg = null
        scope.launch {
            val added = mutableListOf<String>()
            var skippedCount = 0
            toProcess.forEachIndexed { idx, uri ->
                val result = encodeUriToCompressedDataUri(context, uri, maxBytes = 60_000, maxDim = 320)
                result.onSuccess { added.add(it) }.onFailure { skippedCount++ }
                progress = ((idx + 1).toFloat() / toProcess.size.toFloat()).coerceIn(0.1f, 1f)
            }
            isProcessing = false
            if (added.isNotEmpty()) {
                onImagesChanged((images + added).take(maxImages))
            }
            if (skippedCount > 0) {
                errorMsg = "$skippedCount file(s) skipped (only JPG, JPEG, PNG, WEBP supported)."
            }
        }
    }

    val multiGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxImages.coerceAtLeast(2))
    ) { uris -> processMultipleUris(uris) }

    val multiFilesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris -> processMultipleUris(uris) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Additional Gallery Images (${images.size}/$maxImages)", style = MaterialTheme.typography.labelLarge)
                if (images.isNotEmpty()) {
                    TextButton(onClick = { onImagesChanged(emptyList()) }) {
                        Text("Clear All", color = GharTakCoralError)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    enabled = images.size < maxImages && !isProcessing,
                    onClick = {
                        multiGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.testTag("add_more_gallery_images_button")
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add from Gallery")
                }
                OutlinedButton(
                    enabled = images.size < maxImages && !isProcessing,
                    onClick = { multiFilesLauncher.launch(SUPPORTED_FILE_MIMES) },
                    modifier = Modifier.testTag("add_more_file_images_button")
                ) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add from Files")
                }
            }

            if (isProcessing) {
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text("Uploading additional images... ${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }

            if (images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(images) { index, img ->
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            GharTakImage(
                                imageUrl = img,
                                contentDescription = "Additional image ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                isGroceryFallback = isGroceryFallback,
                                isMedicalFallback = isMedicalFallback
                            )
                            IconButton(
                                onClick = {
                                    onImagesChanged(images.filterIndexed { i, _ -> i != index })
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(22.dp)
                                    .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            if (errorMsg != null) {
                Text(errorMsg!!, color = GharTakCoralError, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun isSupportedImageHeader(bytes: ByteArray): Boolean {
    if (bytes.size < 12) return false
    // JPEG: FF D8 FF
    val isJpeg = (bytes[0].toInt() and 0xFF) == 0xFF &&
        (bytes[1].toInt() and 0xFF) == 0xD8 &&
        (bytes[2].toInt() and 0xFF) == 0xFF
    // PNG: 89 50 4E 47
    val isPng = (bytes[0].toInt() and 0xFF) == 0x89 &&
        bytes[1] == 0x50.toByte() &&
        bytes[2] == 0x4E.toByte() &&
        bytes[3] == 0x47.toByte()
    // WEBP: RIFF....WEBP
    val isWebp = bytes[0] == 'R'.code.toByte() &&
        bytes[1] == 'I'.code.toByte() &&
        bytes[2] == 'F'.code.toByte() &&
        bytes[3] == 'F'.code.toByte() &&
        bytes[8] == 'W'.code.toByte() &&
        bytes[9] == 'E'.code.toByte() &&
        bytes[10] == 'B'.code.toByte() &&
        bytes[11] == 'P'.code.toByte()
    return isJpeg || isPng || isWebp
}

internal suspend fun encodeUriToCompressedDataUri(
    context: Context,
    uri: Uri,
    maxBytes: Int = 105_000,
    maxDim: Int = 420
): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val mime = context.contentResolver.getType(uri)?.lowercase()
        if (mime != null && mime !in SUPPORTED_IMAGE_MIME_TYPES) {
            error("Unsupported image format ($mime). Only JPG, JPEG, PNG, and WEBP are allowed.")
        }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to read selected image file.")
        if (bytes.size > 12_000_000) {
            error("Image file is larger than 12 MB. Please select a smaller image.")
        }
        if (!isSupportedImageHeader(bytes)) {
            error("Invalid image signature. Please select a valid JPG, JPEG, PNG, or WEBP file.")
        }
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Could not decode image file.")
        encodeBitmapToCompressedDataUri(original, maxBytes, maxDim)
            ?: error("Could not optimize image.")
    }
}

internal suspend fun encodeBitmapToCompressedDataUri(
    original: Bitmap,
    maxBytes: Int = 105_000,
    maxDim: Int = 420
): String? = withContext(Dispatchers.IO) {
    runCatching {
        val scale = minOf(maxDim.toFloat() / original.width, maxDim.toFloat() / original.height, 1f)
        val targetW = (original.width * scale).toInt().coerceAtLeast(1)
        val targetH = (original.height * scale).toInt().coerceAtLeast(1)
        val resized = if (targetW == original.width && targetH == original.height) {
            original
        } else {
            Bitmap.createScaledBitmap(original, targetW, targetH, true)
        }
        val out = ByteArrayOutputStream()
        var quality = 76
        resized.compress(Bitmap.CompressFormat.JPEG, quality, out)
        while (out.size() > maxBytes && quality > 26) {
            out.reset()
            quality -= 12
            resized.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$b64"
    }.getOrNull()
}

@Composable
fun DiscountBadge(discountPercent: Int, modifier: Modifier = Modifier) {
    if (discountPercent > 0) {
        Surface(
            color = GharTakPink,
            shape = RoundedCornerShape(999.dp),
            modifier = modifier
        ) {
            Text(
                text = "$discountPercent% OFF",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status.lowercase()) {
        "approved",
        "delivered",
        "paid",
        "active",
        "accepted",
        "preparing",
        "ready for pickup",
        "rider assigned",
        "picked up",
        "on the way",
        "completed" -> GharTakEmerald.copy(alpha = 0.14f) to GharTakEmerald
        "pending", "awaiting_verification" -> GharTakAmber.copy(alpha = 0.16f) to GharTakAmber
        else -> GharTakCoralError.copy(alpha = 0.14f) to GharTakCoralError
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OperatingHoursBadge(
    isOpen: Boolean,
    openingTime: String,
    closingTime: String,
    modifier: Modifier = Modifier
) {
    val openNow = GharTakConstants.isWithinOperatingHours(isOpen, openingTime, closingTime)
    val hoursLabel = GharTakConstants.formatOperatingHours(openingTime, closingTime)
    val statusText = when {
        !isOpen -> "Closed • Timing: $hoursLabel"
        openNow -> "Open Now • Timing: $hoursLabel"
        else -> "Closed Now • Timing: $hoursLabel"
    }
    val (bg, fg) = if (openNow) {
        GharTakEmerald.copy(alpha = 0.14f) to GharTakEmerald
    } else {
        GharTakCoralError.copy(alpha = 0.14f) to GharTakCoralError
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = statusText,
                color = fg,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodDayBadges(
    availableDays: List<String>,
    isDailySpecial: Boolean,
    modifier: Modifier = Modifier,
    showAllDaysLabel: Boolean = true
) {
    if (!isDailySpecial && availableDays.isEmpty() && !showAllDaysLabel) return
    val daysText = if (availableDays.isNotEmpty()) {
        "Days: ${availableDays.joinToString(", ")}"
    } else {
        "Days: All Days (Daily)"
    }
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isDailySpecial) {
            Surface(
                color = GharTakAmber.copy(alpha = 0.18f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = GharTakAmber,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        "Today's Special",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GharTakAmber
                    )
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(999.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    daysText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperatingHoursEditorDialog(
    vendorName: String,
    initialIsOpen: Boolean,
    initialOpeningTime: String,
    initialClosingTime: String,
    onDismiss: () -> Unit,
    onSave: (isOpen: Boolean, openingTime: String, closingTime: String) -> Unit
) {
    var isOpen by remember { mutableStateOf(initialIsOpen) }
    var openingTime by remember { mutableStateOf(initialOpeningTime.ifBlank { "00:00" }) }
    var closingTime by remember { mutableStateOf(initialClosingTime.ifBlank { "23:59" }) }
    val validOpen = GharTakConstants.parseMinutesOfDay(openingTime) != null
    val validClose = GharTakConstants.parseMinutesOfDay(closingTime) != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Store Hours • $vendorName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accepting Orders", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (isOpen) "Open according to schedule" else "Manually closed for customers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isOpen,
                        onCheckedChange = { isOpen = it },
                        modifier = Modifier.testTag("vendor_hours_open_switch")
                    )
                }
                Text("Quick Timings", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("24 Hours", "00:00", "23:59"),
                        Triple("9 AM – 11 PM", "09:00", "23:00"),
                        Triple("12 PM – 2 AM", "12:00", "02:00"),
                        Triple("6 PM – 2 AM", "18:00", "02:00")
                    ).forEach { (label, open, close) ->
                        FilterChip(
                            selected = openingTime == open && closingTime == close,
                            onClick = {
                                openingTime = open
                                closingTime = close
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GharTakEmerald,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("preset_hours_${open}_${close}")
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = openingTime,
                        onValueChange = { openingTime = it.take(5) },
                        label = { Text("Open (HH:mm)") },
                        placeholder = { Text("09:00") },
                        isError = !validOpen,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("opening_time_input")
                    )
                    OutlinedTextField(
                        value = closingTime,
                        onValueChange = { closingTime = it.take(5) },
                        label = { Text("Close (HH:mm)") },
                        placeholder = { Text("23:00") },
                        isError = !validClose,
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("closing_time_input")
                    )
                }
                if (validOpen && validClose) {
                    Text(
                        "Preview: ${GharTakConstants.formatOperatingHours(openingTime, closingTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        "Use 24-hour HH:mm format (e.g. 09:00 or 23:30)",
                        style = MaterialTheme.typography.bodySmall,
                        color = GharTakCoralError
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(isOpen, openingTime.trim(), closingTime.trim()) },
                enabled = validOpen && validClose,
                modifier = Modifier.testTag("save_vendor_hours_button")
            ) {
                Text("Save Hours")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PriceRow(
    regularPricePkr: Double,
    discountPricePkr: Double,
    unitSuffix: String = ""
) {
    val hasDiscount = discountPricePkr > 0.0 && discountPricePkr < regularPricePkr
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = GharTakConstants.formatPkr(if (hasDiscount) discountPricePkr else regularPricePkr) +
                if (unitSuffix.isNotBlank()) " / $unitSuffix" else "",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (hasDiscount) {
            Text(
                text = GharTakConstants.formatPkr(regularPricePkr),
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_cart),
            contentDescription = title,
            modifier = Modifier
                .size(136.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(4.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlightColor: Color = GharTakPink
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = highlightColor)
        }
    }
}

@Composable
fun CityPickerDialog(
    cities: List<String>,
    selectedCity: String,
    includeAllCitiesOption: Boolean = false,
    pendingCityRequests: List<CityApprovalRequest> = emptyList(),
    onRequestNewCity: ((String) -> Unit)? = null,
    onSelectCity: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var requestCityInput by remember { mutableStateOf("") }

    val approvedCities = remember(cities) {
        cities.ifEmpty { GharTakConstants.INITIAL_PAKISTAN_CITIES }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals(GharTakConstants.ALL_CITIES, ignoreCase = true) }
            .distinctBy { it.lowercase() }
    }
    val filteredCities = remember(approvedCities, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) approvedCities
        else approvedCities.filter { it.contains(q, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Select Approved Location")
                Text(
                    if (includeAllCitiesOption) "All Cities or ${approvedCities.size} Admin-approved cities"
                    else "${approvedCities.size} Admin-approved cities",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search approved city...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("city_picker_search_input")
                )
                LazyColumn(modifier = Modifier.height(250.dp)) {
                    if (includeAllCitiesOption &&
                        (searchQuery.isBlank() || GharTakConstants.ALL_CITIES.contains(searchQuery.trim(), ignoreCase = true))
                    ) {
                        item(key = "all_cities") {
                            val isSelected = selectedCity.equals(GharTakConstants.ALL_CITIES, ignoreCase = true)
                            Surface(
                                onClick = {
                                    onSelectCity(GharTakConstants.ALL_CITIES)
                                    onDismiss()
                                },
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("city_option_all_cities")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                GharTakConstants.ALL_CITIES,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            Text(
                                                "Show restaurants, marts & pharmacies across all approved cities",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp))
                        }
                    }

                    items(filteredCities, key = { it }) { city ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectCity(city)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Text(city, style = MaterialTheme.typography.bodyLarge)
                            }
                            if (city.equals(selectedCity, ignoreCase = true)) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                if (onRequestNewCity != null) {
                    HorizontalDivider()
                    Text(
                        "City not listed? Request Admin Approval:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = requestCityInput,
                            onValueChange = { requestCityInput = it },
                            placeholder = { Text("Enter your city name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("city_request_input")
                        )
                        Button(
                            enabled = requestCityInput.trim().length >= 2,
                            onClick = {
                                val candidate = requestCityInput.trim()
                                onRequestNewCity(candidate)
                                requestCityInput = ""
                            },
                            modifier = Modifier.testTag("city_request_submit_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Request city", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Request")
                        }
                    }
                    val recentPending = pendingCityRequests.take(3)
                    if (recentPending.isNotEmpty()) {
                        recentPending.forEach { req ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Request: ${req.cityName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusBadge(req.status)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

fun openGoogleMapsLocation(context: Context, lat: Double, lng: Double, label: String) {
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    runCatching { context.startActivity(intent) }.onFailure {
        val fallback = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://maps.google.com/?q=$lat,$lng")
        )
        runCatching { context.startActivity(fallback) }
    }
}

fun openSupportDialer(
    context: Context,
    phoneNumber: String = GharTakConstants.SUPPORT_PHONE_LOCAL
) {
    val cleanPhone = phoneNumber.trim().ifBlank { GharTakConstants.SUPPORT_PHONE_LOCAL }
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
    runCatching { context.startActivity(intent) }
}

fun openWhatsAppSupport(
    context: Context,
    phoneDigits: String = GharTakConstants.SUPPORT_WHATSAPP_NUMBER,
    prefillMessage: String = "Assalam-o-Alaikum GT Service Support, I need assistance."
) {
    val cleanWa = GharTakConstants.toWhatsappDigits(phoneDigits)
    val encoded = Uri.encode(prefillMessage)
    val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$cleanWa?text=$encoded"))
    runCatching { context.startActivity(waIntent) }
}
