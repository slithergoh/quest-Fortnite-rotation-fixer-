plugins { id("com.android.application") }

android {
    namespace = "online.ripperheaven.rotation"
    compileSdk = 35

    defaultConfig {
        applicationId = "online.ripperheaven.rotation"
        minSdk = 29
        targetSdk = 32
        versionCode = 3
        versionName = "0.3.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
