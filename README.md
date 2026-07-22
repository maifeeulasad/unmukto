# unmukto
উন্মুক্ত - a Bengali keyboard for Bengali

[![Google Play](https://img.shields.io/badge/Google_Play-Download-414141?style=for-the-badge&logo=google-play)](https://play.google.com/store/apps/details?id=com.mua.unmukto&hl=en)
[![Releases](https://img.shields.io/badge/Unmukto-Releases-black.svg?style=for-the-badge&logo=android)](https://github.com/maifeeulasad/unmukto/releases)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge)](LICENSE)

Probhat layout, works entirely offline, and declares no Android permissions at all.

## Snaps (2026.07.22)

Base layer             |  Shifted layer
:-------------------------:|:-------------------------:
![keyboard-1](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/Screenshot_2026-07-22-14-29-06-954_com.mua.unmukto.jpg)  |  ![keyboard-2](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/Screenshot_2026-07-22-14-29-10-359_com.mua.unmukto.jpg)

## Building

```sh
./gradlew assembleDebug
```

Requires JDK 17. The Android SDK location is read from `local.properties` or `ANDROID_HOME`.

## License

Unmukto is free software, licensed under the [GNU General Public License v3.0 or later](LICENSE).

It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

## Publishing on F-Droid

See [docs/fdroid-publishing.md](docs/fdroid-publishing.md).
