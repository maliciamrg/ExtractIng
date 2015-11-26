package com.malicia.mrg.banking.scrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

public class Scraper {

	static Integer nbcpt = 0;
	static Integer nbtran = 0;
	private static Properties props;

	public static void main(String[] args) {

		try {
			props = new Properties();
			InputStream in = null;

			String path = System.getProperty("user.home") + File.separator;

			in = Scraper.class.getResourceAsStream("." + File.separator + ".." + File.separator + ".secret" + File.separator + "ing.pass.properties");
			if (in == null) {
				in = Scraper.class.getResourceAsStream(path + ".secret" + File.separator + "ing.pass.properties");
				if (in == null) {
					in = Scraper.class.getResourceAsStream(path + "ing.pass.properties");
					if (in == null) {
						in = Scraper.class.getResourceAsStream("." + File.separator + ".." + File.separator + ".." + File.separator + ".secret" + File.separator + "ing.pass.properties");
						if (in == null) {
							in = Scraper.class.getResourceAsStream("ing.pass.properties");
							if (in == null) {
								System.out.println("Sorry, unable to find " + "ing.pass.properties");
								return;
							}
						}
					}
				}
			}
			props.load(in);

			WebDriver driver = new FirefoxDriver();

			// HtmlUnitDriver driver = new
			// HtmlUnitDriver(BrowserVersion.FIREFOX_38);

			// ScreenCaptureHtmlUnitDriver driver = new
			// ScreenCaptureHtmlUnitDriver(BrowserVersion.FIREFOX_38) ;
			// driver.setJavascriptEnabled(true);

			// ChromeDriverService cdservice=new
			// ChromeDriverService.Builder().usingDriverExecutable(new
			// File("/path/to/chromedriver.exe")) .withLogFile(new
			// File("/path/to/chromedriver.log")) .withSilent(true)
			// .usingAnyFreePort() .build();

			String username = props.getProperty("ing.username");
			String password = props.getProperty("ing.password");
			String passcode = props.getProperty("ing.passcode");

			String url = props.getProperty("ing.url");
			gotourl(driver, url);

			setuserdob(driver, username, password);

			validefirstscreen(driver);

			String secpass = getpasscode(driver, passcode);

			WebElement webelementkeypad = getkeypad(driver);

			System.out.println(clickClavierMobile(secpass, driver, webelementkeypad));

			validesecondscreen(driver);

			bankscrap(driver);

			closebrowser(driver);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void closebrowser(WebDriver driver) {
		System.out.println("closebrowser" + ":");
		((WebDriver) driver).quit();
	}

	private static void bankscrap(WebDriver driver) {
		System.out.println("bankscrap" + ":");
		// TODO Auto-generated method stub
		Date curDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String DateToStr = format.format(curDate);

		try {
			Thread.sleep(5000);
			getWaitElement(driver, By.className("mainclic"));

			for (WebElement compte : ((WebDriver) driver).findElements(By.className("mainclic"))) {
				String compte_title = getWaitElement(compte, By.className("title")).getAttribute("innerHTML");
				String compte_lbl = getWaitElement(compte, By.className("lbl")).getAttribute("innerHTML");
				String compte_account_number = getWaitElement(compte, By.className("account-number")).getAttribute("innerHTML");
				String compte_account_owner = getWaitElement(compte, By.className("account-owner")).getAttribute("innerHTML");
				WebElement compte_solde = getWaitElement(compte, By.className("solde"));
				String compte_solde_digits = getWaitElement(compte_solde, By.className("digits")).getAttribute("innerHTML");

				System.out.println("compte_account_number" + ":" + compte_account_number);

				PrintWriter writer = new PrintWriter(compte_account_number + " " + DateToStr + ".qif", "UTF-8");

				writer.println("!Type:Bank");
				nbcpt++;

				compte.click();
				Thread.sleep(5000);

				for (WebElement item : ((WebDriver) driver).findElements(By.className("isotope-item"))) {
					String item_date = stringToDateToString(getWaitElement(item, By.xpath(".//span[@n='o']")).getAttribute("innerHTML"));
					String item_lbl = getWaitElement(item, By.xpath(".//span[@n='v']")).getAttribute("innerHTML");
					String item_amount = getWaitElement(item, By.className("amount")).getAttribute("innerHTML").replaceAll("[^0-9,.+-]*", "");

					writer.println("D" + item_date);
					writer.println("P" + item_lbl);
					writer.println("T" + item_amount);
					writer.println("^");
					nbtran++;
				}
				writer.close();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block ² ²
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String stringToDateToString(String sDate) {
		System.out.println("stringToDateToString" + ":" + sDate);
		Date d = null;
		String dout = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
			SimpleDateFormat formatterout = new SimpleDateFormat("dd/MM/yyyy");
			ParsePosition pos = new ParsePosition(0);
			d = formatter.parse(sDate, pos);
			dout = formatterout.format(d);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return dout;
	}

	private static void validesecondscreen(WebDriver driver) {
		System.out.println("validesecondscreen" + ":");
		try {
			// TODO Auto-generated method stub
			// mrc:mrg
			getWaitElement(driver, By.id("mrc:mrg")).click();

			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static WebElement getkeypad(WebDriver driver) {
		System.out.println("getkeypad" + ":");
		WebElement keypadele = null;
		int i;
		i = 0;
		while (keypadele == null && i <= 10) {
			i++;
			try {
				Thread.sleep(1000);
				WebElement padele = getWaitElement(driver, By.id("clavierdisplayLogin"));
				keypadele = getWaitElement(padele, By.xpath(".//img[last()]"));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return keypadele;
	}

	private static String getpasscode(WebDriver driver, String passcode) {
		System.out.println("getpasscode" + ":");// +passcode);
		WebElement pinpad = ((WebDriver) driver).findElement(By.id("digitpaddisplayLogin"));
		List<WebElement> sequence = pinpad.findElements(By.xpath(".//*"));
		String secpass = "";
		int i = 1;
		for (WebElement e : sequence) {
			if (!e.getAttribute("class").equals("plein")) {
				secpass += passcode.substring(i - 1, i);
			}
			;
			i++;
		}
		System.out.println(secpass);
		return secpass;
	}

	private static void validefirstscreen(WebDriver driver) throws InterruptedException {
		System.out.println("validefirstscreen" + ":");
		// Now submit the form. WebDriver will find the form for us from the
		// element
		getWaitElement(driver, By.id("zone1Form:submit")).click();

	}

	private static void setuserdob(WebDriver driver, String username, String password) throws InterruptedException {
		System.out.println("setuserdob" + ":");// +username+":"+password);
		// Find the text input element by its name
		getWaitElement(driver, By.id("zone1Form:numClient")).sendKeys(username);
		getWaitElement(driver, By.id("zone1Form:dateDay")).sendKeys(password.substring(0, 2));
		getWaitElement(driver, By.id("zone1Form:dateMonth")).sendKeys(password.substring(2, 4));
		getWaitElement(driver, By.id("zone1Form:dateYear")).sendKeys(password.substring(4, 8));
	}

	private static WebElement getWaitElement(WebDriver driver, By searchBy) throws InterruptedException {
		int i;
		i = 0;
		while (((WebDriver) driver).findElements(searchBy).size() == 0 && i <= 10) {
			i++;
			Thread.sleep(1000);
		}
		WebElement element = ((WebDriver) driver).findElement(searchBy);
		return element;
	}

	private static WebElement getWaitElement(WebElement conteneur, By searchBy) throws InterruptedException {
		int i;
		i = 0;
		while (conteneur.findElements(searchBy).size() == 0 && i <= 10) {
			i++;
			Thread.sleep(1000);
		}
		WebElement element = conteneur.findElement(searchBy);
		return element;
	}

	private static void gotourl(WebDriver driver, String url) throws InterruptedException {
		System.out.println("gotourl" + ":" + url);
		((WebDriver) driver).get(url);
		Thread.sleep(5000);

	}

	public static String clickClavierMobile(String SequentielPass, WebDriver driver, WebElement elementkeypad_img) {
		System.out.println("clickClavierMobile" + ":" + SequentielPass);
		try {
			String ret = "";

			InputStream resourceBuff1 = Scraper.class.getResourceAsStream("/clavierreference.jsf"); //
			ImageIcon icon1 = (new ImageIcon(ImageIO.read(resourceBuff1)));
			BufferedImage img1 = (BufferedImage) icon1.getImage();

			BufferedImage img2 = WebElementExtender.captureElementPicture(elementkeypad_img);
			// display(img2);

			for (int l = 0; l <= SequentielPass.length() - 1; ++l) {
				int y1 = 0;
				int x1 = 0;
				Integer[] y = new Integer[] { 2, 1, 1, 1, 2, 2, 2, 2, 1, 1 };
				Integer[] x = new Integer[] { 5, 2, 5, 1, 4, 1, 3, 2, 3, 4 };
				y1 = y[Integer.parseInt(SequentielPass.substring(l, l + 1))];
				x1 = x[Integer.parseInt(SequentielPass.substring(l, l + 1))];
				int w = 15;
				int h = 15;
				int taille = 40;
				boolean getout = false;
				for (int d2 = -1; d2 <= 1 && !getout; ++d2) {
					BufferedImage img1part = img1.getSubimage((x1 * taille) - w - ((taille - w) / 2), (y1 * taille) - h - ((taille - h) / 2), w, h);
					for (int y2 = 1; y2 <= 2 && !getout; ++y2) {
						for (int x2 = 1; x2 <= 5 && !getout; ++x2) {
							BufferedImage img2part = img2.getSubimage(d2 + (x2 * taille) - w - ((taille - w) / 2), (y2 * taille) - h - ((taille - h) / 2), w, h);
							double res = imgDiffPercent(img1part, img2part);
							if (res < 1) {
								String complement;
								int xclick = d2 + (x2 * taille) - w - ((taille - w) / 2) + (w / 2);
								int yclick = (y2 * taille) - h - ((taille - h) / 2) + (h / 2);
								if (ret == "") {
									complement = "";
								} else {
									complement = ",";
								}
								ret += complement + xclick + "," + yclick;
								Actions builder = new Actions((WebDriver) driver);
								builder.moveToElement(elementkeypad_img, xclick, yclick).click().build().perform();
								System.out.println(SequentielPass.substring(l, l + 1) + " " + xclick + "*" + yclick + " " + y1 + "-" + x1 + "/" + y2 + "-" + x2 + "=" + res);
								getout = true;
							}
						}
					}
				}
			}
			return ret;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	public static double imgDiffPercent(BufferedImage img1, BufferedImage img2) {
		int width1 = img1.getWidth(null);
		int width2 = img2.getWidth(null);
		int height1 = img1.getHeight(null);
		int height2 = img2.getHeight(null);
		if ((width1 != width2) || (height1 != height2)) {
			System.err.println("Error: Images dimensions mismatch");
			return 0;
		}
		long diff = 0;
		for (int y = 0; y < height1; y++) {
			for (int x = 0; x < width1; x++) {
				int rgb1 = img1.getRGB(x, y);
				int rgb2 = img2.getRGB(x, y);
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = (rgb1) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = (rgb2) & 0xff;
				diff += Math.abs(r1 - r2);
				diff += Math.abs(g1 - g2);
				diff += Math.abs(b1 - b2);
			}
		}
		double n = width1 * height1 * 3;
		double p = diff / n / 255.0;
		return (p * 100.0);
	}

	private static JFrame buildFrame() {
		JFrame frame = new JFrame(); //
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(400, 200);
		frame.setVisible(true);
		return frame;
	}

	public static void display(BufferedImage rBuff) throws InterruptedException, IOException {

		JFrame frame = buildFrame();
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		frame.add(container);
		JLabel pane1 = new JLabel();
		container.add(pane1);

		pane1.setIcon(new ImageIcon(rBuff));

		frame.revalidate();

		Thread.sleep(5000);

	}

}