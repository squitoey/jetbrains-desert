# desert.vim → JetBrains mapping

## Palette

desert.vim writes most colours as X11 names. These are the values vim resolves
them to, and the only colours this port uses.

| X11 name | Hex | Used in vim for |
| --- | --- | --- |
| `White` | `#ffffff` | Normal foreground |
| `grey20` | `#333333` | Normal background |
| `grey30` | `#4d4d4d` | Search / Folded / MatchParen background |
| `grey40` | `#666666` | Ignore |
| `grey50` | `#7f7f7f` | VertSplit, StatusLineNC foreground |
| — | `#c2bfa5` | StatusLine / VertSplit background (the sand bar) |
| `khaki` | `#f0e68c` | Statement, Cursor fg, Visual fg, IncSearch bg |
| `darkkhaki` | `#bdb76b` | Type |
| `navajowhite` | `#ffdead` | Special |
| `tan` | `#d2b48c` | FoldColumn |
| `gold` | `#ffd700` | Folded |
| `goldenrod` | `#daa520` | ModeMsg |
| `indianred` | `#cd5c5c` | Cursor bg, Title, PreProc |
| `salmon` | `#fa8072` | WarningMsg |
| — | `#ffa0a0` | Constant |
| `orangered` | `#ff4500` | Todo fg |
| `yellow2` | `#eeee00` | Todo bg |
| `olivedrab` | `#6b8e23` | Visual bg |
| `palegreen` | `#98fb98` | Identifier |
| `yellowgreen` | `#9acd32` | SpecialKey |
| `springgreen` | `#00ff7f` | Question |
| `SeaGreen` | `#2e8b57` | MoreMsg |
| `SkyBlue` | `#87ceeb` | Comment |
| `LightBlue` | `#add8e6` | NonText |
| `slategrey` | `#708090` | IncSearch fg |
| — | `#dfffdf` | Search fg, MatchParen fg |

## Direct transcriptions

| vim group | IntelliJ key |
| --- | --- |
| `Normal` | `TEXT` |
| `Cursor` | `CARET_COLOR` |
| `Visual` | `SELECTION_BACKGROUND` / `SELECTION_FOREGROUND` |
| `Comment` | `DEFAULT_COMMENT`, `..._LINE_COMMENT`, `..._BLOCK_COMMENT`, `..._DOC_COMMENT` |
| `Constant` | `DEFAULT_STRING`, `DEFAULT_NUMBER`, `DEFAULT_CONSTANT` |
| `Statement` | `DEFAULT_KEYWORD`, `DEFAULT_OPERATION_SIGN`, `DEFAULT_LABEL` |
| `Type` | `DEFAULT_CLASS_NAME`, `DEFAULT_CLASS_REFERENCE`, `DEFAULT_INTERFACE_NAME` |
| `Identifier` | `DEFAULT_FUNCTION_DECLARATION/CALL`, `DEFAULT_INSTANCE_METHOD`, `DEFAULT_STATIC_METHOD` |
| `PreProc` | `DEFAULT_METADATA` (annotations, decorators, directives) |
| `Special` | `DEFAULT_VALID_STRING_ESCAPE`, `DEFAULT_ENTITY`, `DEFAULT_DOC_COMMENT_TAG` |
| `Todo` | `TODO_DEFAULT_ATTRIBUTES` |
| `Ignore` | `NOT_USED_ELEMENT_ATTRIBUTES`, `DEPRECATED_ATTRIBUTES`, `INDENT_GUIDE` |
| `Search` | `TEXT_SEARCH_RESULT_ATTRIBUTES` |
| `IncSearch` | `SEARCH_RESULT_ATTRIBUTES` (the *current* match, as in vim) |
| `MatchParen` | `MATCHED_BRACE_ATTRIBUTES` |
| `Folded` | `FOLDED_TEXT_ATTRIBUTES` |
| `SpecialKey` | `WHITESPACES` (both only show when the feature is enabled) |
| `NonText` | `SOFT_WRAP_SIGN_COLOR` |
| `StatusLine` | `desertSand` — tab underlines, focus rings, progress bars |

Several mappings follow vim's own `:hi link` chains rather than the group name:
`String`, `Number`, `Boolean` and `Float` all link to `Constant`; `Conditional`,
`Repeat`, `Operator` and `Keyword` link to `Statement`; `Function` links to
`Identifier`; `StorageClass` and `Typedef` link to `Type`. Markup follows
`html.vim`: `htmlTagName → Statement` (khaki), `htmlArg → Type` (dark khaki),
`htmlTag → Function` (pale green).

## Deliberate departures

**Plain identifiers stay white.** `Identifier` is pale green in vim, but vim's
syntax files apply it almost exclusively to function names — ordinary locals and
parameters go unhighlighted. Colouring every IntelliJ `DEFAULT_LOCAL_VARIABLE`
pale green would make files look nothing like vim, so functions get pale green
and plain identifiers stay `Normal`. Fields get `tan` as a middle ground.

**Inspection severities.** desert.vim defines `Error`, `WarningMsg` and friends
only for `cterm`, as ANSI indices with no GUI equivalent. Filled from the desert
palette: error `orangered`, warning `goldenrod`, weak warning `darkkhaki`, typo
`olivedrab`, info `SeaGreen`.

**Diff and VCS.** Same situation — `DiffAdd`/`DiffChange`/`DiffDelete`/`DiffText`
are `cterm`-only. Backgrounds are desert hues darkened to sit on `grey20`:
added `#3f4a30`, modified `#35435a`, deleted `#4a3634`, conflict `#55432c`.

**Console ANSI palette.** No vim equivalent. Filled from the palette, with the
`white` slot set to the sand `#c2bfa5` so plain terminal output reads warm
rather than stark. `blue`, `magenta` and `cyan` (`#6c8fa8`, `#c98f8f`,
`#7ca8a8`) are the only invented colours in the port — desert has no true
magenta or cyan.

**Links.** vim has no hyperlink concept. The editor's link colours are matched
to the IDE chrome's `Link.*` keys so the two never diverge: `SkyBlue` active,
`LightBlue` followed, `grey50` inactive - all underlined. Comments are `SkyBlue`
as well, so within a comment the underline, not the colour, marks the URL.
Changing one side means changing both: `HYPERLINK_ATTRIBUTES` in `Desert.xml`
and `Link.activeForeground` in `Desert.theme.json`.

**Caret row.** vim leaves `CursorLine` unset. `#3a3a3a` — one step off the
background, since `grey30` is already spoken for by Search and MatchParen.

**Line numbers.** `LineNr` is `cterm`-only (`ctermfg=3`). `grey50` for the
gutter, `khaki` for the caret row's number.

**Status bar.** Dark with sand text rather than vim's sand bar with black text;
IDE status-bar icons are drawn for dark backgrounds. See README for the swap.

## Light mode

`DesertLight.xml` and `DesertLight.theme.json` are derived from the dark
pair, not from vim — desert.vim has no light background variant. Each hue is
its dark sibling taken to roughly 30–40% lightness, hue and role unchanged:

| Role | Dark | Light |
| --- | --- | --- |
| `Normal` foreground | `#ffffff` | `#2b2a22` |
| `Normal` background | `#333333` grey20 | `#f2f0e6` parchment |
| `Comment` | `#87ceeb` SkyBlue | `#1c7099` |
| `Constant` | `#ffa0a0` | `#a63030` |
| `Statement` | `#f0e68c` khaki | `#8a7d0f` |
| `Type` | `#bdb76b` darkkhaki | `#6f6a1e` |
| `Identifier` | `#98fb98` palegreen | `#22701f` |
| `Special` | `#ffdead` navajowhite | `#9a6a1b` |
| `PreProc` | `#cd5c5c` indianred | `#93372c` |
| sand accent | `#c2bfa5` | `#8a8562` |

Three groups resist a plain hue map and are overridden explicitly:

- **Search highlights.** `IncSearch` is khaki *background*; darkening it gives
  dark text on a dark ground. Light mode keeps `#f0e68c` and `#ffd700` as
  backgrounds and darkens only the text.
- **ANSI console slots.** These are named after colours, not roles — `black`
  must stay dark and `white` must stay readable, so the pair inverts rather
  than shifting.
- **`Visual`.** vim paints selected text one colour. Dark uses khaki on
  olivedrab; light uses dark khaki `#4a4410` on a pale olive wash `#ccd5b0`.

`./gradlew checkThemeParity` fails if the two schemes stop defining the
same key set; `buildPlugin` and `verifyPlugin` both depend on it.
