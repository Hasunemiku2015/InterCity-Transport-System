import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "1.7.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

tasks {
    compileKotlin { kotlinOptions.jvmTarget = "1.8" }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map{zipTree(it)})
    }
}

group = "com.hasunemiku2015"
version = "1.6"

repositories {
    mavenCentral()

    maven(uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/"))
    maven(uri("https://ci.mg-dev.eu/plugin/repository/everything/"))
    maven(uri("https://jitpack.io"))
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlin:kotlin-reflect:1.7.0")

    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.bergerkiller.bukkit:TrainCarts:1.20.1-v1")
    compileOnly("com.bergerkiller.bukkit:BKCommonLib:1.20.1-v1")
    implementation("com.github.deanveloper:KBukkit:master")

    compileOnly("com.sparkjava:spark-core:2.9.3")
    compileOnly("com.squareup.okhttp3:okhttp:4.10.0")
}

bukkit {
    name = "InterCity-Transport-System"
    version = "${project.version}"
    main = "$group.icts.ICTSPlugin"
    apiVersion = "1.16"
    depend = listOf("Train_Carts", "BKCommonLib", "Spark-Spigot-API")

    permissions {
        register("icts.buildsign") {
            description = "Allows a player to construct icts signs."
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }

    libraries = listOf(
        "org.jetbrains.kotlin:kotlin-stdlib:1.7.0",
        "org.jetbrains.kotlin:kotlin-reflect:1.7.0",
        "com.squareup.okhttp3:okhttp:4.10.0"
    )
}