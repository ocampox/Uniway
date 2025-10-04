// build.gradle.kts (Uniway root)
plugins {
    // Reemplazamos el alias por la versión directa 8.3.0 o 8.8.0-alpha05
    id("com.android.application") version "8.13.0" apply false // Usa esta versión estable primero

    // Si 8.3.0 falla, usa la sugerida por el IDE:
    // id("com.android.application") version "8.8.0-alpha05" apply false

    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // Es buena práctica definir también la versión de Kotlin si es la fuente del alias
}