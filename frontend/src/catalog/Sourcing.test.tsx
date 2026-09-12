import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { Provenance, Sourcing as SourcingRecord } from '../api/client';
import { Sourcing, merge } from './Sourcing';

/**
 * The first tests this frontend has ever had, on the thing they are most needed
 * for.
 *
 * ADR 0015 says the shipped product carries only data this project sourced
 * itself, and this component is where that stops being a sentence in a README:
 * it is the only place a reader is told which kind of claim a number is. A
 * component that renders the wrong reassuring string is a worse failure than one
 * that crashes, because nobody will report it.
 */
describe('where the numbers came from', () => {
  it('names the reading, the day it was made, and how somebody could check it', () => {
    render(<Sourcing sourcing={sourcing(read)} attribution="Sourced by Storm Almanac." />);

    expect(screen.getByText('Read off a screen in the game')).toBeInTheDocument();
    expect(screen.getByText(/read 2026-09-11/)).toBeInTheDocument();
    expect(screen.getByText(/Chapter 3 stage screen/)).toBeInTheDocument();
    // The credit line is still there. It is a different claim from the sourcing
    // and losing it would be a licence problem, not a cosmetic one.
    expect(screen.getByText('Sourced by Storm Almanac.')).toBeInTheDocument();
  });

  it('says plainly when a number is not this project’s own reading', () => {
    // The publish gate refuses second-hand data unless somebody says out loud
    // that it is not being shipped. Once it is published under that exception
    // this is the only place a reader finds out, so "the origin string is on the
    // page somewhere" is not enough — it has to say so in words.
    render(<Sourcing sourcing={sourcing(borrowed)} attribution="Adapted." />);

    expect(screen.getByText("Somebody else's data, not ours")).toBeInTheDocument();
    expect(screen.getByText(/not to be trusted\s+on its own/)).toBeInTheDocument();
  });

  it('does not warn when every reading is ours', () => {
    render(<Sourcing sourcing={sourcing(read)} attribution="Sourced by Storm Almanac." />);

    expect(screen.queryByText(/not to be trusted/)).not.toBeInTheDocument();
  });

  it('renders silence as silence rather than as nothing', () => {
    // A version published before ADR 0016 carries no records at all. An empty
    // list rendered as an empty space reads as approval, which is the one
    // reading this whole feature exists to prevent — and it is the state most of
    // this repository's data is actually in.
    render(<Sourcing sourcing={{ sources: [], facts: {} }} attribution="Adapted." />);

    expect(screen.getByText(/Nobody recorded where these numbers were read/)).toBeInTheDocument();
  });

  it('renders when the server is older than this bundle and does not send the field at all', () => {
    // Found by a browser, not by a typechecker: the wire type says `sourcing` is
    // always there and a deployed server that predates it disagrees. The
    // frontend and the backend are separate hosts (D1), so every release has a
    // window where exactly this is true — and destructuring an absent object
    // throws before React has anything to paint, which turned a missing footer
    // into a blank page.
    render(
      <Sourcing sourcing={undefined} attribution="Sourced by Storm Almanac." />,
    );

    expect(screen.getByText(/Nobody recorded where these numbers were read/)).toBeInTheDocument();
    expect(screen.getByText('Sourced by Storm Almanac.')).toBeInTheDocument();
  });

  it('calls an origin it has never heard of unrecognised, not fine', () => {
    // A server newer than this bundle. Guessing in the reassuring direction is
    // the expensive mistake; guessing in the other direction costs a confusing
    // line on one page.
    render(
      <Sourcing sourcing={sourcing({ ...read, origin: 'DIVINED' })} attribution="Adapted." />,
    );

    expect(screen.getByText(/Unrecognised source \(DIVINED\)/)).toBeInTheDocument();
  });

  it('shows both readings of a page that asked two questions, and each of them once', () => {
    // A character page asks for the entity and for its upgrade table, which are
    // two readings that per-fact provenance exists to let a bundle distinguish.
    const merged = merge(sourcing(read), sourcing(disclosed), sourcing(read));

    expect(merged.sources.map((source) => source.id)).toEqual(['stage-screens', 'rules-screen']);
    expect(merged.facts).toEqual({ 'entity:sotheby': 'stage-screens', 'banner:debut': 'rules-screen' });
  });

  it('survives a response that has not arrived yet', () => {
    // The upgrade query resolves after the entity one, so the page renders at
    // least once with half its sourcing missing.
    expect(merge(sourcing(read), undefined).sources).toHaveLength(1);
  });
});

const read: Provenance = {
  id: 'stage-screens',
  origin: 'OBSERVED_IN_GAME',
  firstHand: true,
  detail: 'Chapter 3 stage screen, Global, patch 3.5.',
  observedOn: '2026-09-11',
};

const disclosed: Provenance = {
  id: 'rules-screen',
  origin: 'PUBLISHER_DISCLOSURE',
  firstHand: true,
  detail: 'The in-client summon rules screen.',
  observedOn: '2026-09-10',
};

const borrowed: Provenance = {
  id: 'elsewhere',
  origin: 'THIRD_PARTY',
  firstHand: false,
  detail: "Somebody else's numbers, retyped.",
  observedOn: '2026-09-09',
};

/** One record, pointed at by the one fact a test page is about. */
function sourcing(source: Provenance): SourcingRecord {
  const fact = source.origin === 'PUBLISHER_DISCLOSURE' ? 'banner:debut' : 'entity:sotheby';
  return { sources: [source], facts: { [fact]: source.id } };
}
