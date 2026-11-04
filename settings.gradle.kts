pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("https://jitpack.io")
        }
    }
    versionCatalogs {
        create("libsLegacy") {
            from(files("gradle/libsLegacy.versions.toml"))
        }
    }
}

rootProject.name = "S Music Player"

include(":app")
include(":app:lite")
include(":data")
include(":data:sync")
include(":domain")
include(":domain:sync")
include(":shared:app")
include(":shared:app:lite")
include(":shared:app:sync")
include(":shared:data")
include(":shared:domain")

include(":libs:fsync:core")
include(":libs:fsync:impl")
