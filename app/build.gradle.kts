import java.io.PrintWriter

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


abstract class SDPFactory : DefaultTask() {
    companion object {
        private const val MIN_DPI = 300
        private const val MAX_DPI = 1080
        private const val DPI_STEP = 30
    }

    @Input
    var positiveMax = 600

    @Input
    var negativeMax = 60

    @Input
    var defaultDpi = 390

    @TaskAction
    fun create() {
        val resFolder = File(project.projectDir, "src/main/res")
        for (dpi in MIN_DPI..MAX_DPI step DPI_STEP) {
            val folder = File(resFolder, "values-sw${dpi}dp")
            if (!folder.exists()) {
                folder.mkdir()
            }

            createFile(folder, "positive_sdps.xml", dpi, isNegative = false)
            createFile(folder, "negative_sdps.xml", dpi, isNegative = true)
            createFile(folder, "positive_ssps.xml", dpi, isNegative = false, unit = "sp")
            createFile(folder, "negative_ssps.xml", dpi, isNegative = true, unit = "sp")
        }
    }

    private fun createFile(
        folder: File,
        fileName: String,
        dpi: Int,
        isNegative: Boolean,
        unit: String = "dp") {
        val file = File(folder, fileName)
        if (!file.exists()) {
            file.createNewFile()
        }

        PrintWriter(file).use { printerWriter ->
            printerWriter.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            printerWriter.println("<resources>")
            val max = if (isNegative) negativeMax else positiveMax
            for (i in 1..max) {
                val dimensionValue = String.format("%.2f", i * dpi / defaultDpi.toFloat())
                val dimenName = if (isNegative) "_minus${i}s$unit" else "_${i}sdp"
                printerWriter.println("<dimen name=\"$dimenName\">${if (isNegative) "-$dimensionValue" else dimensionValue}$unit</dimen>")
            }
            printerWriter.println("</resources>")
        }
    }
}

tasks.register<SDPFactory>("createSDP")
