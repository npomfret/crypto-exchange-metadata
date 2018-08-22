package snowmonkey.exchangemetadata.model;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class FeeTest {

    @Test
    public void canParseExmoStyleFees() {
        Fee fee = Fee.parse("8% + 30 UAH");
        assertThat(fee.rate.get().value, equalTo("8%"));
        assertThat(fee.fixed.get().value, equalTo(new BigDecimal(30)));
    }
}