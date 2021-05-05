# Changelog

## 0.2.1

* `Versions`
  * `tgbotapi`: `0.33.4` -> `0.34.0`
  * `sqlite`: `3.30.1` -> `3.34.0`

## 0.2.0

* `Versions`
  * `tgbotapi`: `0.33.4` -> `0.34.0`
  * `exposed`: `0.30.2` -> `0.31.1`

## 0.1.9

* `Versions`
  * `tgbotapi`: `0.33.3` -> `0.33.4`
  * `microutils`: `0.4.33` -> `0.4.36`
  * `exposed`: `0.30.1` -> `0.30.2`

## 0.1.8

* `Versions`
  * `tgbotapi`: `0.33.2` -> `0.33.3`
  * `microutils`: `0.4.32` -> `0.4.33`

## 0.1.7

* `Versions`
  * `exposed`: `0.29.1` -> `0.30.1`
  * `tgbotapi`: `0.33.1` -> `0.33.2`
  * `microutils`: `0.4.31` -> `0.4.32`
* `PlaguBot`
  * New class `PlaguBot` (😊)
  * `initPlaguBot` is deprecated
  * New shortcut for params - `plagubot`. `PlaguBot` class can be put inside other plagubot
  for additional opportunities

## 0.1.6

* `Versions`
  * `kotlin`: `1.4.31` -> `1.4.32`
  * `tgbotapi`: `0.33.0` -> `0.33.1`
  * `microutils`: `0.4.29` -> `0.4.31`

## 0.1.5

* `Versions`
  * `kotlin`: `1.4.30` -> `1.4.31`
  * `serialization`: `1.1.0-RC` -> `1.1.0`
  * `coroutines`: `1.4.2` -> `1.4.3`
  * `tgbotapi`: `0.32.8` -> `0.33.0`
  * `microutils`: `0.4.26` -> `0.4.29`

## 0.1.4

* `Versions`
  * `sdi`: `0.4.0-rc2` -> `0.4.1`
  * `tgbotapi`: `0.32.7` -> `0.32.8`
  * `microutils`: `0.4.25` -> `0.4.26`
* `Bot`
  * Fix of [#9](https://github.com/InsanusMokrassar/PlaguBot/issues/9)

## 0.1.3

* `Versions`
  * `tgbotapi`: `0.32.6` -> `0.32.7`
* `Bot`
  * `initPlaguBot` now will return `Job`
* `Plugin`
  * Plugin serializer

## 0.1.2

* `Versions`
  * `tgbotapi`: `0.32.5` -> `0.32.6`

## 0.1.0

* `Versions`
  * `kotlin`: `1.4.21` -> `1.4.30`
  * `serialization`: `1.0.1` -> `1.1.0-RC`
  * `exposed`: `0.28.1` -> `0.29.1`
  * `tgbotapi`: `0.30.10` -> `0.32.5`
  * `microutils`: `0.4.11` -> `0.4.25`
* `Bot`
  * New dependency `sdi`
    * Now it is possible to pass `Module` to configuration for providing a global plugins parameters like different
  common database or tools
* `Plugin`
  * Two new methods `BehaviourContext#invoke`
    * Old method `invoke` has been deprecated

## 0.0.5

* `Versions`
  * `kotlin`: `1.4.10` -> `1.4.21`
  * `kotlin coroutines`: `1.4.1` -> `1.4.2`
  * `tgbotapi`: `0.30.7` -> `0.30.10`
  * `microutils`: `0.4.1` -> `0.4.11`

## 0.0.4

* `Versions`
    * `tgbotapi`: `0.30.4` -> `0.30.7`
    * `microutils`: `0.3.4` -> `0.4.1`

## 0.0.3

* `Bot`:
    * New function `initPlaguBot` which actually will launch the bot

## 0.0.2

* `Versions`
    * `tgbotapi`: `0.30.3` -> `0.30.4`
    * `microutils`: `0.3.2` -> `0.3.3`

## 0.0.1

Inited :)
