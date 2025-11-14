import com.android.build.api.dsl.androidLibrary

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.21"
    id("com.android.kotlin.multiplatform.library") version "8.12.3"
    id("com.vanniktech.maven.publish") version "0.35.0"
    id("org.jetbrains.dokka") version "2.1.0"
}

group = "de.westnordost"
version = "7.0"

kotlin {
    jvm()

    androidLibrary {
        namespace = "de.westnordost.osmfeatures"
        compileSdk = 36
        minSdk = 21
    }

    iosSimulatorArm64()
    iosArm64()
    iosX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                // multiplatform file access
                api("org.jetbrains.kotlinx:kotlinx-io-core:0.8.0")
                // parsing the preset.json
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-io:1.9.0")
                // for stripping diacritics correctly
                implementation("com.doist.x:normalize:1.2.0")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                // we are pulling some current preset json from the iD preset repo to see if parsing
                // does at least not crash and return something
                implementation("io.ktor:ktor-client-core:3.3.2")
                implementation("io.ktor:ktor-client-cio:3.3.2")
                // ktor-client is a suspending API, so we need coroutines too
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
    }
}

dokka {
    moduleName.set("osmfeatures")
    dokkaSourceSets {
        configureEach {
            sourceLink {
                remoteUrl("https://github.com/westnordost/osmfeatures/tree/v${project.version}/")
                localDirectory = rootDir
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), rootProject.name, version.toString())
    
    pom {
        name = "osmfeatures"
        description = "Kotlin multiplatform library to translate OSM tags to and from localized names."
        url = "https://github.com/westnordost/osmfeatures"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://raw.githubusercontent.com/westnordost/osmfeatures/master/LICENSE"
            }
        }
        issueManagement {
            system = "GitHub"
            url = "https://github.com/westnordost/osmfeatures/issues"
        }
        scm {
            connection = "https://github.com/westnordost/osmfeatures.git"
            url = "https://github.com/westnordost/osmfeatures"
            developerConnection = connection
        }
        developers {
            developer {
                id = "westnordost"
                name = "Tobias Zwick"
                email = "osm@westnordost.de"
            }
        }
    }
}
