import { useEffect, useMemo, useRef, useState } from 'react';
import type { Odds, PullTables } from '../api/client';
import { Explain } from '../ui/Explain';
import { cumulative, gapChanceAllows, largestGap, startRolling } from '../ui/dice';
import { prefersMotion } from '../ui/motion';

/**
 * Roll it yourself (C2.21, agreed with the mockup 2026-10-02): the reader's
 * device rolls twenty thousand pull histories and the pile settles onto the
 * curve the server's exact chain drew.
 *
 * <p><b>The chain is still the answer; the dice are the proof.</b> Nothing this
 * panel shows is the reader's chance — that is the number above it, read off
 * the chain. The dice roll against the tables the same answer carried
 * (`model`), from the same counter, so no rule is written twice, and they draw
 * the wall per cycle where the chain integrates it out (ADR 0023).
 *
 * <p><b>The rain is the computation (F2).</b> Each history falls into its bin
 * as it comes back from the worker, slowly at first and then in a pour, so the
 * whole run takes a few seconds; only a handful are drawn falling at once, and
 * the rest land where they belong. Reduced motion gets the finished pile.
 * Off screen it waits.
 */
const TOTAL = 20_000;
const FALLING_PER_FRAME = 24;
const HEIGHT = 300;

interface Particle {
  pulls: number;
  x: number;
  y: number;
  vy: number;
  tx: number;
}

/** One run of the dice, mutable and outside React: a frame touches it sixty times a second. */
interface Run {
  tally: Int32Array;
  landed: number;
  queue: number[];
  next: number;
  spawned: number;
  frames: number;
  particles: Particle[];
  rolled: boolean;
  frame: number | null;
  stop: () => void;
}

export function RollIt({ odds }: { odds: Odds }) {
  if (!odds.model || !odds.curve || odds.curve.length < 2) return null;
  return <Roller odds={odds} tables={odds.model} curve={odds.curve} />;
}

function Roller({ odds, tables, curve }: { odds: Odds; tables: PullTables; curve: number[] }) {
  const worst = curve.length - 1;
  const canvas = useRef<HTMLCanvasElement>(null);
  const run = useRef<Run | null>(null);
  const visible = useRef(true);
  const [phase, setPhase] = useState<'idle' | 'rolling' | 'done'>('idle');
  const [landed, setLanded] = useState(0);
  const [dice, setDice] = useState<number[] | null>(null);
  const [seed, setSeed] = useState<number | null>(null);
  // Read first at the reader's own pulls; with none to spend, "within 0" says nothing, so at the average.
  const [at, setAt] = useState(() => Math.min(odds.budget.pulls > 0 ? odds.budget.pulls : Math.round(odds.expectedPulls * odds.copies), worst));
  const atNow = useRef(at);
  atNow.current = at;

  const bins = useMemo(() => binsOf(curve), [curve]);

  const paint = () => {
    const element = canvas.current;
    if (!element) return;
    draw(element, curve, bins, run.current, atNow.current, odds.copies);
  };

  // Sized to its box at the device's pixel ratio, and again when the box changes.
  useEffect(() => {
    const element = canvas.current;
    if (!element) return;
    const size = () => {
      const box = element.getBoundingClientRect();
      const ratio = window.devicePixelRatio || 1;
      element.width = Math.max(1, Math.round(box.width * ratio));
      element.height = Math.max(1, Math.round(HEIGHT * ratio));
      paint();
    };
    size();
    if (typeof ResizeObserver !== 'function') return;
    const observer = new ResizeObserver(size);
    observer.observe(element);
    return () => observer.disconnect();
  }, []);

  // Paused off screen, picked up where it was on the way back.
  useEffect(() => {
    const element = canvas.current;
    if (!element || typeof IntersectionObserver !== 'function') return;
    const observer = new IntersectionObserver((entries) => {
      visible.current = entries.some((entry) => entry.isIntersecting);
      const current = run.current;
      if (visible.current && current && current.frame === null && !finished(current)) schedule(current);
    });
    observer.observe(element);
    return () => observer.disconnect();
  }, []);

  useEffect(() => paint(), [at]);
  useEffect(() => () => stop(run.current), []);

  const report = (current: Run) => {
    setLanded(current.landed);
    setDice(cumulative(current.tally, current.landed));
  };

  const finished = (current: Run) => current.rolled && current.next >= current.queue.length && current.particles.length === 0;

  const land = (current: Run, pulls: number) => {
    current.tally[pulls] = (current.tally[pulls] ?? 0) + 1;
    current.landed++;
  };

  const schedule = (current: Run) => {
    current.frame = requestAnimationFrame(() => frame(current));
  };

  const frame = (current: Run) => {
    current.frame = null;
    const element = canvas.current;
    if (!element || run.current !== current) return;
    const ratio = element.width / Math.max(1, element.getBoundingClientRect().width || element.width);
    const plot = plotOf(element.width, element.height, ratio);

    // Slowly at first and then a pour: the rate grows with what has fallen.
    const due = Math.min(current.queue.length - current.next, 2 + Math.floor(current.spawned / 60));
    for (let index = 0; index < due; index++) {
      const pulls = current.queue[current.next++] ?? 1;
      current.spawned++;
      if (index >= FALLING_PER_FRAME) {
        land(current, pulls);
        continue;
      }
      const [left, right] = binSpan(bins, pulls, plot, worst);
      current.particles.push({
        pulls,
        x: plot.left + plot.width / 2 + (Math.random() - 0.5) * 60 * ratio,
        y: plot.top,
        vy: 0,
        tx: left + (right - left) * (0.2 + Math.random() * 0.6),
      });
    }
    const floor = plot.top + plot.height;
    current.particles = current.particles.filter((particle) => {
      particle.vy += 0.45 * ratio;
      particle.y += particle.vy;
      particle.x += (particle.tx - particle.x) * 0.12;
      if (particle.y < floor - pileHeight(current, bins, particle.pulls, plot)) return true;
      land(current, particle.pulls);
      return false;
    });

    draw(element, curve, bins, current, atNow.current, odds.copies);
    // The tiles every sixth frame: ten times a second is as fast as anyone reads.
    if (++current.frames % 6 === 0) report(current);
    if (finished(current)) {
      report(current);
      setPhase('done');
      return;
    }
    if (visible.current) schedule(current);
  };

  const roll = () => {
    stop(run.current);
    const fresh = Math.floor(Math.random() * 0x7fffffff);
    const animate = prefersMotion() && typeof requestAnimationFrame === 'function';
    const current: Run = {
      tally: new Int32Array(worst + 1),
      landed: 0,
      queue: [],
      next: 0,
      spawned: 0,
      frames: 0,
      particles: [],
      rolled: false,
      frame: null,
      stop: () => {},
    };
    run.current = current;
    setSeed(fresh);
    setPhase('rolling');
    setLanded(0);
    setDice(null);
    paint();

    current.stop = startRolling(
      {
        tables,
        start: { pullsSinceHit: odds.pity.pullsSinceHit, consecutiveLosses: odds.pity.consecutiveLosses },
        copies: odds.copies,
        ceiling: worst,
        count: TOTAL,
        seed: fresh,
      },
      (results) => {
        if (run.current !== current) return;
        if (animate) {
          for (const pulls of results) current.queue.push(pulls);
          if (current.frame === null && visible.current) schedule(current);
        } else {
          for (const pulls of results) land(current, pulls);
        }
      },
      () => {
        if (run.current !== current) return;
        current.rolled = true;
        if (!animate) {
          paint();
          report(current);
          setPhase('done');
        } else if (current.frame === null && visible.current) {
          schedule(current);
        }
      },
    );
  };

  const chain = curve[at] ?? 1;
  const yours = dice ? (dice[at] ?? 1) : null;
  const gap = dice ? largestGap(dice, curve) : null;
  const wanted = odds.copies === 1 ? 'the featured unit' : `copy ${odds.copies} of the featured unit`;

  return (
    <section className="card space-y-3" aria-labelledby="roll-it">
      <div>
        <h2 id="roll-it" className="font-medium">
          Roll it yourself
        </h2>
        <Explain lead={`Your device rolls ${count(TOTAL)} pull histories from your counter, and the pile settles onto the exact curve.`}>
          The chance above is worked out exactly, not simulated. These dice are the other road: they roll against the
          same rules the server sent, drawing each guarantee as the game does, and their line should land inside the
          band around the exact one. The band is two standard errors wide, and it narrows as the pile grows.
        </Explain>
      </div>

      <canvas
        ref={canvas}
        className="block w-full rounded-lg"
        style={{ height: HEIGHT, border: '1px solid var(--line)', background: 'color-mix(in srgb, var(--raised) 70%, transparent)' }}
        role="img"
        aria-label={`Histogram of pulls until ${wanted}, filling as histories are rolled, with the exact chance drawn over it.`}
      />

      <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
        <Tile label="Histories" value={count(landed)} />
        <Tile label={`Within ${at} — the chain`} value={percent(chain)} />
        <Tile label="— your dice" value={yours === null ? '—' : percent(yours)} />
        <Tile label="Largest gap" value={gap === null ? '—' : `${(gap * 100).toFixed(2)} pts`} />
      </div>

      <div className="flex flex-wrap items-center gap-4">
        <button type="button" className="btn" data-working={phase === 'rolling' || undefined} disabled={phase === 'rolling'} onClick={roll}>
          {phase === 'rolling' ? 'Rolling…' : phase === 'done' ? 'Roll again' : `Roll ${count(TOTAL)} histories`}
        </button>
        <label className="flex items-center gap-2 text-sm" htmlFor="roll-at">
          <span className="muted">Pulls</span>
          <input
            id="roll-at"
            type="range"
            min={0}
            max={worst}
            value={at}
            onChange={(event) => setAt(Number(event.target.value))}
          />
          <output htmlFor="roll-at" className="count w-10">
            {at}
          </output>
        </label>
        <span className="flex flex-wrap items-center gap-3 text-xs muted">
          <span><span className="roll-swatch" style={{ background: 'var(--brand)' }} /> your dice</span>
          <span><span className="roll-swatch" style={{ background: 'var(--ink)' }} /> the chain, exact</span>
        </span>
      </div>

      <p className="muted text-xs" aria-live="polite">
        {phase === 'done' && gap !== null && yours !== null
          ? `${count(landed)} histories · largest gap ${(gap * 100).toFixed(2)} points, where chance alone allows up to ${(gapChanceAllows(landed) * 100).toFixed(2)} · the chain says ${percent(chain)} within ${at} pulls, your dice say ${percent(yours)} · seed ${seed}, which rolls the same dice again.`
          : 'Rolled on this device, in a worker; nothing is sent anywhere.'}
      </p>
    </section>
  );
}

function Tile({ label, value }: { label: string; value: string }) {
  return (
    <div className="stat-tile">
      <span className="stat-label">{label}</span>
      <span className="stat-value text-xl">{value}</span>
    </div>
  );
}

function stop(current: Run | null) {
  if (!current) return;
  current.stop();
  if (current.frame !== null) cancelAnimationFrame(current.frame);
  current.frame = null;
}

// ── Drawing ─────────────────────────────────────────────────────────────────

interface Plot {
  left: number;
  top: number;
  width: number;
  height: number;
  ratio: number;
}

interface Bins {
  size: number;
  count: number;
  /** The chance a history ends in each bin, off the exact curve. */
  exact: number[];
  /** The tallest bin's expected share, so the pile's scale is fixed before it grows. */
  peak: number;
}

/** About thirty bins whatever the worst case, so 60 and 720 pulls both read as a shape. */
function binsOf(curve: number[]): Bins {
  const worst = curve.length - 1;
  const size = Math.max(1, Math.ceil(worst / 30));
  const count = Math.ceil(worst / size);
  const exact: number[] = [];
  for (let bin = 0; bin < count; bin++) {
    exact.push((curve[Math.min((bin + 1) * size, worst)] ?? 1) - (curve[bin * size] ?? 0));
  }
  return { size, count, exact, peak: Math.max(...exact, 1e-9) };
}

function plotOf(width: number, height: number, ratio: number): Plot {
  const left = 40 * ratio;
  const right = 12 * ratio;
  const top = 12 * ratio;
  const bottom = 40 * ratio;
  return { left, top, width: width - left - right, height: height - top - bottom, ratio };
}

function binOf(bins: Bins, pulls: number): number {
  return Math.min(bins.count - 1, Math.max(0, Math.ceil(pulls / bins.size) - 1));
}

function binSpan(bins: Bins, pulls: number, plot: Plot, worst: number): [number, number] {
  const bin = binOf(bins, pulls);
  const x = (at: number) => plot.left + (at / worst) * plot.width;
  return [x(bin * bins.size), x(Math.min((bin + 1) * bins.size, worst))];
}

function pileHeight(current: Run, bins: Bins, pulls: number, plot: Plot): number {
  const bin = binOf(bins, pulls);
  let inBin = 0;
  for (let at = bin * bins.size + 1; at <= (bin + 1) * bins.size && at < current.tally.length; at++) inBin += current.tally[at] ?? 0;
  return (inBin / (bins.peak * TOTAL * 1.1)) * plot.height;
}

function draw(element: HTMLCanvasElement, curve: number[], bins: Bins, current: Run | null, at: number, copies: number) {
  const context = typeof element.getContext === 'function' ? element.getContext('2d') : null;
  if (!context) return;
  const worst = curve.length - 1;
  const ratio = element.width / Math.max(1, element.getBoundingClientRect().width || element.width);
  const plot = plotOf(element.width, element.height, ratio);
  const x = (pulls: number) => plot.left + (pulls / worst) * plot.width;
  const y = (chance: number) => plot.top + (1 - chance) * plot.height;
  // A canvas takes no var(), so the page's colours are read off it.
  const style = getComputedStyle(element);
  const colour = (name: string, fallback: string) => style.getPropertyValue(name).trim() || fallback;
  const brand = colour('--brand', '#0369a1');
  const ink = colour('--ink', '#0b1628');
  const muted = colour('--muted', '#52627d');
  const line = colour('--line', '#d0d7e2');
  const violet = colour('--violet', '#6d28d9');

  context.clearRect(0, 0, element.width, element.height);
  context.font = `${11 * ratio}px system-ui, sans-serif`;

  // The scale: 0, 50 and 100% of histories done.
  context.strokeStyle = line;
  context.fillStyle = muted;
  context.lineWidth = ratio;
  context.textAlign = 'right';
  for (const level of [0, 0.5, 1]) {
    context.setLineDash(level === 0 ? [] : [3 * ratio, 4 * ratio]);
    context.beginPath();
    context.moveTo(plot.left, y(level));
    context.lineTo(plot.left + plot.width, y(level));
    context.stroke();
    context.fillText(`${level * 100}%`, plot.left - 6 * ratio, y(level) + 4 * ratio);
  }
  context.setLineDash([]);
  context.textAlign = 'center';
  const step = worst <= 60 ? 10 : worst <= 150 ? 20 : worst <= 300 ? 60 : 120;
  for (let pulls = 0; pulls <= worst; pulls += step) context.fillText(String(pulls), x(pulls), plot.top + plot.height + 16 * ratio);
  context.textAlign = 'right';
  context.fillText(copies === 1 ? 'pulls until the featured unit' : `pulls until copy ${copies}`, plot.left + plot.width, plot.top + plot.height + 34 * ratio);

  // The pile, on a scale fixed by where it will end, so bars grow in place.
  const landed = current?.landed ?? 0;
  if (current) {
    context.fillStyle = brand;
    context.globalAlpha = 0.3;
    for (let bin = 0; bin < bins.count; bin++) {
      let inBin = 0;
      for (let pulls = bin * bins.size + 1; pulls <= (bin + 1) * bins.size && pulls <= worst; pulls++) inBin += current.tally[pulls] ?? 0;
      const height = (inBin / (bins.peak * TOTAL * 1.1)) * plot.height;
      const left = x(bin * bins.size);
      const right = x(Math.min((bin + 1) * bins.size, worst));
      context.fillRect(left + ratio, plot.top + plot.height - height, Math.max(ratio, right - left - 2 * ratio), height);
    }
    context.globalAlpha = 1;
  }

  // The chain's curve, and two standard errors either side of it once there are dice to judge.
  if (landed > 0) {
    context.fillStyle = ink;
    context.globalAlpha = 0.12;
    context.beginPath();
    curve.forEach((chance, pulls) => {
      const error = 2 * Math.sqrt((chance * (1 - chance)) / landed);
      const point = [x(pulls), y(Math.min(1, chance + error))] as const;
      if (pulls === 0) context.moveTo(...point);
      else context.lineTo(...point);
    });
    for (let pulls = worst; pulls >= 0; pulls--) {
      const chance = curve[pulls] ?? 0;
      const error = 2 * Math.sqrt((chance * (1 - chance)) / landed);
      context.lineTo(x(pulls), y(Math.max(0, chance - error)));
    }
    context.closePath();
    context.fill();
    context.globalAlpha = 1;
  }
  context.strokeStyle = ink;
  context.lineWidth = 1.5 * ratio;
  context.beginPath();
  curve.forEach((chance, pulls) => (pulls === 0 ? context.moveTo(x(pulls), y(chance)) : context.lineTo(x(pulls), y(chance))));
  context.stroke();

  // The dice's own line.
  if (current && landed > 0) {
    context.strokeStyle = brand;
    context.lineWidth = 2.5 * ratio;
    context.beginPath();
    let running = 0;
    for (let pulls = 0; pulls <= worst; pulls++) {
      running += current.tally[pulls] ?? 0;
      const point = [x(pulls), y(running / landed)] as const;
      if (pulls === 0) context.moveTo(...point);
      else context.lineTo(...point);
    }
    context.stroke();
  }

  // Where the reader is reading.
  context.strokeStyle = violet;
  context.lineWidth = 1.5 * ratio;
  context.setLineDash([4 * ratio, 4 * ratio]);
  context.beginPath();
  context.moveTo(x(at), plot.top);
  context.lineTo(x(at), plot.top + plot.height);
  context.stroke();
  context.setLineDash([]);

  if (current) {
    context.fillStyle = brand;
    for (const particle of current.particles) {
      context.beginPath();
      context.arc(particle.x, particle.y, 2.2 * ratio, 0, Math.PI * 2);
      context.fill();
    }
  }
}

function percent(chance: number): string {
  if (chance >= 1) return '100%';
  if (chance <= 0) return '0%';
  return `${(chance * 100).toFixed(1)}%`;
}

function count(quantity: number): string {
  return quantity.toLocaleString();
}
