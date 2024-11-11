# Changelog

## 10.1.1

## 10.1.0

* `Versions`:
  * `kotlin`: `2.0.21`
  * `serialization`: `1.7.3`
  * `coroutines`: `1.9.0`
  * `microutils`: `0.23.0`
  * `tgbotapi`: `20.0.0`
  * `exposed`: `0.55.0`
  * `sqlite`: `3.47.0.0`
  * `koin`: `4.0.0`

## 10.0.0

**OVERALL LOGIC OF PLAGUBOT INITIALIZATION AND WORK HAS BEEN CHANGED**

First of all, since this update `PlaguBot` will use default `StartPlugin` logic and will be built on top of it.
All special methods of `Plugin` will be called from one of `PlaguBot` initialization phases:

* `setupBotClient` will be called from `single` initialization of `telegramBot` (in `setupDI` phase)
* `setupBotPlugin` will be called from `startPlugin` method in time of `buildBehaviourWithFSM` initialization

* `Plugin`:
  * Extension `Module.setupDI(Database,JsonObject)` has been dropped. Use `database` extension in `Module.setupDI(JsonObject)`
* `Bot`:
  * `dev.inmo.plagubot.config.Config` lost its `plugins` section. Now you may retrieve plugins from `Koin` only
  * `defaultJsonFormat` became `Warning` feature due to the fact of its fully default nature
  * `PlaguBot` lost old `start` method and took two new: with `args` as `Array<String>` and `initialConfig` as `JsonObject`

**Migration:**

* If you are running bot and doing it using `StartPlugin` launcher, add `dev.inmo.plagubot.PlaguBot` explicitly
* In plugins: replace your `setupDI` overrides with `Database` as argument by the same one, but `database` will be
available as extension in `single` or `factory` calls (as extension to `Scope` and `Koin`)

## 9.3.0

* `Bot`:
  * Now bot is not built-in into `PlaguBot` and setted up as all other `Koin` dependencies
  * Now it is possible to use `testServer` parameter for bots out of the box
* `Plugin`:
  * New method `setupBotClient` with arguments to let plugin setup bot more freely

## 9.2.0

* `Versions`:
  * `kotlin`: `2.0.20`
  * `serialization`: `1.7.2`
  * `microutils`: `0.22.2`
  * `tgbotapi`: `18.1.0`
  * `exposed`: `0.54.0`
  * `sqlite`: `3.46.1.0`

## 9.1.0

* `Versions`:
  * `tgbotapi`: `17.0.0`

## 9.0.0

* `Versions`:
  * `Kotlin`: `2.0.10`
  * `Serialization`: `1.7.1`
  * `MicroUtils`: `0.22.0`
  * `tgbotapi`: `16.0.0`
  * `Exposed`: `0.53.0`

## 8.5.1

* `Versions`:
  * `MicroUtils`: `0.21.4`
  * `tgbotapi`: `15.2.0`

## 8.5.0

* `Versions`:
  * `MicroUtils`: `0.21.2`
  * `tgbotapi`: `15.1.0`

## 8.4.0

* `Versions`:
  * `Coroutines`: `1.8.1`
  * `MicroUtils`: `0.21.1`
  * `tgbotapi`: `15.0.0`
  * `Exposed`: `0.51.1`

## 8.3.0

* `Versions`:
  * `Serialization`: `1.6.3`
  * `MicroUtils`: `0.20.45`
  * `tgbotapi`: `12.0.1`
  * `Exposed`: `0.49.0`
  * `SQLite`: `3.45.3.0`
  * `Koin`: `3.5.6`

## 8.2.0

* `Versions`:
  * `Coroutines`: `1.8.0`
  * `tgbotapi`: `10.1.0`
  * `MicroUtils`: `0.20.35`

## 8.1.1

* `Versions`:
  * `tgbotapi`: `10.0.1`
  * `MicroUtils`: `0.20.32`
  * `Exposed`: `0.47.0`

## 8.1.0

* Integrate `dev.inmo:micro_utils.startup` into project

## 8.0.0

* `Versions`:
  * `tgbotapi`: `10.0.0`
  * `MicroUtils`: `0.20.26`
  * `Exposed`: `0.46.0`

## 7.4.2

* `Versions`:
  * `Kotlin`: `1.9.22`
  * `tgbotapi`: `9.4.3`
  * `MicroUtils`: `0.20.23`
  * `Koin`: `3.5.7`

## 7.4.1

* `Versions`:
  * `Serialization`: `1.6.2`
  * `tgbotapi`: `9.4.2`
  * `Exposed`: `0.45.0`
  * `SQLite`: `3.44.1.0`
  * `MicroUtils`: `0.20.19`
  * `uuid`: `0.8.2`
  * `ktor`: `2.3.7`

## 7.3.0

* `Versions`:
  * `Kotlin`: `1.9.21`
  * `Serialization`: `1.6.1`
  * `tgbotapi`: `9.4.1`
  * `ktor`: `2.3.6`
  * `KSLog`: Removed explicit dependency, now it is declared in tgbotapi
  * `MicroUtils`: `0.20.15`

## 7.2.3

* `Versions`:
  * `tgbotapi`: `9.2.2`
  * `exposed`: `0.44.0`
  * `koin`: `3.5.0`
  * `ktor`: `2.3.5`

## 7.2.2

* `Bot`:
  * Now you may customize both `onStart` and `onUpdate` conflicts resolvers

## 7.2.1

* `Versions`:
  * `tgbotapi`: `9.2.1`
  * `ktor`: `2.3.4`

## 7.2.0

* `Version`:
  * `tgbotapi`: `9.2.0`
  * `kslog`: `1.1.2`
  * `sqlite`: `3.43.0.0`

## 7.1.0

* `Version`:
  * `microutils`: `0.19.9`
  * `tgbotapi`: `9.1.0`
  * `ktor`: `2.3.3`
  * `coroutines`: `1.7.3`
  * `koin`: `3.4.3`

## 7.0.0

* `Version`:
  * `microutils`: `0.19.7`
  * `tgbotapi`: `9.0.0`
  * `ktor`: `2.3.2`
  * `coroutines`: `1.7.2`

## 6.1.0

* `Version`:
  * `kotlin`: `1.8.22`
  * `microutils`: `0.19.4`
  * `tgbotapi`: `8.1.0`
  * `koin`: `3.4.2`
  * `sqlite`: `3.42.0.0`

## 6.0.1

* `Version`:
  * `microutils`: `0.19.2`
  * `tgbotapi`: `8.0.1`
  * `uuid`: `0.7.1`
  * `ktor`: `2.3.1`
  * `koin`: `3.4.1`

## 6.0.0

* `Versions`:
  * `microutils`: `0.19.1`
  * `tgbotapi`: `8.0.0`
  * `klock`: `4.0.3`

## 5.1.3

* `Versions`:
  * `serialization`: `1.5.1`
  * `microutils`: `0.18.4`
  * `tgbotapi`: `7.1.3`

## 5.1.2

* `Versions`:
  * `microutils`: `0.18.1`
  * `tgbotapi`: `7.1.2`

## 5.1.1

* `Versions`:
    * `kotlin`: `1.8.21`
    * `microutils`: `0.18.0`
    * `tgbotapi`: `7.1.1`

## 5.1.0

* `Versions`:
  * `tgbotapi`: `7.1.0`
  * `sqlite`: `3.41.2.1`

## 5.0.2

* `Versions`:
  * `kotlin`: `1.8.20`
  * `microutils`: `0.17.8`
  * `tgbotapi`: `7.0.2`
  * `kslog`: `1.1.1`
  * `ktor`: `2.3.0`
  * `koin`: `3.4.0`

## 5.0.1

* `Versions`:
  * `tgbotapi`: `7.0.1`

## 5.0.0

* `Versions`:
  * `tgbotapi`: `7.0.0`
  * `microutils`: `0.17.5`

## 4.1.0

* `Versions`:
  * `tgbotapi`: `6.1.0`
  * `microutils`: `0.17.3`

## 4.0.3

* `Versions`:
  * `tgbotapi`: `6.0.3`
  * `microutils`: `0.17.2`

## 4.0.2

* `Versions`:
  * `tgbotapi`: `6.0.2`

## 4.0.1

* `Versions`:
  * `tgbotapi`: `6.0.1`
  * `microutils`: `17.0.1`
  * `ktor`: `2.2.4`

## 4.0.0

* `Versions`:
  * `kotlin`: `1.8.10`
  * `tgbotapi`: `6.0.0`
  * `microutils`: `0.17.0`

## 3.5.0

* `Versions`:
  * `tgbotapi`: `5.2.0`
  * `microutils`: `0.16.10`
  * `koin`: `3.3.2`

## 3.4.1

* `setupBotPlugin` now works synchronously

## 3.4.0

* `Versions`:
  * `tgbotapi`: `5.1.0`
  * `microutils`: `0.16.8`
  * `ktor`: `2.2.3`

## 3.3.1

* `Versions`:
    * `tgbotapi`: `5.0.1`
    * `microutils`: `0.16.6`
    * `ktor`: `2.2.2`

## 3.3.0

* `Versions`:
    * `tgbotapi`: `5.0.0`

## 3.2.3

* `Versions`:
  * `tgbotapi`: `4.2.3`
  * `microutils`: `0.16.4`

## 3.2.2

* `Versions`:
  * `tgbotapi`: `4.2.2`
  * `microutils`: `0.16.2`

## 3.2.1

* `Versions`:
  * `tgbotapi`: `4.2.1`
  * `microutils`: `0.16.0`
  * `ktor`: `2.2.1`

## 3.2.0

* `Versions`:
  * `kotlin`: `1.7.22`
  * `tgbotapi`: `4.2.0`
  * `microutils`: `0.15.0`
  * `kslog`: `0.5.4`
  * `sqlite`: `3.40.0.0`

## 3.1.4

* `Versions`:
  * `tgbotapi`: `4.1.3`
  * `microutils`: `0.14.4`

## 3.1.3

* `Versions`:
  * `tgbotapi`: `4.1.2`

## 3.1.2

* `Versions`:
  * `microutils`: `0.14.2`
  * `exposed`: `0.41.1`

## 3.1.1

* `Versions`:
  * `tgbotapi`: `4.1.1`

## 3.1.0

* `Versions`:
  * `kotlin`: `1.7.21`
  * `microutils`: `0.14.1`
  * `tgbotapi`: `4.1.0`
  * `klock`: `3.4.0`
  * `uuid`: `0.6.0`

## 3.0.0

* `Versions`:
  * `microutils`: `0.14.0`
  * `tgbotapi`: `4.0.0`
  * `kslog`: `0.5.3`
  * `exposed`: `0.40.1`
  * `klock`: `3.3.1`

## 2.4.1

* `Versions`:
  * `microutils`: `0.13.2`
  * `tgbotapi`: `3.3.1`
  * `klock`: `3.3.0`
  * `ktor`: `2.1.3`
  * `koin`: `3.2.2`

## 2.4.0

* `Versions`:
  * `kotlin`: `1.7.20`
  * `serialization`: `1.4.1`
  * `tgbotapi`: `3.3.0`
  * `microutils`: `0.13.1`
  * `klock`: `3.2.0`
  * `ktor`: `2.1.2`

## 2.3.4

* `Versions`:
  * `tgbotapi`: `3.2.7`
  * `microutils`: `0.12.16`

## 2.3.3

* `Versions`:
  * `tgbotapi`: `3.2.6`
  * `sqlite`: `3.39.3.0`

## 2.3.2

* `Versions`:
  * `tgbotapi`: `3.2.3`
  * `microutils`: `0.12.13`
  * `kslog`: `0.5.2`

## 2.3.1

* `Versions`:
  * `klock`: `3.1.0`
  * `tgbotapi`: `3.2.1`
  * `microutils`: `0.12.11`
  * `ktor`: `2.1.1`

## 2.3.0

* `Bot`:
  * Add option `reconnectOptions` in database config

## 2.2.0

* `Versions`:
  * `serialization`: `1.4.0`
  * `tgbotapi`: `3.2.0`
  * `microutils`: `0.12.4`
  * `kslog`: `0.5.1`

## 2.1.1

* `Bot`:
  * Now it is possible to get bot from `koin`

## 2.1.0

* `Versions`:
  * `tgbotapi`: `3.1.1`
  * `ktor`: `2.1.0`
  * `microutils`: `0.12.1`
* `Plugins`:
  * New fum of `Plugin` with `BehaviourContextWithFSM` receiver
* `Bot`:
  * Now bot uses `buildBehaviourWithFSM` to be able to setup bot with FSM

## 2.0.0

* `Versions`:
  * `kotlin`: `1.7.10`
  * `serialization`: `1.4.0-RC`
  * `tgbotapi`: `3.0.2`
  * `kslog`: `0.5.0`
  * `uuid`: `0.5.0`
  * `exposed`: `0.39.2`
  * `microutils`: `0.12.0`

## 1.4.1

* `Versions`:
  * `tgbotapi`: `2.2.2`
  * `kslog`: `0.4.2`

## 1.4.0

* `Versions`:
  * `kslog`: `0.4.1`

## 1.3.1

* `Versions`:
  * `tgbotapi`: `2.2.1`
  * `microutils`: `0.11.13`

## 1.3.0

* `Versions`
  * `tgbotapi`: `2.2.0`

## 1.2.3

* `Versions`
  * `tgbotapi`: `2.1.3`

## 1.2.2

* `Versions`
  * `tgbotapi`: `2.1.2`
  * `microutils`: `0.11.12`
  * `coroutines`: `1.6.3`
  * `ktor`: `2.0.3`

## 1.2.1

* `Versions`
  * `tgbotapi`: `2.1.1`
  * `microutils`: `0.11.6`
  * `kslog`: `0.3.2`

## 1.2.0

* `Versions`
  * `tgbotapi`: `2.1.0`

## 1.1.2

* `Versions`
  * `tgbotapi`: `2.0.3`
  * `microutils`: `0.11.3`
  * `kslog`: `0.3.1`
* `Plugin`:
  * Now it is possible to use `object`s of plugins instead of classes

## 1.1.1

* `Versions`
  * `coroutines`: `1.6.2`
  * `tgbotapi`: `2.0.2`
  * `microutils`: `0.11.0`
  * `ktor`: `2.0.2`
  * `uuid`: `0.4.1`

## 1.1.0

* `Versions`
  * `tgbotapi`: `2.0.0`
  * `microutils`: `0.10.5`
* `Plugin`:
  * All plugins will be loaded in parallel

## 1.0.0

* `Versions`
  * `kotlin`: `1.6.21`
  * `coroutines`: `1.6.1`
  * `serialization`: `1.3.3`
  * `exposed`: `0.38.2`
  * `tgbotapi`: `1.1.0`
  * `microutils`: `0.10.4`
* `Common`:
  * ___ALL THE SDI/KLASSINDEX FUNCTIONALITY HAS BEEN REMOVED___
* `Plugin`:
  * Now plugins must have empty constructor
  * Now plugins may provide realization of two methods: `setupDI` and `setupBotPlugin`
* `PlaguBot`:
  * `Config` now is simple serializable `data class`
  * `PlaguBot` now is more simple as a plugin

## 0.5.1

* `Versions`
  * `tgbotapi`: `0.38.4`
  * `microutils`: `0.9.5`

## 0.5.0

* `Versions`
  * `kotlin`: `1.6.10`
  * `coroutines`: `1.6.0`
  * `serialization`: `1.3.2`
  * `exposed`: `0.37.2`
  * `tgbotapi`: `0.38.0`
  * `microutils`: `0.9.0`

## 0.4.1

Temporal update for compatibility with java 1.8

## 0.3.2

* `Versions`
  * `kotlin`: `1.5.20` -> `1.5.31`
  * `coroutines`: `1.5.0` -> `1.5.2`
  * `serialization`: `1.2.1` -> `1.2.2`
  * `exposed`: `0.32.1` -> `0.34.2`
  * `tgbotapi`: `0.35.1` -> `0.35.9`
  * `microutils`: `0.5.15` -> `0.5.28`

## 0.3.1

* `Versions`
  * `kotlin`: `1.5.10` -> `1.5.20`
  * `tgbotapi`: `0.35.0` -> `0.35.1`
  * `microutils`: `0.5.7` -> `0.5.15`

## 0.3.0

* `Versions`
  * `kotlin`: `1.4.32` -> `1.5.10`
  * `coroutines`: `1.4.3` -> `1.5.0`
  * `serialization`: `1.1.0` -> `1.2.1`
  * `exposed`: `0.31.1` -> `0.32.1`
  * `sdi`: `0.4.1` -> `0.5.0`
  * `tgbotapi`: `0.34.1` -> `0.35.0`
  * `microutils`: `0.4.36` -> `0.5.7`
* `Bot`
  * Add plugin `PluginsHolder`
  * Rewrite mechanism of `Config` working
  * `PlaguBot` now is correctly serializable/deserializable

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
