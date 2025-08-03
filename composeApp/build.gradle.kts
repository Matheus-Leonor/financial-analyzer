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
            implementation("androidx.collection:collection:1.4.0")
            implementation(compose.materialIconsExtended)  // ← ESTA!
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            implementation("org.jetbrains.exposed:exposed-core:0.50.1")
            implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
            implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
            implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.50.1")
            implementation("org.xerial:sqlite-jdbc:3.45.3.0")
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.financialanalyzer.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.financialanalyzer.app"
            packageVersion = "1.0.0"
        }
    }
}
