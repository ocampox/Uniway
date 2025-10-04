package com.universidad.uniway.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient - Cliente HTTP singleton para comunicación con el backend Spring Boot
 * 
 * Esta clase configura y proporciona una instancia única de Retrofit para realizar
 * llamadas HTTP al backend del foro estudiantil.
 * 
 * Configuración incluida:
 * - Logging de requests/responses para debugging
 * - Timeouts configurados para conexiones lentas
 * - Conversión automática JSON con Gson
 * - URL base configurable según el entorno (emulador/dispositivo físico)
 * 
 * URLs de configuración:
 * - Emulador Android: "http://10.0.2.2:8080/" (mapea a localhost del host)
 * - Dispositivo físico: "http://IP_LOCAL:8080/" (IP de la máquina host)
 * - Producción: "https://tu-servidor.com/api/"
 */
object ApiClient {
    
    // ==================== CONFIGURACIÓN DE URL ====================
    
    /**
     * URL base del backend Spring Boot
     * 
     * IMPORTANTE: Cambiar según tu configuración:
     * - Para emulador Android: "http://10.0.2.2:8080/"
     * - Para dispositivo físico: "http://TU_IP_LOCAL:8080/"
     * - Para producción: "https://tu-servidor.com/api/"
     */
    private const val BASE_URL = "http://10.0.2.2:8080/"
    
    // ==================== CONFIGURACIÓN DE LOGGING ====================
    
    /**
     * Interceptor para logging de requests y responses HTTP
     * Nivel BODY: muestra headers, parámetros y contenido completo
     * Útil para debugging durante desarrollo
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    // ==================== CONFIGURACIÓN DE CLIENTE HTTP ====================
    
    /**
     * Cliente OkHttp configurado con:
     * - Logging interceptor para debugging
     * - Timeouts extendidos para conexiones lentas (30 segundos)
     * - Configuración optimizada para desarrollo móvil
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)    // Timeout para establecer conexión
        .readTimeout(30, TimeUnit.SECONDS)       // Timeout para leer respuesta
        .writeTimeout(30, TimeUnit.SECONDS)      // Timeout para enviar datos
        .build()
    
    // ==================== CONFIGURACIÓN DE RETROFIT ====================
    
    /**
     * Instancia de Retrofit configurada con:
     * - URL base del backend
     * - Cliente OkHttp personalizado
     * - Convertidor Gson para serialización JSON automática
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /** Instancia del servicio API generada automáticamente por Retrofit */
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

// ==================== CLASES PARA MANEJO DE RESPUESTAS ====================

/**
 * Sealed class para manejar diferentes estados de respuestas de la API
 * 
 * Permite un manejo tipo-seguro de los diferentes estados que puede tener
 * una llamada a la API: éxito, error o cargando.
 * 
 * Estados disponibles:
 * - Success: Operación exitosa con datos
 * - Error: Error con mensaje descriptivo y código HTTP opcional
 * - Loading: Estado de carga (útil para mostrar indicadores de progreso)
 */
sealed class ApiResult<T> {
    /** Estado de éxito con los datos obtenidos */
    data class Success<T>(val data: T) : ApiResult<T>()
    
    /** Estado de error con mensaje descriptivo y código HTTP opcional */
    data class Error<T>(val message: String, val code: Int? = null) : ApiResult<T>()
    
    /** Estado de carga para mostrar indicadores de progreso */
    data class Loading<T>(val isLoading: Boolean = true) : ApiResult<T>()
}

// ==================== FUNCIÓN DE UTILIDAD PARA LLAMADAS SEGURAS ====================

/**
 * Función de extensión para realizar llamadas seguras a la API
 * 
 * Esta función envuelve las llamadas de Retrofit en un try-catch y convierte
 * las respuestas HTTP en objetos ApiResult tipados.
 * 
 * Manejo de errores incluido:
 * - Códigos HTTP de error (400, 401, 403, 404, 500, etc.)
 * - Excepciones de red (sin conexión, timeout, etc.)
 * - Parsing de mensajes de error del backend
 * - Logging detallado para debugging
 * 
 * @param apiCall Función suspendida que realiza la llamada HTTP
 * @return ApiResult con el resultado de la operación
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let { body ->
                ApiResult.Success(body)
            } ?: ApiResult.Error("Respuesta vacía del servidor")
        } else {
            // Intentar obtener el mensaje de error del cuerpo de la respuesta
            val errorBody = response.errorBody()?.string()
            android.util.Log.e("ApiClient", "Error ${response.code()}: $errorBody")
            
            val errorMessage = when (response.code()) {
                400 -> {
                    // Intentar extraer el mensaje específico del error
                    if (errorBody?.contains("error") == true) {
                        try {
                            val gson = com.google.gson.Gson()
                            val errorResponse = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                            "Error: ${errorResponse.error}"
                        } catch (e: Exception) {
                            "Solicitud incorrecta: $errorBody"
                        }
                    } else {
                        "Solicitud incorrecta"
                    }
                }
                401 -> "No autorizado"
                403 -> "Acceso prohibido"
                404 -> "Recurso no encontrado"
                500 -> "Error interno del servidor"
                else -> "Error desconocido: ${response.code()}"
            }
            ApiResult.Error(errorMessage, response.code())
        }
    } catch (e: Exception) {
        android.util.Log.e("ApiClient", "Exception en safeApiCall", e)
        ApiResult.Error("Error de conexión: ${e.message}")
    }
}