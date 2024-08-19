# Sekret

This project is a security-focused tool designed to enhance application amd user safety by preventing accidental leaks of sensitive information.

It can be used to hide user credentials in logs and provides powerful obfuscation techniques to protect application secrets by embedding them securely in native binaries.

With planned features like class-level String obfuscation, Sekret is your solution for safeguarding confidential data and maintaining secure codebases.

## Install

The functionality and all features is based on the Gradle and Compiler plugin, which is available through `mavenCentral`.

```gradle
plugins { 
    id("dev.datlag.sekret") version "2.0.0-alpha-06"
}
```

## Why Alpha version?

The latest version is currently in alpha stage, reflecting our adherence to versioning standards.

While the core features are stable and fully functional, the project remains in alpha due to the ongoing development of a critical feature.

This versioning approach ensures transparency and sets clear expectations as we finalize the remaining feature, paving the way for a stable release.

## 🔒 Secure Logging

Prevent leaking credentials by using the Secret annotation.

Read more [here](Logging.md)

## 🤐 Application Secrets

Hide application secrets deeply (in native binaries).

Read more [here](Secrets.md)

## Support the project

[![Github-sponsors](https://img.shields.io/badge/sponsor-30363D?style=for-the-badge&logo=GitHub-Sponsors&logoColor=#EA4AAA)](https://github.com/sponsors/DATL4G)
[![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/datlag)

### This is a non-profit project!

Sponsoring to this project means sponsoring to all my projects!
So the further text is not to be attributed to this project, but to all my apps and libraries.

Supporting this project helps to keep it up-to-date. You can donate if you want or contribute to the project as well.
This shows that the library is used by people, and it's worth to maintain.