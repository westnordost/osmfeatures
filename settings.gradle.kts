pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()

    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "de.westnordost.osmfeatures"
include(":library")
