buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo1.maven.org/maven2") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.22")
    }
}
