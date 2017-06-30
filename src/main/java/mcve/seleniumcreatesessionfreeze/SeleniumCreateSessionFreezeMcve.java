package mcve.seleniumcreatesessionfreeze;

import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Test to narrow down issue with frozen/hanging session creation against standalone Selenium server with ChromeDriver
 */
public class SeleniumCreateSessionFreezeMcve {

	/**
	 * How many iteration to try
	 */
	private static final int ITERATIONS = 200;
	/**
	 * Where to connect to. Page content does not matter, may be empty.
	 */
	private static final String URL = "http://localhost:8089/";
	/**
	 * Hub to connect to
	 */
	private static final String HUB_URL = "http://localhost:4444/wd/hub";
	/**
	 * Which browser to use. It seem only Chrome and Chromedriver is affected.
	 */
	private static final String BROWSER = DesiredCapabilities.chrome().getBrowserName();

	public static void test() throws MalformedURLException {
		DesiredCapabilities dc = new DesiredCapabilities();
		RemoteWebDriver driver = null;

		dc.setBrowserName(BROWSER);
		driver = new RemoteWebDriver(new URL(HUB_URL), dc);

		driver.get(URL);

		driver.close();
	}

	public static void main(String[] args) throws MalformedURLException {
		for (int i = 1; i <= ITERATIONS; i++) {
			System.out.println("Run #" + i);
			test();
		}
	}
}
