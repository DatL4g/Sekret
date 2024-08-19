# Secure Logging

Sekret allows you to conceal sensitive properties in the generated `toString()` method for data and value classes.

This feature is particularly useful for logging user login attempts without exposing credentials or other sensitive information.

## Install

Make sure to apply the Gradle plugin as explained in the [ReadMe](README.md#install)

Add the annotations dependency as follows:

```gradle
dependencies {
	implementation("dev.datlag.sekret:annotations:2.0.0-alpha-06")
}
```

## Usage

Apply the `@Secret` annotation to data and value class properties to mask them in the `toString()` method.

```kotlin
data class User(
	val name: String,
	@Secret val password: String
)

println(User("Hello", "World"))
```

Will result in the following output:

```
User(name=Hello, password=***)
```

### Supported types

| Type          | Kotlin | Java |
| ------------- | :------: | :----: |
| String        | ✅      | ✅    |
| StringBuilder | ✅      | ✅    |
| CharSequence  | ✅      | ✅    |
| Appendable    | ✅      | ✅    |
| StringBuffer  | NaN    | ✅    |
| CharArray     | ❌      | ❌    |

## Custom Configuration

You can modify the behavior lightly by changing the following properties in the gradle plugin.

```gradle
sekret {
    obfuscation {
        secretAnnotation {
            mask.set("###") // default: ***
            maskNull.set(false) // default: true
        }
    }
}
```