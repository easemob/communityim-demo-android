pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.aliyun.com/repository/public/")}
        maven { url = uri("https://developer.huawei.com/repo/")}
        maven { url = uri("https://developer.hihonor.com/repo")}
        maven { url = uri("https://jitpack.io")}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.aliyun.com/repository/public/")}
        maven { url = uri("https://developer.huawei.com/repo/")}
        maven { url = uri("https://developer.hihonor.com/repo")}
        maven { url = uri("https://jitpack.io")}
    }
}

rootProject.name = "chat-android-kotlin"
include(":app")

//include(":ease-im-kit")
//project(":ease-im-kit").projectDir = File("../chatuikit-android/ease-im-kit")
//
//include(":ease-call-kit")
//project(":ease-call-kit").projectDir = File("../easecallkitui-android/ease-call-kit")
//
//include(":hyphenatechatsdk")
//project(":hyphenatechatsdk").projectDir = File("../emclient-android/hyphenatechatsdk")
//
//include(":ease-linux")
//project(":ease-linux").projectDir = File("../emclient-linux")