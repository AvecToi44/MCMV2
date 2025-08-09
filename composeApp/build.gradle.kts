import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

            implementation("com.fazecast:jSerialComm:2.9.3")

            implementation("org.apache.poi:poi:5.0.0")

            implementation("org.jfree:jcommon:1.0.24")
            implementation("org.jfree:jfreechart:1.5.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
            implementation("io.github.thechance101:chart:1.1.0")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "ru.atrs.mcm.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageVersion = "1.2.12"
            packageName = "MCM (${packageVersion})"

        }
        buildTypes.release {
            proguard {
                //isEnabled.set(false)
                configurationFiles.from("compose-desktop.pro")
            }
        }
    }
}
