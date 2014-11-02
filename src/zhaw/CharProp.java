package zhaw;

import java.math.BigDecimal;
import java.util.Formatter;

public class CharProp {
	public int occurence = 1;
	public BigDecimal probability = new BigDecimal(0.0);
	public BigDecimal information = new BigDecimal(0.0);
	@Override
	public String toString() {
		try (Formatter ft  = new Formatter()){
			return ft.format("o=%1$7s p=%2$10s i=%3$10s", occurence, probability, information).toString();
		}
	}
};

