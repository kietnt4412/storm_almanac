import { act, fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { contactEmail, Footer } from './Footer';

/**
 * The foot of every page (2026-10-01): the maintainer's credit and contact,
 * and the server's state said only when a reader needs it. A commit hash in
 * front of readers was the bug that asked for this.
 */
describe('the footer', () => {
  it('credits Thel with a signature that writes them an email, and says nothing about a server that answers', () => {
    render(<Footer unreachable={false} />);

    expect(screen.getByRole('link', { name: 'Email Thel' })).toBeInTheDocument();
    expect(screen.queryByText(/discord/i)).not.toBeInTheDocument();
    expect(screen.queryByText(/backend|server/i)).not.toBeInTheDocument();
  });

  it('keeps the address out of the page until someone reaches for the link, so a harvester finds none', () => {
    const { container } = render(<Footer unreachable={false} />);
    const link = screen.getByRole('link', { name: 'Email Thel' });

    expect(container.innerHTML).not.toContain('@');

    fireEvent.pointerEnter(link);
    expect(link).toHaveAttribute('href', `mailto:${contactEmail()}`);
  });

  it('arms the link for a keyboard too', () => {
    render(<Footer unreachable={false} />);
    const link = screen.getByRole('link', { name: 'Email Thel' });

    act(() => link.focus());
    expect(link).toHaveAttribute('href', `mailto:${contactEmail()}`);
  });

  it('says so when the server cannot be reached, because the screens are then what this device remembers', () => {
    render(<Footer unreachable />);

    expect(screen.getByRole('status')).toHaveTextContent("Can't reach the server");
  });
});
