plugins {
    id("com.android.application")
}

android {
    namespace = "com.momos.mouseandroid"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.momos.mouseandroid"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation(libs.androidx.activity.ktx)
}
