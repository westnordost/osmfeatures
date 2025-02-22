plugins {
    kotlin("multiplatform") version "2.1.0"
    id("com.android.library") version "8.5.2"
    id("org.jetbrains.dokka") version "1.9.20"

    id("maven-publish")
    id("signing")
}

kotlin {
    group = "de.westnordost"
    version = "6.3"

    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    iosSimulatorArm64()
    iosX64()
    iosArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                // multiplatform file access
                api("org.jetbrains.kotlinx:kotlinx-io-core:0.6.0")
                // parsing the preset.json
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-io:1.8.0")
                // for stripping diacritics correctly
                implementation("com.doist.x:normalize:1.0.5")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                // we are pulling some current preset json from the iD preset repo to see if parsing
                // does at least not crash and return something
                implementation("io.ktor:ktor-client-core:3.1.0")
                implementation("io.ktor:ktor-client-cio:3.1.0")
                // ktor-client is a suspending API, so we need coroutines too
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
    }
}


android {
    namespace = "de.westnordost.osmfeatures"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }
}

val javadocJar = tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml)
}

publishing {
    publications {
        withType<MavenPublication> {
            artifactId = rootProject.name + if (name != "kotlinMultiplatform") "-$name" else ""
            artifact(javadocJar)

            pom {
                name.set("osmfeatures")
                description.set("Java library to translate OSM tags to and from localized names.")
                url.set("https://github.com/westnordost/osmfeatures")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://raw.githubusercontent.com/westnordost/osmfeatures/master/LICENSE")
                    }
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/westnordost/osmfeatures/issues")
                }
                scm {
                    connection.set("https://github.com/westnordost/osmfeatures.git")
                    url.set("https://github.com/westnordost/osmfeatures")
                }
                developers {
                    developer {
                        id.set("westnordost")
                        name.set("Tobias Zwick")
                        email.set("osm@westnordost.de")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "oss"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                val ossrhUsername: String by project
                val ossrhPassword: String by project
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications)
}


// FIXME - workaround for https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>().configureEach {
    mustRunAfter(signingTasks)
}