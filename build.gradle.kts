plugins {
    kotlin("multiplatform") version "1.6.20-M1"
    kotlin("plugin.serialization") version "1.6.20-M1"
    `maven-publish`
    signing
}

group = "io.github.darkweird"
version = "0.3.1"

repositories {
    mavenCentral()
}

val ktorVersion = "2.1.0"

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        compilations.all {
            kotlinOptions.freeCompilerArgs +=
                listOf("-memory-model", "experimental")
        }
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }

    }
    ios()

    js(BOTH) {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
            testTask {
                useMocha()
            }
        }
        nodejs {
            testTask {
                useMocha()
            }
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }


    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-auth:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0-native-mt")
                implementation("io.ktor:ktor-client-mock:$ktorVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val jvmTest by getting


        val iosMain by getting {
            dependencies {
                // TODO replace with CIO when will support tls
                implementation("io.ktor:ktor-client-ios:$ktorVersion")
            }
        }
        val iosTest by getting

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
            }
        }
        val jsTest by getting
        val nativeMain by getting {
            dependencies {
                // TODO replace with CIO when will support tls
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
            }
        }
        val nativeTest by getting
    }
}

val emptyJar = tasks.register<Jar>("emptyJar") {
    archiveAppendix.set("empty")
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    extra["signing.gnupg.keyName"] = signingKey
    extra["signing.gnupg.passphrase"] = signingPassword
    useGpgCmd()
    sign(the<PublishingExtension>().publications)
}

publishing {
    repositories {
        val ossUser: String? by project
        val ossPass: String? by project
        maven {
            name = "sonatypeStaging"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = ossUser
                password = ossPass
            }
        }
    }

    publications {
        withType<MavenPublication> {
            pom {
                name.set(rootProject.name)
                description.set("Async BrowserStack API client in pure Kotlin/MPP")
                url.set("https://github.com/DarkWeird/browserstack.kt")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("DarkWeird")
                        name.set("Nail Khanipov")
                        email.set("foxflameless@gmail.com")
                    }
                }

                scm {
                    developerConnection.set("https://github.com/DarkWeird/browserstack.kt.git")
                    connection.set("https://github.com/DarkWeird/browserstack.kt.git")
                    url.set("https://github.com/DarkWeird/browserstack.kt")
                }
            }
        }
        kotlin.targets.forEach { target ->
            val publication = publications.findByName(target.name) as? MavenPublication ?: return@forEach

            if (target.platformType.name == "jvm") {
                publication.artifact(emptyJar) {
                    classifier = "javadoc"
                }
            } else {
                publication.artifact(emptyJar) {
                    classifier = "javadoc"
                }
                publication.artifact(emptyJar) {
                    classifier = "kdoc"
                }
            }

            if (target.platformType.name == "native") {
                publication.artifact(emptyJar)
            }
        }
    }
}