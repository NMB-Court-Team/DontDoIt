plugins {
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.5.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "net.astrorbits"
version = "1.0.8"


repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.1.2")
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("kotlin", "net.astrorbits.dontdoit.shadow.kotlin")
    relocate("org.intellij.lang.annotations", "net.astrorbits.dontdoit.shadow.org.intellij.lang.annotations")
    relocate("org.jetbrains.annotations", "net.astrorbits.dontdoit.shadow.org.jetbrains.annotations")
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
