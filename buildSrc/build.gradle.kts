plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Can't use the library catalog here
    //noinspection UseTomlInstead
    implementation("com.github.jk1:gradle-license-report:3.1.1")
}