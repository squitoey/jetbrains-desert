# Desert for JetBrains IDEs

A port of Hans Fugal's [desert.vim](https://github.com/fugalh/desert.vim) (2003)
to the JetBrains platform — warm sand and khaki on soft charcoal, sky-blue
comments, pale-green functions, olive selections.

One plugin, two themes: **Desert** (dark, transcribed from vim) and
**Desert Light** (derived — see below). Each ships its own editor colour
scheme; pick either in Settings → Appearance.

The plugin declares only `com.intellij.modules.platform`, so it installs in
**every** JetBrains IDE: IntelliJ IDEA, PyCharm, WebStorm, PhpStorm, GoLand,
RubyMine, CLion, Rider, DataGrip, RustRover, Aqua, Android Studio.

## Build

```sh
./build.sh
```

No JDK or Gradle required — a theme plugin is pure resources, and `build.sh`
validates then zips them. It produces:

| File | What it is |
| --- | --- |
| `build/Desert-1.1.0.zip` | Installable plugin: both themes **and** both editor schemes |
| `build/desert.icls` | Dark editor colour scheme only |
| `build/desert-light.icls` | Light editor colour scheme only |

`build.sh` also checks that the two themes stay structurally identical — same
editor-scheme keys, same `ui` keys. A key added to one and not the other fails
the build rather than quietly leaving light and dark to drift apart.

## Install

**Script** — installs into every JetBrains IDE found on this machine:

```sh
./build.sh && ./install.sh
```

Restart the IDE, then Settings → Appearance & Behavior → Appearance → Theme →
**Desert**. `./install.sh WebStorm` limits it to one IDE, `./install.sh
--uninstall` removes it again. This just unpacks the zip into each IDE's
`plugins/` directory — exactly what the UI route below does.

**UI** — Settings → Plugins → ⚙ → *Install Plugin from Disk…* → pick
`build/Desert-1.1.0.zip` → restart → set the theme as above.

**Editor colours only** — if you'd rather keep your current IDE chrome and just
take the syntax colours: Settings → Editor → Color Scheme → ⚙ →
*Import Scheme…* → pick `build/desert.icls` → choose **Desert**.

Note that installing the plugin makes Desert *available*; it doesn't switch
your theme. You pick it in Settings.

## Layout

```
src/main/resources/
  META-INF/plugin.xml            plugin descriptor
  themes/desert.theme.json       IDE chrome, dark
  themes/desert.xml              editor colour scheme, dark
  themes/desert-light.theme.json IDE chrome, light
  themes/desert-light.xml        editor colour scheme, light
MAPPING.md                       every vim group → IntelliJ key, and why
build.sh                         validate + package
install.sh                       install into local IDEs
```

The layout is the standard Gradle resource layout, so dropping in the
IntelliJ Platform Gradle Plugin later works without moving anything.

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
`desert-light.xml` — search highlights (which must stay light backgrounds with
dark text) and the ANSI console slots (where `black` must stay dark and
`white` must stay readable).

## Tweaking

`desert.theme.json` routes every UI colour through a named colour at the top of
the file, so most changes are one line. Two you may actually want:

- **Tan status bar.** desert.vim's signature is a `#c2bfa5` status line with
  black text. That's set to a dark bar with tan text by default, because IDE
  status-bar icons are drawn for dark backgrounds and disappear on tan. For the
  full vim look, set `desertStatusBarBg` to `#c2bfa5` and `desertStatusBarFg`
  to `#000000`.
- **TODO highlighting.** Faithfully orangered-on-yellow2, which is loud in a
  gutter-wide IDE. Soften `TODO_DEFAULT_ATTRIBUTES` in `desert.xml`.

After editing, re-run `./build.sh` and reinstall the zip.

## Troubleshooting

**Colours don't change no matter how often you reinstall.** You probably
imported `build/desert.icls` at some point *and* installed the plugin. Both
register a scheme called "Desert", and the imported one wins — so the plugin
installs correctly and is then ignored. `install.sh` warns when it spots this.
Fix it by quitting the IDE (config is rewritten on exit, so deleting it while
the IDE runs won't stick), removing the file, and restarting:

```sh
rm ~/Library/Application\ Support/JetBrains/<IDE>/colors/Desert.icls
```

Pick one delivery route or the other, not both.

**Comments render white.** Symptom of the same thing. The IDE rewrites
`baseAttributes="DEFAULT_COMMENT"` to `baseAttributes=""` when it saves an
imported scheme, and an empty inherit resolves to plain text. Every attribute
in `desert.xml` is set explicitly for this reason — never reintroduce
`baseAttributes`.

## Credit

Original colour scheme by Hans Fugal <hans@fugal.net>, distributed with vim.
This is an adaptation of that palette; see `MAPPING.md` for what was
transcribed and what had to be invented.
