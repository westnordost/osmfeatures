plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.android.library)
}

kotlin {

    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {

        commonMain.dependencies {
            //noinspection UseTomlInstead
            implementation(libs.okio)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.normalize)
        }

        commonTest.dependencies {
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

signing {
    sign(publishing.publications.mavenJava)
}

//plugins {
//    id("java-library")
//    id("maven-publish")
//    id("signing")
//    id("com.android.library")
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


//
//java {
//    sourceCompatibility = JavaVersion.VERSION_1_8
//    targetCompatibility = JavaVersion.VERSION_1_8
//}