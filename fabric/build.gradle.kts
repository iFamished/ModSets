@file:Suppress(
    "DSL_SCOPE_VIOLATION",
    "MISSING_DEPENDENCY_CLASS",
    "FUNCTION_CALL_EXPECTED",
    "PropertyName",
    "UnstableApiUsage",
)

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val archives_name: String by rootProject
val mod_name: String by rootProject

version = rootProject.version

base {
    archivesName.set("$archives_name-fabric")
}

loom {
    mods {
        register(archives_name) {
            modFiles.from("../common/build/devlibs/${project(":common").base.archivesName.get()}-$version-dev.jar")
            sourceSet(sourceSets.main.get())
            dependency(
                libs.kotlin.stdlib.jdk8.get(),
                libs.kotlin.reflect.get(),
                libs.kotlinx.serialization.core.get(),
                libs.kotlinx.serialization.json.get(),
            )
        }
    }
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName}" }
        }
    }

    processResources {
        from(project(":common").sourceSets.main.get().resources)
    }
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
}

dependencies {
    implementation(project(path = ":common", configuration = "namedElements")) {
        exclude(module = "fabric-loader")
    }
    include(project(path = ":common", configuration = "namedElements"))

    modImplementation(libs.fabric.loader)
    modRuntimeOnly(libs.fabric.languageKotlin) {
        exclude(module = "fabric-loader")
    }

    modRuntimeOnly(libs.yacl.fabric)
    modRuntimeOnly(libs.modmenu) {
        exclude(module = "fabric-loader")
    }

    val kinecraft = "maven.modrinth:kinecraft-serialization:${libs.versions.kinecraft.serialization.get()}-fabric"
    modRuntimeOnly(kinecraft)
    include(kinecraft)
}
