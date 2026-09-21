import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { StateChips, statesOfGraph } from './StateChips';

/**
 * The control that says where a reader stands, on the two rules that cost money
 * when they are wrong.
 *
 * <p>Both are about the planner's bill rather than about the screen. A state
 * that cannot be recorded is a track the reader is charged for from the
 * beginning (ADR 0027), and "no states" meaning something other than "not
 * owned" would make the roster hold a third thing the API has no word for.
 *
 * <p>What is deliberately not here is whether any of it can be used. The
 * multi-select this replaced passed every test it had and was unusable — 68
 * options four at a time, deselected by a ctrl-click nobody finds — and jsdom
 * computes no layout, so that is a person's job with a browser. These tests
 * cover the half a test can honestly cover.
 */
describe('recording where an entity stands', () => {
  it('offers only the states not already recorded', async () => {
    render(
      <StateChips
        subject="Helentine"
        states={['promote-6']}
        choices={['promote-6', 'level-80', 'evolve-s']}
        emptyWord="not owned"
        onChange={() => {}}
      />,
    );

    const dropdown = screen.getByLabelText('Add a current state for Helentine');
    const offered = [...dropdown.querySelectorAll('option')].map((option) => option.value);

    // The recorded one is a chip, not an option: offering it again invites a
    // duplicate the merge would have to define an answer for.
    expect(offered).toEqual(['', 'level-80', 'evolve-s']);
    expect(screen.getByRole('button', { name: 'Remove promote-6 from Helentine' })).toBeInTheDocument();
  });

  it('adds a second state without replacing the first', async () => {
    // A construct has a level, a rank and an evolution at once and the game ties
    // none of them together. This is the rule V13 exists for, asserted where a
    // reader would break it.
    const changed = vi.fn();
    render(
      <StateChips
        subject="Helentine"
        states={['promote-6']}
        choices={['promote-6', 'level-80']}
        emptyWord="not owned"
        onChange={changed}
      />,
    );

    await userEvent.selectOptions(screen.getByLabelText('Add a current state for Helentine'), 'level-80');

    expect(changed).toHaveBeenCalledWith(['promote-6', 'level-80']);
  });

  it('reports the last state coming off as "not owned", not as an empty roster entry', async () => {
    // Empty and absent are one meaning. The API refuses an empty list precisely
    // so that this stays true at both ends — sending [] would be a row saying
    // the entity is owned and nowhere, which is not a thing.
    const changed = vi.fn();
    render(
      <StateChips
        subject="Helentine"
        states={['promote-6']}
        choices={['promote-6']}
        emptyWord="not owned"
        onChange={changed}
      />,
    );

    await userEvent.click(screen.getByRole('button', { name: 'Remove promote-6 from Helentine' }));

    expect(changed).toHaveBeenCalledWith(null);
  });

  it('separates where you can stand from where you can aim', () => {
    // The base of a track is a fromState and never a toState, so a reader
    // sitting on it could not say so while these were one list. That was a real
    // defect and it hid behind a track with a single base, where the two lists
    // happen to answer the same.
    const graph = statesOfGraph([
      { fromState: 'promote-0', toState: 'promote-1' },
      { fromState: 'promote-1', toState: 'promote-2' },
    ]);

    expect(graph.targets).toEqual(['promote-1', 'promote-2']);
    expect(graph.starts).toEqual(['promote-0', 'promote-1', 'promote-2']);
  });
});
