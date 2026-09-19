package io.stormalmanac.gamedata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.stormalmanac.common.id.ItemId;
import java.time.Period;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShopTest {

    private static final ItemId COIN = ItemId.of("coin");
    private static final ItemStack ONE_GEM = new ItemStack(ItemId.of("gem"), 1);

    private static Shop shop(int price, int periodLimit, Period period) {
        return new Shop("offer", COIN, price, ONE_GEM, periodLimit, period, Availability.ALWAYS);
    }

    @Test
    @DisplayName("only whole periods count: a weekly limit gives nothing in six days and twice in fourteen")
    void purchasesRoundAgainstThePlayer() {
        Shop weekly = shop(10, 3, Period.ofDays(7));

        assertThat(weekly.purchasesIn(6)).isZero();
        assertThat(weekly.purchasesIn(7)).isEqualTo(3);
        assertThat(weekly.purchasesIn(14)).isEqualTo(6);
        assertThat(weekly.purchasesIn(20)).isEqualTo(6);
    }

    @Test
    @DisplayName("a month is 31 days, the long end, so a monthly limit is never promised early")
    void aMonthIsTheLongMonth() {
        Shop monthly = shop(10, 1, Period.ofMonths(1));

        assertThat(monthly.purchasesIn(30)).isZero();
        assertThat(monthly.purchasesIn(31)).isEqualTo(1);
    }

    @Test
    @DisplayName("a periodLimit of zero is no limit at all, and the period is inert")
    void zeroIsUnlimited() {
        Shop open = shop(262, 0, Period.ofDays(1));

        assertThat(open.isUnlimited()).isTrue();
        assertThat(open.purchasesIn(1)).isEqualTo(Long.MAX_VALUE);
        assertThat(open.purchasesIn(0)).isZero();
    }

    @Test
    @DisplayName("a free offer with a limit is a daily pack; a free offer with none is refused")
    void freeAndUnlimitedIsUnboundedSupply() {
        assertThat(shop(0, 1, Period.ofDays(1)).purchasesIn(3)).isEqualTo(3);

        assertThatThrownBy(() -> shop(0, 0, Period.ofDays(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unbounded free supply");
    }

    @Test
    @DisplayName("a limit that never resets is the whole allowance in any horizon of a day or more")
    void aLimitThatNeverResets() {
        Shop lifetime = shop(10, 30, null);

        assertThat(lifetime.neverResets()).isTrue();
        assertThat(lifetime.purchasesIn(0)).isZero();
        assertThat(lifetime.purchasesIn(1)).isEqualTo(30);
        assertThat(lifetime.purchasesIn(366)).isEqualTo(30);
    }

    @Test
    @DisplayName("a period that never ends is written \"never\", and reads back as one")
    void neverRoundTrips() {
        assertThat(shop(10, 30, null).periodText()).isEqualTo(Shop.NEVER);
        assertThat(Shop.parsePeriod(Shop.NEVER)).isNull();
        assertThat(Shop.parsePeriod("P7D")).isEqualTo(Period.ofDays(7));
        assertThat(shop(10, 3, Period.ofDays(7)).neverResets()).isFalse();
    }

    @Test
    @DisplayName("a limit per period needs a period of at least a day to be counted in")
    void aLimitNeedsAPeriod() {
        assertThatThrownBy(() -> shop(10, 3, Period.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no period of at least one day");
    }
}
