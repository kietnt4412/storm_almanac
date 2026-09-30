import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { Explain } from './Explain';

/** C2: the first sentence stays, the reason is one tap away, and none of the wording is lost. */
describe('an explanation', () => {
  it('shows its first sentence, and the rest only when asked', async () => {
    render(<Explain lead="What it is.">Why it is so.</Explain>);
    expect(screen.getByText(/What it is\./)).toBeInTheDocument();
    expect(screen.queryByText('Why it is so.')).not.toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: 'Why?' }));
    expect(screen.getByText('Why it is so.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Less' })).toHaveAttribute('aria-expanded', 'true');
  });
});
