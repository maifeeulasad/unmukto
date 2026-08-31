# unmukto
উন্মুক্ত - a Bengali keyboard for Bengali

[![Google Play](https://img.shields.io/badge/Google_Play-Download-414141?style=for-the-badge&logo=google-play)](https://play.google.com/store/apps/details?id=com.mua.unmukto&hl=en)
[![Releases](https://img.shields.io/badge/Unmukto-Releases-black.svg?style=for-the-badge&logo=android)](https://github.com/maifeeulasad/unmukto/releases)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg?style=for-the-badge)](LICENSE)

Two Bengali layouts -- Probhat and one in alphabetical order -- plus an emoji panel.
Works entirely offline, and declares no Android permissions at all.

## Snaps (1.5)

Probhat | Alphabetical | Emoji
:---:|:---:|:---:
![probhat](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/unmukto-1.5-probhat.jpg) | ![alphabetical](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/unmukto-1.5-alphabetical.jpg) | ![emoji](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/unmukto-1.5-emoji.jpg)

Shifted layer | Dark | Dark, emoji
:---:|:---:|:---:
![shifted](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/unmukto-1.5-shifted.jpg) | ![dark](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/unmukto-1.5-dark.jpg) | ![dark emoji](https://raw.githubusercontent.com/maifeeulasad/unmukto/gh-pages/snaps/unmukto-1.5-dark-emoji.jpg)

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
