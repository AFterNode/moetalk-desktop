import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")

    id("org.jetbrains.compose")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cn.afternode"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jogamp.org/deployment/maven")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    implementation("io.netty:netty-all:4.1.110.Final")
    runtimeOnly("io.ktor:ktor-server-core:2.3.11")
    implementation("io.ktor:ktor-server-netty:2.3.11")
    implementation("io.ktor:ktor-server-call-logging:2.3.11")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")

    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.23.1")

    implementation("com.github.ajalt.mordant:mordant:2.6.0")
    implementation("com.github.ajalt.mordant:mordant-coroutines:2.6.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += listOf("-Djava.net.useSystemProxies")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "MoeTalkDesktop"
            packageVersion = project.version as String

            modules("java.management")

            windows {
                console = true

                shortcut = true
                menu = true
                menuGroup = "MoeTalk Desktop"

                upgradeUuid = "A4C80A54-3E5A-440E-B27E-450CED6E7E01"
            }
        }

        buildTypes.release {
            proguard {
                configurationFiles.from("compose-desktop.pro")
            }
        }
    }
}
