import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { TrackPicker } from './TrackPicker';
import { sectionsOf, statesOfGraph, tracksOfGraph } from './tracks';

/**
 * Recording where an entity stands, on the rules that cost money when they are
 * wrong: a track that cannot be answered is charged from its base (ADR 0027),
 * and an answer on one track must never erase another's.
 *
 * <p>What is not here is whether it reads. The two editors before this passed
 * their tests and could not be used; that is a browser's job.
 */

// The shape of a Punishing: Gray Raven construct's graph, cut down: two plain
// ladders, a grade, and a skill that starts locked.
const STEPS = [
  { fromState: 'promote-0', toState: 'promote-1' },
  { fromState: 'promote-1', toState: 'promote-2' },
  { fromState: 'level-1', toState: 'level-2' },
  { fromState: 'level-2', toState: 'level-10' },
  { fromState: 'evolve-s', toState: 'evolve-ss' },
  { fromState: 'leader-skill-locked', toState: 'leader-skill-1' },
  // One step at two prices is still one link (ADR 0021).
  { fromState: 'level-1', toState: 'level-2' },
];

describe('an upgrade graph read as tracks', () => {
  it('splits into the pieces the steps connect, each from its base, in catalog order', () => {
    const tracks = tracksOfGraph(STEPS);

    expect(tracks.map((track) => track.states.map((state) => state.state))).toEqual([
      ['promote-0', 'promote-1', 'promote-2'],
      ['level-1', 'level-2', 'level-10'],
      ['evolve-s', 'evolve-ss'],
      ['leader-skill-locked', 'leader-skill-1'],
    ]);
  });

  it('names a track by the words its states share, and each state by the rest', () => {
    const tracks = tracksOfGraph(STEPS);

    expect(tracks.map((track) => track.name)).toEqual(['Promote', 'Level', 'Evolve', 'Leader skill']);
    expect(tracks[1]!.states.map((state) => state.label)).toEqual(['1', '2', '10']);
    expect(tracks[2]!.states.map((state) => state.label)).toEqual(['S', 'SS']);
    expect(tracks[3]!.states.map((state) => state.label)).toEqual(['Locked', '1']);
  });

  it('falls back to the whole id when the states share no word', () => {
    const [track] = tracksOfGraph([{ fromState: 'dormant', toState: 'awakened' }]);

    expect(track!.name).toBe('Dormant');
    expect(track!.states.map((state) => state.label)).toEqual(['Dormant', 'Awakened']);
  });

  it('separates where you can stand from where you can aim', () => {
    // The base of a track is a fromState and never a toState, so a reader
    // sitting on it could not say so while these were one list.
    const graph = statesOfGraph([
      { fromState: 'promote-0', toState: 'promote-1' },
      { fromState: 'promote-1', toState: 'promote-2' },
    ]);

    expect(graph.targets).toEqual(['promote-1', 'promote-2']);
    expect(graph.starts).toEqual(['promote-0', 'promote-1', 'promote-2']);
  });
});

describe('the game\'s own words, where the bundle has them (ADR 0032)', () => {
  // Cut down from sequence 11: a rank that names its states, a skill with a tag,
  // and the leader, whose step arrives before the skills' and whose heading
  // comes after theirs on the game's screen.
  const NAMED = [
    { fromState: 'promote-0', toState: 'promote-1', fromName: 'Private ★1', toName: 'Sergeant ★1', section: 'Growth' },
    { fromState: 'promote-1', toState: 'promote-2', toName: 'Sergeant ★2', section: 'Growth' },
    { fromState: 'leader-skill-locked', toState: 'leader-skill-1', section: 'Common Effect', tag: 'Leader' },
    { fromState: 'withering-spiral-1', toState: 'withering-spiral-2', section: 'Basic Skill', tag: 'Yellow Orb' },
    { fromState: 'weapon-1', toState: 'weapon-2' },
  ];
  const ORDER = ['Growth', 'Basic Skill', 'Common Effect'];

  it('names a state as the game does, and guesses only where the bundle is silent', () => {
    const [rank, , skill] = tracksOfGraph(NAMED);

    expect(rank!.states.map((state) => state.label)).toEqual(['Private ★1', 'Sergeant ★1', 'Sergeant ★2']);
    expect(skill!.states.map((state) => state.label)).toEqual(['1', '2']);
    expect(skill).toMatchObject({ section: 'Basic Skill', tag: 'Yellow Orb', name: 'Withering spiral' });
  });

  it('puts the headings in the game\'s order, not the order the steps came in, and the unplaced last', () => {
    const sections = sectionsOf(tracksOfGraph(NAMED), ORDER);

    expect(sections.map((section) => section.name)).toEqual(['Growth', 'Basic Skill', 'Common Effect', undefined]);
    expect(sections[3]!.tracks.map((track) => track.name)).toEqual(['Weapon']);
  });

  it('shows each heading, and leads a skill with its tag and its name small beside it', () => {
    render(
      <TrackPicker subject="Selena" tracks={tracksOfGraph(NAMED)} order={ORDER} states={[]} onChange={() => {}} />,
    );

    expect(screen.getAllByRole('heading').map((heading) => heading.textContent)).toEqual([
      'Growth',
      'Basic Skill',
      'Common Effect',
    ]);
    const skill = screen.getByLabelText('Yellow Orb, Withering spiral for Selena');
    expect(skill.closest('label')).toHaveTextContent('Yellow OrbWithering spiral');
    expect(screen.getByLabelText('Promote for Selena')).toHaveDisplayValue('Private ★1');
  });
});

describe('recording where an entity stands', () => {
  const tracks = tracksOfGraph(STEPS);

  it('shows the furthest recorded state per track, and the base where nothing is', () => {
    render(
      <TrackPicker subject="Selena" tracks={tracks} states={['promote-0', 'promote-1']} onChange={() => {}} />,
    );

    expect(screen.getByLabelText('Promote for Selena')).toHaveValue('promote-1');
    expect(screen.getByLabelText('Level for Selena')).toHaveValue('level-1');
  });

  it('answers one track without touching another', async () => {
    const changed = vi.fn();
    render(
      <TrackPicker subject="Selena" tracks={tracks} states={['promote-0', 'promote-1', 'level-10']} onChange={changed} />,
    );

    await userEvent.selectOptions(screen.getByLabelText('Promote for Selena'), 'promote-2');

    // Both of the old promote answers go; level stays.
    expect(changed).toHaveBeenCalledWith(['level-10', 'promote-2']);
  });

  it('records the base when it is chosen, so "owned and untouched" is an answer', async () => {
    const changed = vi.fn();
    render(<TrackPicker subject="Selena" tracks={tracks} states={['evolve-ss']} onChange={changed} />);

    await userEvent.selectOptions(screen.getByLabelText('Evolve for Selena'), 'evolve-s');

    expect(changed).toHaveBeenCalledWith(['evolve-s']);
  });

  it('shows only the tracks asked for, and still counts the others as tracks', async () => {
    // A goal row shows its own track. The first browser run of this showed the
    // reader's promote and level answers there as "on no track".
    const changed = vi.fn();
    render(
      <TrackPicker
        subject="Selena"
        tracks={tracks}
        only={[tracks[2]!]}
        states={['promote-1', 'level-10']}
        onChange={changed}
      />,
    );

    expect(screen.getAllByRole('combobox')).toHaveLength(1);
    expect(screen.queryByText(/on no track/)).not.toBeInTheDocument();
    await userEvent.selectOptions(screen.getByLabelText('Evolve for Selena'), 'evolve-ss');

    expect(changed).toHaveBeenCalledWith(['promote-1', 'level-10', 'evolve-ss']);
  });

  it('keeps and names a recorded state that is on no published track', async () => {
    const changed = vi.fn();
    render(<TrackPicker subject="Selena" tracks={tracks} states={['insight-3']} onChange={changed} />);

    expect(screen.getByText(/on no track this patch publishes: insight-3/)).toBeInTheDocument();
    await userEvent.selectOptions(screen.getByLabelText('Level for Selena'), 'level-2');

    expect(changed).toHaveBeenCalledWith(['insight-3', 'level-2']);
  });
});
