package com.example.thuchanhapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.google.gson.annotations.SerializedName

// 1. DATA MODEL (Thêm dấu ? để an toàn với dữ liệu null)
data class ProductData(
    val id: String?,
    val name: String?,
    val price: String?,
    
    @SerializedName("imgURL") 
    val imageUrl: String?, 
    
    @SerializedName("des") 
    val description: String?
)

// 2. RETROFIT API INTERFACE
interface ApiService {
    @GET("m1/890655-872447-default/v2/product")
    suspend fun getProductDetail(): ProductData
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://mock.apidog.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)

// 3. MAIN ACTIVITY & UI
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProductDetailScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(modifier: Modifier = Modifier) {
    var product by remember { mutableStateOf<ProductData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Biến trạng thái để kích hoạt tải lại
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Gọi API mỗi khi refreshTrigger thay đổi
    LaunchedEffect(refreshTrigger) {
        isLoading = true // Bật vòng xoay loading khi bắt đầu tải lại
        errorMessage = null
        try {
            val response = apiService.getProductDetail()
            product = response
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message ?: "Lỗi kết nối mạng"
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Bar: Sắp xếp lại để chứa cả nút Back (trái) và Refresh (phải)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween, // Đẩy 2 cụm ra 2 góc
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* Xử lý nút back */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00BCD4)
                    )
                }
                Text(
                    text = "Product detail",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Nút Refresh
            IconButton(onClick = { refreshTrigger++ }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF00BCD4)
                )
            }
        }

        // Body
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00BCD4))
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lỗi: $errorMessage", color = Color.Red)
            }
        } else if (product != null) {
            AsyncImage(
                model = product!!.imageUrl ?: "",
                contentDescription = product!!.name ?: "Ảnh sản phẩm",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product!!.name ?: "Sản phẩm không có tên",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Giá: ${product!!.price ?: "Đang cập nhật"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = product!!.description ?: "Chưa có mô tả cho sản phẩm này.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
        }
    }
}