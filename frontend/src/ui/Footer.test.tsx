import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { DISCORD_HANDLE, Footer } from './Footer';

/**
 * The foot of every page (2026-10-01): the maintainer's credit and contact,
 * and the server's state said only when a reader needs it. A commit hash in
 * front of readers was the bug that asked for this.
 */
describe('the footer', () => {
  it('credits Thel, with a Discord handle to reach them, and says nothing about a server that answers', () => {
    render(<Footer unreachable={false} />);

    expect(screen.getByRole('img', { name: 'Thel' })).toBeInTheDocument();
    expect(screen.getByText(`@${DISCORD_HANDLE}`)).toBeInTheDocument();
    expect(screen.queryByText(/backend|server/i)).not.toBeInTheDocument();
  });

  it('says so when the server cannot be reached, because the screens are then what this device remembers', () => {
    render(<Footer unreachable />);

    expect(screen.getByRole('status')).toHaveTextContent("Can't reach the server");
  });

  it('copies the handle, and says it did', async () => {
    const user = userEvent.setup();
    const writeText = vi.spyOn(navigator.clipboard, 'writeText').mockResolvedValue();
    render(<Footer unreachable={false} />);

    await user.click(screen.getByRole('button', { name: `Copy ${DISCORD_HANDLE}` }));

    expect(writeText).toHaveBeenCalledWith(DISCORD_HANDLE);
    expect(await screen.findByText('Copied')).toBeInTheDocument();
  });
});
