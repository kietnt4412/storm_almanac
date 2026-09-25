import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { NextStep, StepBar } from './Steps';

/**
 * S2's sibling from the maintainer's rehearsal (S3, 2026-09-25): a reader who
 * saved their goals had no way on, and one who forgot what came next had to go
 * home to read it.
 */
describe('the way through the four steps', () => {
  it('shows every step in order and marks the one the reader is on', () => {
    render(
      <MemoryRouter initialEntries={['/roster']}>
        <StepBar />
      </MemoryRouter>,
    );

    const steps = screen.getAllByRole('link');
    expect(steps.map((step) => step.textContent)).toEqual([
      '1What you own',
      '2Where they stand',
      '3What you want',
      '4The plan',
    ]);
    expect(screen.getByRole('link', { current: 'page' })).toHaveTextContent('Where they stand');
  });

  it('leads from each step to the next, and back to the one before', () => {
    render(
      <MemoryRouter>
        <NextStep from="/roster" />
      </MemoryRouter>,
    );

    expect(screen.getByRole('link', { name: 'Next: What you want →' })).toHaveAttribute('href', '/goals');
    expect(screen.getByRole('link', { name: '← What you own' })).toHaveAttribute('href', '/inventory');
  });

  it('saves before leaving when there is a draft, and only then goes on', async () => {
    const save = vi.fn(() => Promise.resolve());
    renderAt('/goals', <NextStep from="/goals" before={save} label="Save and get the plan →" />);

    await userEvent.click(screen.getByRole('button', { name: 'Save and get the plan →' }));

    expect(save).toHaveBeenCalledOnce();
    expect(await screen.findByText('the plan page')).toBeInTheDocument();
  });

  it('stays when the save fails, so the reader can read why', async () => {
    const save = vi.fn(() => Promise.reject(new Error('offline')));
    renderAt('/goals', <NextStep from="/goals" before={save} label="Save and get the plan →" />);

    await userEvent.click(screen.getByRole('button', { name: 'Save and get the plan →' }));

    await waitFor(() => expect(screen.getByRole('button', { name: 'Save and get the plan →' })).toBeEnabled());
    expect(screen.queryByText('the plan page')).not.toBeInTheDocument();
  });
});

function renderAt(path: string, element: React.ReactElement) {
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path={path} element={element} />
        <Route path="/plan" element={<p>the plan page</p>} />
      </Routes>
    </MemoryRouter>,
  );
}
