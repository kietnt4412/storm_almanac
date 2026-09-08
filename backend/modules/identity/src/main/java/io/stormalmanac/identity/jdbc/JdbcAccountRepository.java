package io.stormalmanac.identity.jdbc;

import io.stormalmanac.common.id.AccountId;
import io.stormalmanac.identity.Account;
import io.stormalmanac.identity.AccountRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Accounts, keyed by what the provider says rather than by what the user typed.
 *
 * <p>The only interesting method here is {@link #upsertFromOidc}, and what makes
 * it interesting is what it does <em>not</em> do: it never looks an account up
 * by email. A provider's email claim is mutable, sometimes by the user and
 * sometimes to an address they do not control, so matching on it is the standard
 * way a service like this one hands one person's inventory to another. The
 * subject claim is the provider's promise that this is the same human as last
 * time, and it is the only thing keyed on.
 *
 * <p>Signing in with Google and then with Discord therefore produces two
 * accounts, deliberately. Merging them is a link flow performed by an
 * already-authenticated user, and it does not exist yet; the schema permits it
 * because {@code account_identity} is a separate table, but nothing in this
 * phase writes a second row.
 */
@Repository
public class JdbcAccountRepository implements AccountRepository {

    private static final RowMapper<Account> ACCOUNT = (rs, row) -> account(rs);

    private final JdbcTemplate jdbc;

    public JdbcAccountRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> find(AccountId id) {
        return one(jdbc.query(
                """
                SELECT id, display_name, email, created_at
                  FROM identity.account
                 WHERE id = ?
                """,
                ACCOUNT,
                id.value()));
    }

    /**
     * Find the account this provider subject already belongs to, or create one.
     *
     * <p>The display name and email are refreshed on every sign-in, because the
     * provider is the authority on both and a stale name is a support ticket.
     * Nothing else about the account is touched: {@code created_at} is when this
     * person first arrived, not when they last did.
     *
     * <p>Two concurrent first sign-ins by the same subject race here. The primary
     * key on {@code (provider, subject)} decides the race in the database, and
     * the loser's {@code ON CONFLICT DO NOTHING} leaves it to re-read the
     * winner's row rather than fail — which is why the insert is not the last
     * statement.
     */
    @Override
    @Transactional
    public Account upsertFromOidc(String provider, String subject, String displayName, String email) {
        require(provider, "provider");
        require(subject, "subject");

        Optional<AccountId> existing = accountFor(provider, subject);
        AccountId id = existing.orElseGet(() -> AccountId.of(UUID.randomUUID().toString()));

        if (existing.isEmpty()) {
            jdbc.update(
                    """
                    INSERT INTO identity.account (id, display_name, email)
                    VALUES (?, ?, ?)
                    """,
                    id.value(),
                    displayName == null ? "" : displayName,
                    email == null ? "" : email);
            jdbc.update(
                    """
                    INSERT INTO identity.account_identity (provider, subject, account_id)
                    VALUES (?, ?, ?)
                    ON CONFLICT (provider, subject) DO NOTHING
                    """,
                    provider,
                    subject,
                    id.value());
        } else {
            jdbc.update(
                    """
                    UPDATE identity.account SET display_name = ?, email = ? WHERE id = ?
                    """,
                    displayName == null ? "" : displayName,
                    email == null ? "" : email,
                    id.value());
        }

        // Re-read rather than construct: the row that exists may be the one the
        // other side of a race inserted, and created_at is the database's to say.
        AccountId settled = accountFor(provider, subject).orElse(id);
        return find(settled).orElseThrow(() ->
                new IllegalStateException("account vanished immediately after being written: " + settled));
    }

    private Optional<AccountId> accountFor(String provider, String subject) {
        return one(jdbc.query(
                """
                SELECT account_id
                  FROM identity.account_identity
                 WHERE provider = ? AND subject = ?
                """,
                (rs, row) -> AccountId.of(rs.getString("account_id")),
                provider,
                subject));
    }

    private static Account account(ResultSet rs) throws SQLException {
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        return new Account(
                AccountId.of(rs.getString("id")),
                rs.getString("display_name"),
                rs.getString("email"),
                createdAt == null ? null : createdAt.toInstant());
    }

    private static <T> Optional<T> one(List<T> rows) {
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    private static void require(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " must not be blank");
        }
    }
}
