plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(libs.ksp.api)
    implementation(libs.kotlin.poet)
    implementation(libs.kotlin.poet.ksp)

    compileOnly(libs.auto.service)
    kapt(libs.auto.service)
}