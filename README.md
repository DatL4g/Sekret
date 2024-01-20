# Sekret

The Sekret plugin allows any Kotlin developer to deeply hide secrets in your project. This prevents **credentials harvesting** to a certain level.

The following obfuscation techniques are used:

- The secret is obfuscated using a reversible XOR operation, so it never appears as plain text
- Obfuscated secret is stored in a native binary as a hexadecimal array, so it's hard to decompile
  - **This is not the case for javascript modules**
- The obfuscated string is not persisted in the binary to force runtime evaluation

This plugin is a more flexible and **Kotlin Multiplatform** compatible version of [klaxit/hidden-secrets-gradle-plugin](https://github.com/klaxit/hidden-secrets-gradle-plugin)

⚠️ Nothing on the client-side is unbreakable. So generally speaking, **keeping a secret in application package is not a smart idea**. But when you absolutely need to, this is the best method to hide it.

This plugin can be used with any Kotlin project (single-, or multiplatform).

## Install

This project is available through `mavenCentral`.

### Gradle Plugin

#### ⚠️ Make sure to apply the plugin to the desired project/module only!

```gradle
plugins { 
    id("dev.datlag.sekret") version "1.0.0"
}
```

### Plugin configuration

You can call the following after applying the plugin in your `build.gradle.kts`

```gradle
sekret {
    packageName.set("your.package.name")
    encryptionKey.set("yourUniqueEncryption") // default is the specified packageName
    // more configuration...
}
```

## Setup

The plugin will generate a separate module, since it needs to compile for native targets even if you just use Android or JVM for example.

### Add generated sekret module

Therefore, you need to include the generated sekret module in your `settings.gradle.kts`.

⚠️ Make sure to exclude this module by adding it to your `.gitignore`

```gradle
include(":sample", ":sample:sekret")
```

### Add secrets

Create a `sekret.properties` file in the project/module where you applied the plugin and simply add key-value pairs.

```properties
YOUR_KEY_NAME=yourSuperSecretSecret
OTHER_SECRET=th1s1s4n0th3rS3cr3t
```

You can change the name and location by changing the sekret configuration:

```gradle
sekret {
  propertiesFile.set(project.layout.projectDirectory.file("my-secrets.properties"))
}
```

⚠️ Make sure to exclude this file by adding it to your `.gitignore`

### Generate secrets

To generate source code for your provided secrets, just call `./gradlew generateSekret`.

### Customize targets

You can change the generated `build.gradle.kts` depending on your needs, just make sure to apply the required native targets:

- For **Android**: `androidNativeX`
- For **JVM**: `linuxX`, `mingwX` and `macOsX`, depending on your output platform.

If you misconfigured your build script or added new targets to your base module, you can run `./gradlew sample:generateSekretBuildScript`.

## Compose

Make sure to configure the compose application correctly, take a look at the sample project.

- If you use **Android** or **JVM** target you have to call `./gradlew createAndCopySekretNativeLibrary`.
- If you use any **JS** target you can just use the generated `Sekret` class as is.

### Load secrets in code

```kotlin
val binaryLoded = NativeLoader.loadLibrary("sekret", System.getProperty("compose.application.resources.dir")?.let { File(it) })

if (binaryLoded) {
  val yourKeyName = Sekret().yourKeyName("your-encryption-key")
  val otherSecret = Sekret().otherSecret("your-encryption-key")
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