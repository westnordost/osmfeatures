
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {

        commonMain.dependencies {
            //noinspection UseTomlInstead
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            implementation(libs.fluid.locale)
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.normalize)
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.junit)
            },
            resources.sr
        }
    }
}


android {
    namespace = "de.westnordost.osmfeatures"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
}

//plugins {
//    id("java-library")
//    id("maven-publish")
//    id("signing")
//    id("com.android.library")
//    kotlin("multiplatform") version "1.9.10"
//    kotlin("plugin.serialization") version "1.9.21"
//}
//
//repositories {
//    mavenCentral()
//}
//
//dependencies {
//
//}
//
//kotlin {
//
//    targetHierarchy.default()
//    jvm()
//    androidTarget {
//        publishLibraryVariants("release")
//        compilations.all {
//            kotlinOptions {
//                jvmTarget = "1.8"
//            }
//        }
//    }
//    iosX64()
//    iosArm64()
//    iosSimulatorArm64()
//}
//    sourceSets {
//        val commonMain by getting {
//            dependencies {
//                //put your multiplatform dependencies here
//            }
//        }
//        val commonTest by getting {
//            dependencies {
//
//            }
//        }
//    }
//
//publishing {
//    repositories {
//        maven {
//            url = uri("https://github.com/westnordost/osmfeatures")
//        }
//    }
//    publications {
//        create<MavenPublication>("mavenJava") {
//            groupId = "de.westnordost"
//            artifactId = "osmfeatures"
//            version = "5.2"
//            from(components["java"])
//
//        pom {
//            name.value("osmfeatures")
//            description.value("Java library to translate OSM tags to and from localized names.")
//            url.value("https://github.com/westnordost/osmfeatures")
//            scm {
//                connection.value("https://github.com/westnordost/osmfeatures.git")
//                developerConnection.value("https://github.com/westnordost/osmfeatures.git")
//                url.value("https://github.com/westnordost/osmfeatures")
//            }
//            licenses {
//                license {
//                    name.value("The Apache License, Version 2.0")
//                    url.value("http://www.apache.org/licenses/LICENSE-2.0.txt")
//                }
//            }
//            developers {
//                developer {
//                    id.value("westnordost")
//                    name.value("Tobias Zwick")
//                    email.value("osm@westnordost.de")
//                }
//            }
//        }
//        }
//    }
//}
//
//signing {
//    sign(publishing.publications["mavenJava"])
//}
//
//java {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//    targetCompatibility = JavaVersion.VERSION_1_8
//}