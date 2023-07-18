@file:Suppress(
    "DSL_SCOPE_VIOLATION",
    "MISSING_DEPENDENCY_CLASS",
    "FUNCTION_CALL_EXPECTED",
    "PropertyName",
    "UnstableApiUsage",
)

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)

    alias(libs.plugins.fabric.loom)
}

val archives_name: String by rootProject
val mod_name: String by rootProject
val loom: LoomGradleExtensionAPI by extensions

version = rootProject.version

base {
    archivesName = "$archives_name-fabric"
}

loom {
    mods {
        register(archives_name) {
            modFiles.from("../common/build/devlibs/mod-sets-common-$version-dev.jar")
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
    "minecraft"(libs.minecraft)
    "mappings"(
        loom.layered {
            officialMojangMappings()
            parchment(
                variantOf(libs.parchment) {
                    artifactType("zip")
                },
            )
        },
    )

    include(project(path = ":common", configuration = "namedElements"))
    implementation(project(path = ":common", configuration = "namedElements")) {
        exclude(module = "fabric-loader")
    }

    modImplementation(libs.fabric.loader)
    modRuntimeOnly(libs.fabric.languageKotlin) {
        exclude(module = "fabric-loader")
    }

    modRuntimeOnly(libs.yacl.fabric)
    modRuntimeOnly(libs.modmenu) {
        exclude(module = "fabric-loader")
    }
    modRuntimeOnly(libs.kinecraft.serialization)

    include(libs.kotlin.stdlib.jdk8)
    include(libs.kotlinx.serialization.core)
    include(libs.kotlinx.serialization.json)
    include(libs.kotlin.reflect)
    include(libs.kinecraft.serialization)
}

tasks {
    processResources {
        inputs.property("id", archives_name)
        inputs.property("version", rootProject.version)
        inputs.property("group", rootProject.group)
        inputs.property("name", rootProject.property("mod_name").toString())
        inputs.property("description", rootProject.property("mod_description").toString())
        inputs.property("author", rootProject.property("mod_author").toString())
        inputs.property("source", rootProject.property("mod_source").toString())
        inputs.property("minecraft_version", libs.versions.min.minecraft.get())
        inputs.property("fabric_loader_version", libs.versions.fabric.loader.get())
        inputs.property("fabric_language_kotlin_version", libs.versions.fabric.language.kotlin.get())
        inputs.property("yacl_version", libs.versions.min.yacl.get())
        inputs.property("kinecraft_serialization_version", libs.versions.kinecraft.serialization.get())
        inputs.property("mod_menu_version", libs.versions.min.modmenu.get())

        filesMatching("fabric.mod.json") {
            expand(
                "id" to archives_name,
                "version" to rootProject.version,
                "group" to rootProject.group,
                "name" to mod_name,
                "description" to rootProject.property("mod_description").toString(),
                "author" to rootProject.property("mod_author").toString(),
                "source" to rootProject.property("mod_source").toString(),
                "minecraft_version" to libs.versions.min.minecraft.get(),
                "fabric_loader_version" to libs.versions.fabric.loader.get(),
                "fabric_language_kotlin_version" to libs.versions.fabric.language.kotlin.get(),
                "yacl_version" to libs.versions.min.yacl.get(),
                "kinecraft_serialization_version" to libs.versions.kinecraft.serialization.get(),
                "mod_menu_version" to libs.versions.min.modmenu.get(),
            )
        }
    }

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
}
