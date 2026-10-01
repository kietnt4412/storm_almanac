import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import {
  ApiError,
  getBanners,
  getMeasures,
  getOdds,
  getPity,
  savePity,
  type Banner,
  type Measure,
  type Odds,
} from '../api/client';
import { ProfileGate } from '../profile';
import { reachOf, usePlannerStore } from '../store/plannerStore';
import { Ladder } from './PlanView';
import { ChanceCurve } from '../ui/ChanceCurve';
import { ChanceByDate, CopiesBar, PityDial } from '../ui/PullPictures';
import { formatUntil, useNow } from '../ui/time';
import { Explain } from '../ui/Explain';

/**
 * "Will I get her, and by when?" — C1's screen.
 *
 * <p><b>Three things only the reader knows, and nothing else is asked.</b> Their
 * pity counter, which is on their own pool screen and nowhere this site can
 * read; how far they get in the game's scored income, the same answer the plan
 * takes (ADR 0022); and how many days they are asking about. The balance comes
 * from the saved inventory and the rates from the banner, so neither is typed
 * twice.
 *
 * <p><b>One button, and it saves the counter first.</b> The counter is stored
 * because the game carries it from one pool to the next, and a reader who
 * comes back next week should find it where they left it. Saving it as part of
 * asking is what makes it stored without a second thing to press.
 */
export function Pulls() {
  return <ProfileGate>{(profile) => <PullPlanner profileId={profile.id} game={profile.game} />}</ProfileGate>;
}

function PullPlanner({ profileId, game }: { profileId: string; game: string }) {
  const banners = useQuery({ queryKey: ['banners', game], queryFn: () => getBanners(game) });
  const measures = useQuery({ queryKey: ['measures', game], queryFn: () => getMeasures(game), staleTime: Infinity });

  // Only a banner that is open and priced can be asked about; the rest are
  // refused by the server, and offering them would be offering a refusal.
  // The one open longest first: a pool closing in hours is still offered, but
  // opening on it answered "0% within 0 days" for a reader who had asked nothing.
  const askable = useMemo(
    () =>
      (banners.data?.banners ?? [])
        .filter((banner) => banner.open && banner.pullPrice !== null)
        .sort((a, b) => (closing(a) === closing(b) ? 0 : closing(b) > closing(a) ? 1 : -1)),
    [banners.data],
  );
  const now = useNow();
  const [bannerId, setBannerId] = useState<string | null>(null);
  const banner = askable.find((candidate) => candidate.id === bannerId) ?? askable[0] ?? null;

  if (banners.isPending) return <p className="muted">Loading the banners…</p>;
  if (banners.isError) return <p className="card">Could not load the banners: {banners.error.message}</p>;

  return (
    <div className="space-y-4">
      <header>
        <h1 className="text-xl font-semibold">Pulls</h1>
        <p className="muted text-sm">
          How likely you are to get the featured unit, from your own pity counter, what you hold and what
          the game pays you until then.
        </p>
      </header>

      {banner === null ? (
        <p className="card">No banner is open and priced right now, so there is nothing to work out.</p>
      ) : (
        <>
          {/*
            The open pools side by side, each with how long it has left, so the
            choice is made on what tells them apart — a pool closing in hours
            reads as one — rather than inside a dropdown that hides it.
          */}
          {askable.length > 1 && (
            <div className="grid gap-2 sm:grid-cols-2" role="group" aria-label="Banner">
              {askable.map((candidate) => {
                const left = candidate.closesAt ? new Date(candidate.closesAt).getTime() - now.getTime() : null;
                const soon = left !== null && left < 86_400_000;
                const here = candidate.id === banner.id;
                return (
                  <button
                    key={candidate.id}
                    type="button"
                    aria-pressed={here}
                    onClick={() => setBannerId(candidate.id)}
                    className={`card text-left transition ${here ? 'card-next' : ''}`}
                    style={here ? { borderWidth: 2 } : undefined}
                  >
                    <span className="block font-medium">{candidate.displayName}</span>
                    <span className="block text-sm" style={{ color: soon ? 'var(--signal)' : 'var(--muted)' }}>
                      {left === null ? 'No close date' : `Closes in ${formatUntil(left)}`}
                    </span>
                  </button>
                );
              })}
            </div>
          )}
          <Asker
            key={banner.id}
            named={askable.length <= 1}
            profileId={profileId}
            banner={banner}
            ladders={(measures.data?.measures ?? []).filter((ladder) => ladder.paysForPulls !== false)}
          />
        </>
      )}
    </div>
  );
}

function Asker({
  named,
  profileId,
  banner,
  ladders,
}: {
  /** Whether to say the banner's name; not when a card above already does. */
  named: boolean;
  profileId: string;
  banner: Banner;
  ladders: Measure[];
}) {
  const queryClient = useQueryClient();
  const pity = useQuery({
    queryKey: ['pity', profileId, banner.id],
    queryFn: () => getPity(profileId, banner.id),
  });

  const [pulls, setPulls] = useState(0);
  const [losses, setLosses] = useState(0);
  useEffect(() => {
    if (pity.data) {
      setPulls(pity.data.pullsSinceHit);
      setLosses(pity.data.consecutiveLosses);
    }
  }, [pity.data]);

  const [days, setDays] = useState(() => daysUntil(banner.closesAt) ?? 14);
  const [copies, setCopies] = useState(1);

  const reach = usePlannerStore((state) => reachOf(state, profileId));
  const setReach = usePlannerStore((state) => state.setReach);

  const run = useMutation<Odds, Error>({
    mutationFn: async () => {
      const stored = pity.data;
      if (!stored || stored.pullsSinceHit !== pulls || stored.consecutiveLosses !== losses) {
        const saved = await savePity(profileId, banner.id, pulls, losses);
        queryClient.setQueryData(['pity', profileId, banner.id], saved);
      }
      return getOdds(profileId, { banner: banner.id, days, copies, reach });
    },
  });

  const splits = banner.featuredChance < 1;

  return (
    // Two columns from a laptop's width: the questions on the left and the
    // answer beside them, held in view, so changing a question and reading what
    // it did are one glance rather than a scroll. One column on a phone.
    <form
      className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)] lg:items-start"
      onSubmit={(event) => {
        event.preventDefault();
        run.mutate();
      }}
    >
      <div className="space-y-4">
      <section className="card space-y-1">
        {named && <h2 className="font-medium">{banner.displayName}</h2>}
        <p className="muted text-sm">{describe(banner)}</p>
      </section>

      <section className="card space-y-3">
        <div>
          <h2 className="font-medium">Your pity</h2>
          <Explain lead="As your pool screen shows it.">
            It is saved, and every pool that carries the same counter reads it back.
          </Explain>
        </div>
        <div className="flex flex-wrap items-end gap-4">
          <PityDial pulls={pulls} hardAt={banner.hardAt} drawnFrom={banner.drawnFrom} />
          <div>
            <label className="label" htmlFor="since">
              Pulls since your last {banner.headline.label}
            </label>
            <input
              id="since"
              className="input count w-24"
              type="number"
              min={0}
              max={banner.hardAt}
              value={pulls}
              onChange={(event) => setPulls(Math.max(0, Number(event.target.value)))}
            />
            <span className="muted ml-2 text-sm">of {banner.drawnFrom ? `${banner.drawnFrom}–${banner.hardAt}` : banner.hardAt}</span>
          </div>
          {splits &&
            (banner.guaranteeAfterLoss === 1 ? (
              <label className="flex items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  checked={losses >= 1}
                  onChange={(event) => setLosses(event.target.checked ? 1 : 0)}
                />
                Your last {banner.headline.label} was not the featured unit
              </label>
            ) : (
              <div>
                <label className="label" htmlFor="losses">
                  {banner.headline.label} hits in a row that were not the featured unit
                </label>
                <input
                  id="losses"
                  className="input count w-20"
                  type="number"
                  min={0}
                  max={banner.guaranteeAfterLoss}
                  value={losses}
                  onChange={(event) => setLosses(Math.max(0, Number(event.target.value)))}
                />
              </div>
            ))}
        </div>
      </section>

      {ladders.length > 0 && (
        <section className="card space-y-3">
          <div>
            <h2 className="font-medium">How far do you get?</h2>
            <Explain lead="What the game pays towards pulls depends on what you finish.">
              Leave one alone and none of it is counted, so the answer is never better than the truth.
            </Explain>
          </div>
          {ladders.map((ladder) => (
            <Ladder
              key={ladder.measure}
              ladder={ladder}
              score={reach[ladder.measure] ?? 0}
              onPick={(score) => setReach(profileId, ladder.measure, score)}
            />
          ))}
        </section>
      )}

      <div className="card flex flex-wrap items-end gap-4">
        <div>
          <label className="label" htmlFor="days">
            Days from now
          </label>
          <input
            id="days"
            className="input count w-20"
            type="number"
            min={0}
            value={days}
            onChange={(event) => setDays(Math.max(0, Number(event.target.value)))}
          />
          {/* Zero is a real question — what you hold now — and on a pool closing today it is the only one. */}
          {days === 0 && <span className="muted ml-2 text-sm">what you hold now</span>}
        </div>
        <div>
          <label className="label" htmlFor="copies">
            Copies
          </label>
          <select
            id="copies"
            className="input"
            value={copies}
            onChange={(event) => setCopies(Number(event.target.value))}
          >
            {[1, 2, 3, 4, 5, 6].map((count) => (
              <option key={count} value={count}>
                {count}
              </option>
            ))}
          </select>
        </div>
        <button type="submit" className="btn" disabled={run.isPending || pity.isPending}>
          {run.isPending ? 'Working it out…' : 'Work it out'}
        </button>
      </div>
      </div>

      <div className="space-y-4 lg:sticky lg:top-24">
        {run.isError && (
          <div className="card">
            <p className="font-medium">
              {run.error instanceof ApiError && run.error.isUnanswerable ? 'There is no answer for this' : 'That did not work'}
            </p>
            <p className="muted mt-1 text-sm">{run.error.message}</p>
          </div>
        )}
        {run.data ? (
          <Answer odds={run.data} />
        ) : (
          !run.isError && (
            <p className="card muted hidden text-sm lg:block">
              Your chance shows here — set your pity and how far you get, then work it out.
            </p>
          )
        )}
      </div>
    </form>
  );
}

function Answer({ odds }: { odds: Odds }) {
  const { budget } = odds;
  const wanted = odds.copies === 1 ? 'the featured unit' : `${odds.copies} copies of the featured unit`;

  return (
    <section className="card-raised space-y-4" aria-live="polite">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-4xl font-bold leading-none" style={{ color: 'var(--brand)' }}>
            {percent(odds.chance)}
          </p>
          <p className="mt-1">
            chance of {wanted} within {plural(odds.days, 'day')}.
          </p>
        </div>
        <div className="grid grid-cols-3 gap-2 text-center">
          <Stat label="You can afford" value={count(budget.pulls)} />
          <Stat label="Average to first" value={count(Math.round(odds.expectedPulls))} />
          <Stat label="Certain by" value={count(odds.worstCasePulls)} />
        </div>
      </div>

      {/* Absent from a server a deploy behind the page; the answer stands without it. */}
      {/* By date where the server says it (C2.8), the question the page exists
          for; by pull count from a server a deploy behind. The answer stands without either. */}
      {odds.byDay && odds.byDay.length > 1 ? (
        <ChanceByDate byDay={odds.byDay} asked={odds.days} closesAt={odds.closesAt} />
      ) : (
        odds.curve && odds.curve.length > 1 && <ChanceCurve curve={odds.curve} afforded={budget.pulls} />
      )}
      {odds.byCopies && <CopiesBar byCopies={odds.byCopies} />}

      <ul className="space-y-1 text-sm">
        <li>
          <strong>{plural(budget.pulls, 'pull')}</strong> you can afford at {count(budget.perPull)}{' '}
          {budget.currencyName} each: {count(budget.held)} held
          {budget.accruing > 0 ? ` and ${count(budget.accruing)} to come` : ''}.
        </li>
        {budget.converted.map((converted) => (
          <li key={converted.item} className="muted">
            Includes {count(converted.held)} {converted.displayName} held
            {converted.accruing > 0 ? ` and ${count(converted.accruing)} to come` : ''}, counted as{' '}
            {budget.currencyName} because they exchange into it.
          </li>
        ))}
        <li>
          From your counter, <strong>{plural(odds.worstCasePulls, 'pull')}</strong> make it certain; the first
          copy takes about {Math.round(odds.expectedPulls)} on average.
        </li>
        {odds.cappedAtClose && odds.closesAt && (
          <li className="muted">
            The banner closes {when(odds.closesAt)}, so income is counted for {plural(odds.days, 'day')}, not{' '}
            {odds.daysAsked}.
          </li>
        )}
        {budget.uncounted.length > 0 && (
          <li className="muted">
            {plural(budget.uncounted.length, 'scored grant')} not counted. Say how far you get above to count
            them.
          </li>
        )}
      </ul>

      <p className="muted text-xs">
        Worked out exactly, not simulated, against sequence {odds.versionSequence} ({odds.versionLabel}). Your
        balance is your saved <Link to="/inventory">inventory</Link>.
      </p>
    </section>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="stat-tile">
      <span className="stat-label">{label}</span>
      <span className="stat-value text-xl">{value}</span>
    </div>
  );
}

// ── Wording ─────────────────────────────────────────────────────────────────

function describe(banner: Banner): string {
  const parts: string[] = [];
  if (banner.baseRate !== null) parts.push(`${(banner.baseRate * 100).toFixed(2)}% ${banner.headline.label} rate`);
  parts.push(
    banner.drawnFrom
      ? `certain somewhere between pull ${banner.drawnFrom} and ${banner.hardAt}`
      : `certain by pull ${banner.hardAt}`,
  );
  parts.push(
    banner.featuredChance >= 1
      ? 'every hit is the featured unit'
      : `${Math.round(banner.featuredChance * 100)}% featured, guaranteed after ${plural(banner.guaranteeAfterLoss, 'loss', 'losses')}`,
  );
  if (banner.pullPrice) parts.push(`${count(banner.pullPrice.perPull)} ${banner.pullPrice.currencyName} a pull`);
  if (banner.closesAt) parts.push(`closes ${when(banner.closesAt)}`);
  return parts.join(' · ');
}

export function percent(chance: number): string {
  if (chance >= 1) return '100%';
  if (chance <= 0) return '0%';
  if (chance < 0.001) return 'under 0.1%';
  if (chance > 0.999) return 'over 99.9%';
  return `${(chance * 100).toFixed(1)}%`;
}

function plural(quantity: number, one: string, many = `${one}s`): string {
  return `${count(quantity)} ${quantity === 1 ? one : many}`;
}

function count(quantity: number): string {
  return quantity.toLocaleString();
}

/** In UTC, which is the zone the game's own close is read in. */
function when(instant: string): string {
  return `${new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short', timeZone: 'UTC' }).format(new Date(instant))} UTC`;
}

function daysUntil(instant: string | null): number | null {
  if (!instant) return null;
  return Math.max(0, Math.floor((new Date(instant).getTime() - Date.now()) / 86_400_000));
}

/** When a pool closes, for ordering: one with no close date is open the longest. */
function closing(banner: Banner): number {
  return banner.closesAt ? new Date(banner.closesAt).getTime() : Number.POSITIVE_INFINITY;
}
