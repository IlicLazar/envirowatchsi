plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvmToolchain(17)

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation("com.google.code.gson:gson:2.8.8")
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}