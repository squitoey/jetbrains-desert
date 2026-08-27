# Desert Theme for JetBrains IDEs

A port of Hans Fugal's [desert.vim](https://github.com/fugalh/desert.vim) (2003)
to the JetBrains platform — warm sand and khaki on soft charcoal, sky-blue
comments, pale-green functions, olive selections.

One plugin, two themes: **Desert** (dark, transcribed from vim) and
**Desert Light** (derived — see below). Each ships its own editor colour
scheme; pick either in Settings → Appearance.

The plugin declares only `com.intellij.modules.platform`, so it installs in
**every** JetBrains IDE: IntelliJ IDEA, PyCharm, WebStorm, PhpStorm, GoLand,
RubyMine, CLion, Rider, DataGrip, RustRover, Aqua, Android Studio.

## Requirements

A JDK (17 or newer) and Gradle. Gradle is not bundled here — there is no
committed wrapper, because generating one requires Gradle in the first place.
Get it either way:

**Install Gradle, then generate the wrapper once:**

```sh
brew install gradle     # or: sdk install gradle
gradle wrapper          # writes ./gradlew, gradle/wrapper/ — commit these
```

After that everyone uses `./gradlew` and nobody needs Gradle installed.

**Or open the project in IntelliJ IDEA.** It ships its own Gradle, imports the
project on open, and lists every task below in the Gradle tool window
(View → Tool Windows → Gradle).

## Build

```sh
./gradlew buildPlugin
```

Produces `build/distributions/desert-1.1.0.zip` — the installable plugin,
containing both themes and both editor schemes.

```sh
./gradlew runIde
```

Launches a sandboxed IDE with the plugin already loaded. This is the fastest
way to iterate: change a colour, re-run, look at it. The sandbox keeps its own
settings, so it will not disturb your real IDE.

```sh
./gradlew verifyPlugin
```

Runs JetBrains' plugin verifier against the built artifact — worth doing before
publishing.

All three depend on `checkThemeParity`, a task in `build.gradle.kts` that fails
the build if the dark and light themes stop defining the same keys (339 editor
scheme keys, 525 `ui` keys each) or if a colour reference does not resolve. It
also rejects `baseAttributes`, which silently resolves to nothing once a scheme
is saved and leaves those keys rendering as plain text.

## Install

**For iterating** — use `./gradlew runIde` rather than installing.

**For real use** — Settings → Plugins → ⚙ → *Install Plugin from Disk…* → pick
`build/distributions/desert-1.1.0.zip` → restart. Then Settings → Appearance &
Behavior → Appearance → Theme → **Desert** or **Desert Light**.

**Editor colours only** — if you would rather keep your current IDE chrome:
Settings → Editor → Color Scheme → ⚙ → *Import Scheme…* → pick
`src/main/resources/themes/Desert.xml` or `DesertLight.xml`.

Pick one route or the other, not both — an imported scheme shadows the
plugin's. See [Troubleshooting](#troubleshooting).

## Versioning

`gradle.properties` holds the coordinates:

```properties
pluginVersion = 1.1.0
pluginSinceBuild = 213
platformVersion = 2024.3
```

`pluginVersion` names the distribution zip and is written into `plugin.xml` at
build time by `patchPluginXml` — `plugin.xml` itself carries no `<version>` or
`<idea-version>`. `platformVersion` only decides which IDE `runIde` launches
and which one the verifier checks against; a theme contains no code, so it does
not affect what the plugin supports.

## Signing and publishing

Both need credentials. **None of them belong in this repository** —
`gradle.properties` here is committed and holds only version numbers. Put them
in `~/.gradle/gradle.properties` instead, which Gradle reads for every build on
your machine and which is never part of any repo.

### One-time: generate a signing key

The Marketplace requires signed plugins. Generate a 4096-bit key and a
self-signed certificate chain, somewhere outside this repo:

Nothing here comes from JetBrains — you generate all of it, and the
certificate is self-signed. The Marketplace only cares that successive uploads
are signed by the *same* key.

```sh
mkdir -p ~/.gradle/desert-signing && cd ~/.gradle/desert-signing

# 1. private key, encrypted with a passphrase you choose (prompts twice)
openssl genpkey -aes-256-cbc -algorithm RSA \
  -out private_encrypted.pem -pkeyopt rsa_keygen_bits:4096

# 2. a decrypted copy, only needed to generate the certificate below
openssl rsa -in private_encrypted.pem -out private.pem

# 3. the self-signed certificate chain
openssl req -key private.pem -new -x509 -days 365 -out chain.crt

chmod 600 private_encrypted.pem private.pem
```

**Sign with the encrypted key** (`private_encrypted.pem`) and the passphrase —
that is what `desert.signing.password` is for. The signer accepts either form,
but a decrypted key makes the passphrase pointless: anyone who gets the file
can sign as you. `private.pem` is only needed for step 3; you can delete it
afterwards.

Keep `private_encrypted.pem` and `chain.crt`. Losing them means you cannot
publish an update the Marketplace will accept as the same author.

### One-time: get a Marketplace token

Generate one at
[plugins.jetbrains.com/author/me/tokens](https://plugins.jetbrains.com/author/me/tokens)
→ *Generate Token*. Copy it immediately — it is shown once.

### Put the four keys in `~/.gradle/gradle.properties`

Create the file if it does not exist:

```properties
# JetBrains plugin signing (paths, not contents - `~` is expanded)
desert.signing.certificateChainFile = ~/.gradle/desert-signing/chain.crt
desert.signing.privateKeyFile       = ~/.gradle/desert-signing/private_encrypted.pem
desert.signing.password             = your-key-passphrase

# JetBrains Marketplace
desert.publishing.token             = perm:xxxxxxxxxxxxxxxxxxxx
```

```sh
chmod 600 ~/.gradle/gradle.properties
```

| Key | What it is |
| --- | --- |
| `desert.signing.certificateChainFile` | Path to `chain.crt` from step 3 |
| `desert.signing.privateKeyFile` | Path to `private_encrypted.pem` from step 1 |
| `desert.signing.password` | The passphrase from step 1, which decrypts that key |
| `desert.publishing.token` | Marketplace permanent token |

These are **paths**, not file contents. The private key and certificate are
multi-line PEM, which a `.properties` file cannot hold without escaping every
newline — pointing at the files avoids that entirely.

### On CI

Do not use a properties file. The build falls back to environment variables,
which is what a CI secret store provides:

| Environment variable | Replaces |
| --- | --- |
| `PRIVATE_KEY_PASSWORD` | `desert.signing.password` |
| `PUBLISH_TOKEN` | `desert.publishing.token` |

For the key and certificate on CI, write the secrets to temporary files during
the job and pass their paths with
`-Pdesert.signing.privateKeyFile=... -Pdesert.signing.certificateChainFile=...`.

### Publish

```sh
./gradlew publishPlugin
```

`signPlugin` runs automatically first. Bump `pluginVersion` in
`gradle.properties` before each release — the Marketplace rejects a version it
has already seen.

To check signing without publishing:

```sh
./gradlew signPlugin verifyPluginSignature
```

That writes `build/distributions/desert-<version>-signed.zip` alongside the
unsigned archive and verifies the signature on it. The release workflow
attaches the signed one when it exists.

Everything above is inert for ordinary builds. `buildPlugin` and `runIde` never
read these properties, so you can ignore this whole section until you publish.

## Continuous integration

Two workflows.

**`.github/workflows/build.yml`** — on every push and pull request, and on
demand. Runs `checkThemeParity`, `verifyPluginProjectConfiguration`,
`verifyPluginStructure` and `buildPlugin`, then uploads the distribution zip as
a job artifact. No secrets needed.

**`.github/workflows/release.yml`** — on a pushed tag. Checks the tag matches
`pluginVersion` in `gradle.properties`, builds, signs if the secrets are
present, and opens a **draft** GitHub release with the zip attached. Publish
the draft yourself once you have read the notes.

`.github/dependabot.yml` keeps the Gradle dependencies and the actions
themselves up to date.

### Repository secrets

Only `release.yml` uses them, and only for signing. Set them under
Settings → Secrets and variables → Actions:

| Secret | Value |
| --- | --- |
| `PRIVATE_KEY` | Contents of `private_encrypted.pem` |
| `CERTIFICATE_CHAIN` | Contents of `chain.crt` |
| `PRIVATE_KEY_PASSWORD` | The key passphrase |

Set them from the files rather than pasting, which avoids mangling newlines:

```sh
gh secret set PRIVATE_KEY          < ~/.gradle/desert-signing/private_encrypted.pem
gh secret set CERTIFICATE_CHAIN    < ~/.gradle/desert-signing/chain.crt
gh secret set PRIVATE_KEY_PASSWORD          # prompts, nothing echoed
```

Paste the PEM files' full contents, `-----BEGIN`/`-----END` lines included.
GitHub secrets hold multi-line values fine, so no base64 is needed. The
workflow writes them to files under `$RUNNER_TEMP`, passes the paths to Gradle,
and deletes them in the same step.

If the secrets are absent the signing step is skipped and the release is still
created, unsigned — useful on a fork, or before you have keys. The Marketplace
requires a signed archive, so set them before publishing for real.

There is no `PUBLISH_TOKEN` secret, because publishing is not automated. See
[Signing and publishing](#signing-and-publishing).

### What CI does not run

`verifyPlugin` — the JetBrains Plugin Verifier — is not in either workflow. It
downloads every IDE build in the supported range, which for `since-build=213`
is around **47 GB**, far past what a CI cache can hold. It also checks *binary*
compatibility, and a theme ships no bytecode (`compileJava` is `NO-SOURCE`), so
there is little for it to find. Run it locally before a release:

```sh
./gradlew verifyPlugin
```

The last full run reported `Compatible` against all twelve builds from
IC-213.7172.25 through IC-252.28539.97.

### Two warnings you will see, and can ignore

`verifyPluginProjectConfiguration` reports both on every run:

- *since-build is lower than target platform version* — deliberate. The plugin
  is built against `platformVersion` but supports back to `sinceBuild`, and the
  Plugin Verifier confirms that range is real.
- *Java targetCompatibility exceeds since-build requirements* — inapplicable.
  There are no Java sources, so no bytecode is produced at any level.

Neither fails the build.

## Layout

```
build.gradle.kts                 IntelliJ Platform Gradle Plugin, repositories,
                                 and the checkThemeParity task
settings.gradle.kts              project name
gradle.properties                plugin and platform versions
src/main/resources/
  META-INF/plugin.xml            plugin descriptor, two themeProviders
  themes/Desert.theme.json       IDE chrome, dark
  themes/Desert.xml              editor colour scheme, dark
  themes/DesertLight.theme.json  IDE chrome, light
  themes/DesertLight.xml         editor colour scheme, light
MAPPING.md                       every vim group → IntelliJ key, and why
LICENSE                          MIT, plus attribution for the vim original
.github/workflows/build.yml      check + build on push and PR
.github/workflows/release.yml    tag → signed draft GitHub release
.github/dependabot.yml           daily Gradle + actions updates
```

Each `*.theme.json` is named after the `name` it declares, matching the
platform's convention, and is registered by its own `<themeProvider>` in
`plugin.xml`.

## Dark and light

**Dark is desert.vim, transcribed.** X11 names resolved exactly as vim
resolves them; see `MAPPING.md` for every group.

**Light is derived, not transcribed.** desert.vim is `set background=dark` and
has no light counterpart — every colour in it is a light tint chosen to sit on
charcoal, and khaki on white is unreadable. Each light hue is its dark sibling
pulled to roughly 30–40% lightness with hue and role held constant: comments
stay sky blue, keywords stay khaki, functions stay green. The ground is
desert's own sand `#c2bfa5` lightened to parchment.

A handful of keys can't be derived by hue alone and are set explicitly in
`DesertLight.xml` — search highlights (which must stay light backgrounds with
dark text) and the ANSI console slots (where `black` must stay dark and
`white` must stay readable).

## Tweaking

`Desert.theme.json` routes every UI colour through a named colour at the top of
the file, so most changes are one line. Two you may actually want:

- **Tan status bar.** desert.vim's signature is a `#c2bfa5` status line with
  black text. That's set to a dark bar with tan text by default, because IDE
  status-bar icons are drawn for dark backgrounds and disappear on tan. For the
  full vim look, set `desertStatusBarBg` to `#c2bfa5` and `desertStatusBarFg`
  to `#000000`.
- **TODO highlighting.** Faithfully orangered-on-yellow2, which is loud in a
  gutter-wide IDE. Soften `TODO_DEFAULT_ATTRIBUTES` in `Desert.xml`.

After editing, re-run `./gradlew runIde` to see the change.

## Troubleshooting

**Colours don't change no matter how often you reinstall.** You probably
imported `Desert.xml` as a scheme at some point *and* installed the plugin. Both
register a scheme called "Desert", and the imported one wins — so the plugin
installs correctly and is then ignored.
Fix it by quitting the IDE (config is rewritten on exit, so deleting it while
the IDE runs won't stick), removing the file, and restarting:

```sh
rm ~/Library/Application\ Support/JetBrains/<IDE>/colors/Desert.icls
```

Pick one delivery route or the other, not both.

**Comments render white.** Symptom of the same thing. The IDE rewrites
`baseAttributes="DEFAULT_COMMENT"` to `baseAttributes=""` when it saves an
imported scheme, and an empty inherit resolves to plain text. Every attribute
in `Desert.xml` is set explicitly for this reason — never reintroduce
`baseAttributes`.

## Credit

Original colour scheme by Hans Fugal <hans@fugal.net>, distributed with vim.
This is an adaptation of that palette; see `MAPPING.md` for what was
transcribed and what had to be invented.
