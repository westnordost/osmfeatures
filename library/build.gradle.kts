plugins {
    kotlin("multiplatform") version "1.9.24"
    id("com.android.library") version "8.2.0"
    id("org.jetbrains.dokka") version "1.9.20"

    id("maven-publish")
    id("signing")
}

kotlin {
    group = "de.westnordost"
    version = "6.0"

    jvm()
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    linuxX64()
    linuxArm64()
    mingwX64()

    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()
    iosArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("com.squareup.okio:okio:3.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("com.doist.x:normalize:1.0.5")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        jvmTest {
            dependencies {
            }
        }
    }
}


android {
    namespace = "de.westnordost.osmfeatures"
    compileSdk = 33
    defaultConfig {
        minSdk = 9
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