# Sekret

The Sekret plugin allows any Kotlin developer to deeply hide secrets in it's project.
This prevents **credentials harvseting** to a certain level.

The following obfuscation techniques are used:

- the secret is obfuscated using a reversible XOR operation, so it never appears as plain text
- obfuscated secret is stored in a native binary as a hexadecimal array, so it's hard to decompile
- the obfuscated string is not persisted in the binary to force runtime evaluation

This plugin is a more flexible and **Kotlin Multiplatform** compatible version of [klaxit/hidden-secrets-gradle-plugin](https://github.com/klaxit/hidden-secrets-gradle-plugin)

⚠️ Nothing on the client-side is unbreakable. So generally speaking, **keeping a secret in a application package is not a smart idea**. But when you absolutely need to, this is the best method to hide it.

This plugin can be used with any Kotlin project (single-, or multiplatform).

## Install

### Snapshot

This project is currently available as snapshot version only!

⚠️ The snapshot **can not be used with any Apple target**, for example macOsX64 or iosX64

To access the snapshots add the follwoing to your repository resolution:

```kotlin
maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
```

### Maven Central

This project is available through `mavenCentral`

Make sure to add `mavenCentral` to your repository resolution.

Then apply the gradle plugin to the desired project in the corresponding `build.gradle.kts`

```kotlin
plugins {
  id("dev.datlag.sekret") version "0.1.0-SNAPSHOT"
}
```

#### ⚠️ Make sure to apply the plugin to the desired project only!

If you have a multimodule project with this structure for example and need your secrets in the app module:

```
app \
  build.gradle.kts
network \
  build.gradle.kts
build.gradle.kt
settings.gradle.kts
```

Then apply the plugin to the `app/build.gradle.kts` only.

##### Multiple secrets

If you need different secrets for `app` and `network`, you can add the plugin to you root `build.gradle.kts`

```kotlin
plugins {
  id("dev.datlag.sekret") version "0.1.0-SNAPSHOT" apply false
}
```

And apply them in the `app` and `network` module `build.gradle.kts`

```kotlin
plugins {
  id("dev.datlag.sekret")
}
```

#### Plugin configuration

You can configure the plugin by calling, the follwoing in your `build.gradle.kts`

```kotlin
sekret {
  packageName = "your.package.name" // default is "dev.datlag.sekret"
  password = "yourExamplePassword" // default is the specified packageName
  propertiesFile = "mysecrets.properties" // default is sekret.properties, you can specify a directory or full file path as well if you want
}
```

## Generate secrets

If you have multiple modules the use the plugin, the correct location of your `propertiesFile` matters.

If you don't specify the full file path, it will search in the project directory where the plugin is applied and if none is found, it will use the root properties file.

**⚠️ It will never use the parent project file, unless configured otherwise**

Then in your `properties` file add your secrets as a simple key-value pair

```properties
YOUR_KEY_NAME=yourSuperSecretSecret
OTHER_SECRET=th1s1s4n0th3rS3cr3t
```

⚠️ Make sure to exclude this file by adding it to your `.gitignore`

### Generate

To generate the native secrets call `./gradlew yourProject:generateSekret` or add a task in your `build.gradle.kts`, depending on `generateSekret`

If we take our previous example it looks like this: `./gradlew app:generateSekret`

You will see that a new submodule will be created called sekret, make sure to add it to your `settings.gradle.kts`

```kotlin
include("app:sekret")
```

If you didn't have this `include` before, you have to re-run the `generateSekret` task.

#### Customize targets

You can change the generated `build.gradle.kts` depending on your needs, just make sure to apply the required native targets:

If you use `android`, make sure to add (all) `androidNativeX`

If you use `jvm`, make sure to add `linuxX`, `mingwX` and `macOsX`, depending on your `jvm` output platform.

## Use secrets

First you have to compile the sekret module to a binary, you can do this by calling `./gradlew yourproject:sekret:assemble`, the binary will probably located under `yourproject/sekret/build/bin/(target)/releaseShared/libsekret.(targetEnding)`

You can use my implementation of the binary loader called `NativeLoader`, available for **Android** and **JVM**, or use your own.

Just call it like the following in your application code:

```kotlin
val binaryLoded = NativeLoader.loadLibrary("sekret", File("path/to/libsekret.xx"))

if (binaryLoaded) {
  val password = getPackageName() // depends on your plugin configuration
  val yourKeyName = Sekret().yourKeyName(password)
  val otherSecret = Sekret().otherSecret(password)
} else {
  // use your own binary loader
}
```

## Support the project

[![Github-sponsors](https://img.shields.io/badge/sponsor-30363D?style=for-the-badge&logo=GitHub-Sponsors&logoColor=#EA4AAA)](https://github.com/sponsors/DATL4G)
[![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/datlag)

### This is a non-profit project!

Sponsoring to this project means sponsoring to all my projects!
So the further text is not to be attributed to this project, but to all my apps and libraries.

Supporting this project helps to keep it up-to-date. You can donate if you want or contribute to the project as well.
This shows that the library is used by people, and it's worth to maintain.
