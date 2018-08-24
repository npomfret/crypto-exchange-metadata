package snowmonkey.exchangemetadata.model;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class FeeTest {

    @Test
    public void canParseRate() throws Exception {
        Fee fee = Fee.parse("4.01%");
        assertThat(fee.rate.get().value, equalTo("4.01%"));
        assertThat(fee.rate.get().asDecimal().toPlainString(), equalTo("0.0401"));
    }

    @Test
    public void canParseNegativeRate() throws Exception {
        // binance and hitbtc both have "rebate" fees

        Fee fee = Fee.parse("-4.01%");
        assertThat(fee.rate.get().value, equalTo("-4.01%"));
        assertThat(fee.rate.get().asDecimal().toPlainString(), equalTo("-0.0401"));
    }

    @Test
    public void canParseExmoStyleFees() {
        Fee fee = Fee.parse("8% + 30 UAH");
        assertThat(fee.rate.get().value, equalTo("8%"));
        assertThat(fee.fixed.get().value, equalTo(new BigDecimal(30)));
        assertThat(fee.rate.get().asDecimal().toPlainString(), equalTo("0.08"));
    }

}