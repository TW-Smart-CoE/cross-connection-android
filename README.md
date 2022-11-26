# cross-connection-android

## How to import cross-connection-android into Android project

1. Add repository

settings.gradle.kts

```
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      maven { setUrl("https://jitpack.io") }
   }
}
```

settings.gradle

```
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      maven { url 'https://jitpack.io' }
   }
}
```

2. Add dependency

build.gradle.kts

```
dependencies {
   implementation 'com.github.TW-Smart-CoE:cross-connection-android:0.0.4'
}
```

build.gradle

```
dependencies {
   implementation("com.github.TW-Smart-CoE:cross-connection-android:0.0.4")
}
```

## Quick start

```

private val connection = createConnection(context, TCP)

```