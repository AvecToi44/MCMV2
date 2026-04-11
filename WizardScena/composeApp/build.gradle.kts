import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val appVersion = providers.gradleProperty("app.version").orElse("1.0.0").get()

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("com.github.gmazzo.buildconfig") version "5.6.7"
}

buildConfig {
    packageName("org.atrsx.wizardscena")
    buildConfigField("APP_VERSION", provider { appVersion })
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

            implementation("org.apache.poi:poi-ooxml:5.2.5")

            implementation("br.com.devsrsouza.compose.icons:feather:1.1.1")
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
        mainClass = "org.atrsx.wizardscena.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WizardScena ($appVersion)"
            packageVersion = appVersion
            windows {
                iconFile.set(project.file("src/jvmMain/resources/iconapp.ico"))
            }
        }
    }
}
