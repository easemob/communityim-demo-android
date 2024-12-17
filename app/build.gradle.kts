import com.android.build.api.dsl.Ndk
import java.util.*

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
    // Add the Huawei services Gradle plugin
    id("com.huawei.agconnect")
    // Add honor services Gradle plugin
    id("com.hihonor.mcs.asplugin")
    // Add the ksp plugin when using Room
    id("com.google.devtools.ksp")
}

val properties = Properties()
val inputStream = project.rootProject.file("local.properties").inputStream()
properties.load( inputStream )

android {
    namespace = "com.hyphenate.chatdemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hyphenate.chatdemo"
        minSdk = 21
        targetSdk = 35
        versionCode = 135
        versionName = "4.11.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Set app server info from local.properties
        buildConfigField ("String", "APP_SERVER_PROTOCOL", "\"https\"")
        buildConfigField ("String", "APP_SERVER_DOMAIN", "\"${properties.getProperty("APP_SERVER_DOMAIN")}\"")
        buildConfigField ("String", "APP_BASE_USER", "\"${properties.getProperty("APP_BASE_USER")}\"")
        buildConfigField ("String", "APP_BASE_GROUP", "\"${properties.getProperty("APP_BASE_GROUP")}\"")
        buildConfigField ("String", "APP_SERVER_LOGIN", "\"${properties.getProperty("APP_SERVER_LOGIN")}\"")
        buildConfigField ("String", "APP_SEND_SMS_FROM_SERVER", "\"${properties.getProperty("APP_SEND_SMS_FROM_SERVER")}\"")
        buildConfigField ("String", "APP_UPLOAD_AVATAR", "\"${properties.getProperty("APP_UPLOAD_AVATAR")}\"")
        buildConfigField ("String", "APP_GROUP_AVATAR", "\"${properties.getProperty("APP_GROUP_AVATAR")}\"")
        buildConfigField ("String", "APP_RTC_TOKEN_URL", "\"${properties.getProperty("APP_RTC_TOKEN_URL")}\"")
        buildConfigField ("String", "APP_RTC_CHANNEL_MAPPER_URL", "\"${properties.getProperty("APP_RTC_CHANNEL_MAPPER_URL")}\"")

        // Set appkey from local.properties
        buildConfigField("String", "APPKEY", "\"${properties.getProperty("APPKEY")}\"")

        // Set push info from local.properties
        buildConfigField("String", "MEIZU_PUSH_APPKEY", "\"${properties.getProperty("MEIZU_PUSH_APPKEY")}\"")
        buildConfigField("String", "MEIZU_PUSH_APPID", "\"${properties.getProperty("MEIZU_PUSH_APPID")}\"")
        buildConfigField("String", "OPPO_PUSH_APPKEY", "\"${properties.getProperty("OPPO_PUSH_APPKEY")}\"")
        buildConfigField("String", "OPPO_PUSH_APPSECRET", "\"${properties.getProperty("OPPO_PUSH_APPSECRET")}\"")
        buildConfigField("String", "VIVO_PUSH_APPID", "\"${properties.getProperty("VIVO_PUSH_APPID")}\"")
        buildConfigField("String", "VIVO_PUSH_APPKEY", "\"${properties.getProperty("VIVO_PUSH_APPKEY")}\"")
        buildConfigField("String", "MI_PUSH_APPKEY", "\"${properties.getProperty("MI_PUSH_APPKEY")}\"")
        buildConfigField("String", "MI_PUSH_APPID", "\"${properties.getProperty("MI_PUSH_APPID")}\"")
        buildConfigField("String", "FCM_SENDERID", "\"${properties.getProperty("FCM_SENDERID")}\"")
        buildConfigField("String", "HONOR_PUSH_APPID", "\"${properties.getProperty("HONOR_PUSH_APPID")}\"")
        buildConfigField("String", "BUGLY_APPID", "\"${properties.getProperty("BUGLY_APPID")}\"")
        buildConfigField("String", "BUGLY_ENABLE_DEBUG", "\"${properties.getProperty("BUGLY_ENABLE_DEBUG")}\"")

        // Set RTC appId from local.properties
        buildConfigField("String", "RTC_APPID", "\"${properties.getProperty("RTC_APPID")}\"")

        addManifestPlaceholders(mapOf(
            "VIVO_PUSH_APPKEY" to properties.getProperty("VIVO_PUSH_APPKEY", "******"),
            "VIVO_PUSH_APPID" to properties.getProperty("VIVO_PUSH_APPID", "******"),
            "HONOR_PUSH_APPID" to properties.getProperty("HONOR_PUSH_APPID", "******"),
            "BUGLY_APPID" to properties.getProperty("BUGLY_APPID", "******"),
            "BUGLY_ENABLE_DEBUG" to properties.getProperty("BUGLY_ENABLE_DEBUG", "******")
        ))

        //指定room.schemaLocation生成的文件路径  处理Room 警告 Schema export Error
        javaCompileOptions {
            annotationProcessorOptions {
                arguments(mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                ))
            }
        }

        ndk {
            abiFilters .addAll(mutableSetOf("arm64-v8a","armeabi-v7a"))
        }
//        //用于设置使用as打包so时指定输出目录
        externalNativeBuild {
            ndkBuild {
                abiFilters("arm64-v8a","armeabi-v7a")
                arguments("-j8")
            }
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file(properties.getProperty("DEBUG_STORE_FILE_PATH", "./keystore/sdkdemo.jks"))
            storePassword = properties.getProperty("DEBUG_STORE_PASSWORD", "123456")
            keyAlias = properties.getProperty("DEBUG_KEY_ALIAS", "easemob")
            keyPassword = properties.getProperty("DEBUG_KEY_PASSWORD", "123456")
        }
        create("release") {
            storeFile = file(properties.getProperty("RELEASE_STORE_FILE_PATH", "./keystore/sdkdemo.jks"))
            storePassword = properties.getProperty("RELEASE_STORE_PASSWORD", "123456")
            keyAlias = properties.getProperty("RELEASE_KEY_ALIAS", "easemob")
            keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD", "123456")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // Set toolchain version
    kotlin {
        jvmToolchain(8)
    }

    //打开注释后，可以直接在studio里查看和编辑emclient-linux里的代码
//    externalNativeBuild {
//        ndkBuild {
//            path = File("jni/Android.mk")
//        }
//    }
//    ndkVersion = "19.2.5345600"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
    implementation("io.github.scwang90:refresh-header-material:2.1.0")
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
    implementation("pub.devrel:easypermissions:3.0.0")
    // lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    // lifecycle viewmodel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // coroutines core library
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // coroutines android library
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // hms push
    implementation("com.huawei.hms:push:6.12.0.300")
    // xiaomi push
    implementation(files("libs/MiPush_SDK_Client_6_0_1-C_3rd.aar"))
    // hihonor push
    implementation("com.hihonor.mcs:push:7.0.61.303")
    // meizu push
    implementation("com.meizu.flyme.internet:push-internal:4.3.0")//配置集成sdk
    // vivo push
    implementation(files("libs/vivo_push_v4.0.4.0_504.aar"))
    //oppo push
    implementation(files("libs/oppo_push_3.5.2.aar"))
    //oppo push需添加以下依赖
    implementation("com.google.code.gson:gson:2.6.2")
    implementation("commons-codec:commons-codec:1.6")
    implementation("androidx.annotation:annotation:1.1.0")
    // Google firebase cloud messaging
    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))

    // Declare the dependencies for the Firebase Cloud Messaging and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")

    // image corp library
    implementation("com.github.yalantis:ucrop:2.2.8")
    // bugly
    implementation("com.tencent.bugly:crashreport:4.1.9.3")

    // Coil: load image library
    implementation("io.coil-kt:coil:2.5.0")
    // Room
    implementation("androidx.room:room-runtime:2.5.1")
    ksp("androidx.room:room-compiler:2.5.1")
    // optional - Kotlin Extensions and Coroutines support for Room
    // To use Kotlin Flow and coroutines with Room, must include the room-ktx artifact in build.gradle file.
    implementation("androidx.room:room-ktx:2.5.1")

    implementation("io.hyphenate:ease-chat-kit:4.11.1")
//    implementation(project(mapOf("path" to ":ease-im-kit")))

    implementation("io.hyphenate:ease-call-kit:4.11.0")
//    implementation(project(mapOf("path" to ":ease-call-kit")))

    implementation("io.hyphenate:hyphenate-chat:4.11.0")
//    implementation(project(mapOf("path" to ":hyphenatechatsdk")))
}