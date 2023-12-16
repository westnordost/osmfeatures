plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.21"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20230227")
    testImplementation("junit:junit:4.13.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.fluidsonic.locale:fluid-locale:0.13.0")
    implementation("com.squareup.okio:okio:3.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn.add(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
    }
}



publishing {
    repositories {
        maven {
            url = uri("https://github.com/westnordost/osmfeatures")
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "de.westnordost"
            artifactId = "osmfeatures"
            version = "5.2"
            from(components["java"])

        pom {
            name.value("osmfeatures")
            description.value("Java library to translate OSM tags to and from localized names.")
            url.value("https://github.com/westnordost/osmfeatures")
            scm {
                connection.value("https://github.com/westnordost/osmfeatures.git")
                developerConnection.value("https://github.com/westnordost/osmfeatures.git")
                url.value("https://github.com/westnordost/osmfeatures")
            }
            licenses {
                license {
                    name.value("The Apache License, Version 2.0")
                    url.value("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.value("westnordost")
                    name.value("Tobias Zwick")
                    email.value("osm@westnordost.de")
                }
            }
        }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
//compileKotlin {
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//}
//compileTestKotlin {
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
//}