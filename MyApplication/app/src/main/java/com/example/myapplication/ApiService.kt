package com.example.myapplication

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Data model for Registration
data class User(
    @SerializedName("first_name") val firstName: String?,
    @SerializedName("last_name") val lastName: String?,
    @SerializedName("mobile_number") val mobileNumber: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

// Data model for Customer
data class CustomerResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("mobile_number") val mobileNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("length") val length: String?
)

// Data model for Login
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("mobile_number") val mobileNumber: String?,
    @SerializedName("full_name") val fullName: String?
)

// Data model for Order Status response
data class OrderStatusResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("estimated_completion_date") val estimatedCompletionDate: String?
)

data class DashboardStatsResponse(
    @SerializedName("total_customers") val totalCustomers: Int?,
    @SerializedName("total_orders") val totalOrders: Int?,
    @SerializedName("active_orders") val activeOrders: Int?,
    @SerializedName("pending_orders") val pendingOrders: Int?,
    @SerializedName("completed_orders") val completedOrders: Int?
)

data class RecentOrderResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("mobile_number") val mobileNumber: String?,
    @SerializedName("garment_type") val garmentType: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("order_date") val orderDate: String?
)

data class MeasurementResponse(
    @SerializedName("id") val id: Int?,
    @SerializedName("garment_type") val garmentType: String?,
    @SerializedName("length") val length: String?,
    @SerializedName("chest") val chest: String?,
    @SerializedName("waist") val waist: String?,
    @SerializedName("collar") val collar: String?,
    @SerializedName("shoulder") val shoulder: String?,
    @SerializedName("sleeve") val sleeve: String?,
    @SerializedName("hip") val hip: String?,
    @SerializedName("rise") val rise: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("count") val count: Int?
)

data class AppUpdateResponse(
    @SerializedName("latest_version_code") val latestVersionCode: Int?,
    @SerializedName("latest_version_name") val latestVersionName: String?,
    @SerializedName("download_url") val downloadUrl: String?,
    @SerializedName("force_update") val forceUpdate: Boolean?,
    @SerializedName("update_message") val updateMessage: String?
)

data class AppVersionResponse(
    @SerializedName("latest_version") val latestVersion: String?,
    @SerializedName("force_update") val forceUpdate: Boolean?,
    @SerializedName("apk_url") val apkUrl: String?
)

interface ApiService {
    @POST("api/register/")
    fun registerUser(@Body user: User): Call<Void>

    @POST("api/login/")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/add_customer/")
    fun addCustomer(@Body customerData: Map<String, String>): Call<Void>

    @GET("api/get_customers/")
    fun getAllCustomers(): Call<List<CustomerResponse>>

    @GET("api/customer/{mobile}/")
    fun getCustomerDetails(@Path("mobile") mobile: String): Call<CustomerResponse>

    @DELETE("api/delete_customer/{mobile}/")
    fun deleteCustomer(@Path("mobile") mobile: String): Call<Void>

    @POST("api/add_measurement/")
    fun addMeasurement(@Body measurementData: Map<String, String>): Call<Void>

    @GET("api/orders/{orderId}/")
    fun getOrderStatus(@Path("orderId") orderId: String): Call<OrderStatusResponse>

    @GET("api/dashboard/stats/")
    fun getDashboardStats(): Call<DashboardStatsResponse>

    @GET("api/orders/recent/")
    fun getRecentOrders(
        @retrofit2.http.Query("limit") limit: Int,
        @retrofit2.http.Query("status") status: String? = null
    ): Call<List<RecentOrderResponse>>

    @GET("api/measurements/{mobile}/")
    fun getCustomerMeasurements(
        @Path("mobile") mobile: String,
        @retrofit2.http.Query("garment_type") garmentType: String
    ): Call<MeasurementResponse>

    @POST("api/update_order_status/")
    fun updateOrderStatus(@Body statusData: Map<String, String>): Call<Void>

    @GET("api/check_update/")
    fun checkAppUpdate(): Call<AppUpdateResponse>

    @GET("app-version/")
    fun getAppVersion(): Call<AppVersionResponse>
}