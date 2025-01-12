plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    `maven-publish`
    signing
}

android {
    namespace = "com.matnsolutions.matlive_sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
            pom {
                packaging = "aar"
            }
        }
        publications {
            create<MavenPublication>("release") {
                groupId = "io.matlive"
                artifactId = "matlive-android"
                version = "1.0.0"

                from(components["release"])

                pom {
                    name.set("MatLive Android SDK")
                    description.set("Android SDK for MatLive audio room functionality")
                    url.set("https://github.com/MatLiveIO/MatLive-Client-SDK-Android")
                    
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    
                    developers {
                        developer {
                            id.set("matlive")
                            name.set("MatLive Team")
                            email.set("support@matlive.io")
                        }
                    }
                    
                    scm {
                        connection.set("scm:git:git://github.com/MatLiveIO/MatLive-Client-SDK-Android.git")
                        developerConnection.set("scm:git:ssh://github.com:MatLiveIO/MatLive-Client-SDK-Android.git")
                        url.set("https://github.com/MatLiveIO/MatLive-Client-SDK-Android/tree/main")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "MavenCentral"
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = project.findProperty("ossrhUsername")?.toString()
                    password = project.findProperty("ossrhPassword")?.toString()
                }
            }
            maven {
                name = "MavenCentralSnapshots"
                url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                credentials {
                    username = project.findProperty("ossrhUsername")?.toString() ?: System.getenv("OSSRH_USERNAME") ?: ""
                    password = project.findProperty("ossrhPassword")?.toString() ?: System.getenv("OSSRH_PASSWORD") ?: ""
                }
            }
        }
    }
    
    signing {
        sign(publishing.publications["release"])
        publishing.publications.withType<MavenPublication> {
            sign(this)
        }
    }
}
tasks.register("checkCredentials") {
    doLast {
        println("Username: ${project.findProperty("ossrhUsername")}")
        println("Password exists: ${project.findProperty("ossrhPassword") != null}")
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.livekit.android)
    implementation(libs.livekit.android.camerax)
//    implementation("io.ktor:ktor-client-core:2.3.7")
//    implementation("io.ktor:ktor-client-cio:2.3.7")
//    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
//    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}
