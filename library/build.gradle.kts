plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    jvm()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosArm64()

    sourceSets {

        commonMain.dependencies {
            //noinspection UseTomlInstead
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.normalize)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.test)
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