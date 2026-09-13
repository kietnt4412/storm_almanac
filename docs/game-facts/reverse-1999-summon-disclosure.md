# Reverse: 1999 — the summon rules, read off the client

**The first first-hand reading in this repository, and the live half of
[Q4](../../TRACKER.md#open-questions).** Recorded 2026-09-13 (twentieth session).

## Provenance, stated first because it is the whole point

| | |
|---|---|
| **Origin** | `PUBLISHER_DISCLOSURE` — the banner's own Details panel |
| **Client** | English-language client, i.e. Global. The screen names no region |
| **Patch** | **Not shown on the screen**, so not recorded. "3.7" was said in chat and came from outside it |
| **Banner** | *A Newly Hatched Chapter*, an Anniversary Limited Banner |
| **Read** | 2026-09-13. The maintainer took screenshots of every section of the rules. A Claude Code session transcribed the numbers below from those screenshots. **No screenshot is committed**: game assets are not allowed in this repository |
| **Open and close dates** | **Not on these screens, so not recorded.** An earlier message carried dates, but it could not be traced to the screen, so they are left out |

**What does not count, and was offered along the way.** A second answer quoting
a wiki, two videos and two guide sites was relayed the same day. It agrees
with everything below, and none of it is used, because ADR 0015 says a fact
enters because someone read it in the game. The screenshots are what closed it.

## What the screen states

Numbers only. The rules text is the publisher's, so it is paraphrased here rather
than reproduced.

**Rates.** Basic rates: 6★ 1.5%, 5★ 8.5%, 4★ 40%, 3★ 45%, 2★ 5%, which sum to
100%. **Overall 6★ rate including the guarantee: 2.36%.**

**Pity.** After 60 summons in a row without a 6★, the next summon's 6★ rate goes
from 1.5% to 4%. Each further summon without a 6★ adds 2.5%. Every 70 summons
guarantees a 6★. Obtaining a 6★ resets the rate to 1.5%.

**Rate-up.** A 6★ is the featured character (Rhiannon) half the time. After a 6★
that is not her, the next 6★ is guaranteed to be her. A 5★ is one of the two
featured 5★ characters (Ulu, Avgust) half the time, split equally between them.

**Floors.** The first 10 summons on this banner guarantee at least one 5★ or
higher. Every 10 summons guarantee at least one 4★ or higher.

**Scope.** The banner's guarantee count is independent: it is not shared with
other banners of its kind, and it is cleared when the event ends rather than
carried over.

**Cassettes of the Lost.** Every summon grants one, so a ten-pull grants ten. They
are exchanged in the Limited Shop for Rhiannon and for growth materials, and they
expire when the shop closes.

**The Limited Shop**, from a second screenshot the same day: **Rhiannon costs 200
Cassettes of the Lost.** Three growth items (Clawed Pendulum, Goose Neck, Golden
Beetle) cost 12 each. Every entry showed "11d left" on 2026-09-13. **No purchase
limit is shown on that view.** The maintainer reports from the game that she can
be bought **without limit until she reaches Portrait 5**. That is recorded as
their in-game observation. It is not on a screenshot.

**Portraits**, from a screenshot of her Portrait screen the same day: the portrait
levels run to **Lv. 5**, and raising one costs one of her portrait items (shown as
0/1). That agrees with the duplicate rule above, where copies 2–6 each give one
Artifice and copy 7 onwards gives none. So **six copies is the most that changes
anything**: the original, then five portraits.

**Duplicates**, whatever the source. The 2nd to 6th copy converts to one of the
character's Artifice plus a fixed amount of currency. The 7th copy onwards converts
to a larger amount of currency and no Artifice.

| Rarity | 2nd–6th copy | 7th copy onwards |
|---|---|---|
| 6★ | 1 Artifice + 12 Albums of the Lost | 28 Albums of the Lost + 10 Pneuma Film Developers |
| 5★ | 1 Artifice + 3 Albums of the Lost | 7 Albums of the Lost |
| 4★ | 1 Artifice + 8 Tracks of the Lost | 12 Tracks of the Lost |
| 3★ | 1 Artifice + 4 Tracks of the Lost | 7 Tracks of the Lost |
| 2★ | 1 Artifice + 3 Tracks of the Lost | 5 Tracks of the Lost |

## What it confirmed, what it contradicted, and what it found

**Confirmed: the pity curve, twice.** The screen states the curve the fixtures
were already using: 60, then 4%, then +2.5%, with a wall at 70. It also states
**a number the curve was not written from**. The 2.36% overall rate is one 6★
per expected wait, and the curve's expected wait is 42.3869 pulls, which is
2.3592%. A curve starting one pull earlier prints 2.38%, one pull later prints
2.34%, and dropping the rise entirely prints 2.30%. So the match rules out
the near misses. It also settles an ambiguity in the text: "+2.5%" means
percentage points, not a relative increase. `PublishedRatesTest.Disclosed`
pins both.

**Contradicted: the featured rule.** The second-hand fixture `reverseDebut` hands
the featured character over with every 6★. This banner splits it 50/50 with a
guarantee after one miss. So on this banner **70 pulls is certain to give a 6★
but gives her only 65.78% of the time**, the no-shop worst case is 140 pulls, and
the average wait for her is 63.58 pulls. `reverseDebut` is kept, and its
javadoc now says it is contradicted. No first-hand reading of a banner that
hands her over outright exists.

**Found: a second way to get her, which no engine models, and it does not bind
for one copy.** The cassettes are a guaranteed exchange that has nothing to do
with luck. One cassette per summon means cassettes never outnumber pulls, and her
price is 200 against a no-shop worst case of 140, **so for one copy the pulls
always deliver her before the shop could**. The engines' one-copy answers are exact
as they stand. **For two or more copies the shop does bind**, because the two roads
add up. The smallest pull count that guarantees *k* copies in the worst case,
using the most pull copies that many pulls can be kept from delivering, plus one
purchase per 200 cassettes:

| Copies | Portrait reached | Pulls alone | With the shop |
|---|---|---|---|
| 1 | P0 | 140 | 140 |
| 2 | P1 | 280 | **200** |
| 3 | P2 | 420 | **280** |
| 4 | P3 | 560 | **400** |
| 5 | P4 | 700 | **420** |
| 6 | P5 | 840 | **560** |

The shop column assumes no purchase limit, and the maintainer's report is that
there is none until Portrait 5, which is exactly where the table stops. So the
column holds for every goal that means anything. **At six copies the engines
alone say 840 pulls and the truth is 560.** This is
[ADR 0018](../adr/0018-the-gacha-engines-answer-one-question-about-one-rarity.md)'s
scope meeting the game. Nothing in `BannerModel` can express it yet, and it
belongs with **N28**, which is already about what a pull costs and what it is
worth.

**Found: a one-off floor.** "The first 10 summons guarantee a 5★" happens once,
and `Floor` can only express "every N". It is below the headline rarity, so it
changes no answer the engines give, and the fixture leaves it out instead of
writing it as a recurring rule.

**Left open:**
- **A screenshot of the purchase limit**, if the game shows one anywhere, to
  move it from a report to a reading. It changes no number in the table.
- **Whether "guarantee count" includes a guarantee earned by missing the 50/50,**
  or only the pull count. The screen's wording does not say.
- **Every other banner.** The beginner banner and both Gray Raven games remain
  second-hand.
