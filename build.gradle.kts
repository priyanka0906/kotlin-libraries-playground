import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm").apply(false)
    id("com.github.ben-manes.versions")
    java
}

/**
 * Toolchains for JVM projects
 * https://docs.gradle.org/current/userguide/toolchains.html
 *
 * Work around for Kotlin https://youtrack.jetbrains.com/issue/KT-43095
 */
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

allprojects {
    group = "playground"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap/")
        maven("https://kotlin.bintray.com/kotlinx/")
        maven("https://dl.bintray.com/kodein-framework/Kodein-DB") // TODO: Remove when Kodein DB exits beta
        maven("https://dl.bintray.com/jetbrains/markdown")
    }



    tasks.withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"

        kotlinOptions.jdkHome = javaToolchains
            .compilerFor(java.toolchain)
            .get()
            .metadata
            .installationPath
            .asFile
            .absolutePath
        kotlinOptions.jvmTarget = java.toolchain.languageVersion.get().toString()
    }
}

/**
 * How do I setup GitHub Actions for my Gradle or Android project?
 * https://dev.to/jmfayard/how-do-i-setup-github-actions-for-my-gradle-or-android-project-3eal
 */
tasks.register("runOnGitHub") {
    dependsOn(":kotlin-jvm:run")
    group = "custom"
    description = "$ ./gradlew runOnGitHub # runs on GitHub Action"
}

tasks.register<DefaultTask>("hello") {
    group = "Custom"
    description = "Minimal task that do nothing. Useful to debug a failing build"
}



/**
 * The Gradle Versions Plugin is another Gradle plugin to discover dependency updates
 * plugins.id("com.github.ben-manes.versions")
 * Run it with $ ./gradlew --scan dependencyUpdates
 * https://github.com/ben-manes/gradle-versions-plugin
 * **/
tasks.named("dependencyUpdates", DependencyUpdatesTask::class.java).configure {
    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }

    rejectVersionIf {
        isNonStable(candidate.version)
    }
    checkConstraints = true

}
