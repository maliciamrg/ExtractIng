package com.malicia.mrg.banking.scrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Scraper {

	static Integer nbcpt = 0;
	static Integer nbtran = 0;
	private static Properties props;
	private static Properties propssecret;
	static WebDriverWait wait;

	static infoBank[] Bank = new infoBank[2];

	public static void main(String[] args) {

		try {
			props = new Properties();
			InputStream in = null;
			in = Scraper.class.getResourceAsStream("/app.properties");
			props.load(in);

			GetParamBank(Bank);

			for (infoBank bk : Bank) {

				WebDriver driver = new FirefoxDriver();
				driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
				wait = new WebDriverWait(driver, 20);
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
				gotourl(driver, bk.url);

				setuserdob(driver, bk);

				validefirstscreen(driver, bk);

				char[] secpass = getpasscode(driver, bk.passcode, bk);
				WebElement webelementkeypad = null;
				if (bk.title.equals("ing")) {
					webelementkeypad = getkeypading(driver);
				}
				if (bk.title.equals("boursorama")) {
					webelementkeypad = getkeypadboursrama(driver);
				}

				if (bk.title.equals("ing")) {
					System.out.println(clickClavierMobileing(secpass, driver, webelementkeypad));
				}
				if (bk.title.equals("boursorama")) {
					System.out.println(clickClavierMobileboursorama(secpass, driver, webelementkeypad));
				}

				validesecondscreen(driver, bk);

				if (bk.title.equals("ing")) {
					bankscraping(driver);
				}
				if (bk.title.equals("boursorama")) {
					bankscrapboursorama(driver);
				}

				closebrowser(driver);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static WebElement getkeypading(WebDriver driver) {
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

	private static WebElement getkeypadboursrama(WebDriver driver) throws InterruptedException {
		System.out.println("getkeypad" + ":");
		WebElement keypadele = null;
		if (driver.findElements(By.id("password")).size() != 0) {
			if ((driver.findElements(By.id("password"))).get(0).isDisplayed()) {
				getWaitElement(driver, By.id("password")).click();
				if (driver.findElements(By.name("submit2")).size() != 0) {
					if (driver.findElements(By.name("submit2")).get(0).isDisplayed()) {
						getWaitElement(driver, By.name("submit2")).click();
					}
				}
			}
		}
		int i;
		i = 0;
		while (keypadele == null && i <= 10) {
			i++;
			try {

				Thread.sleep(1000);
				keypadele = getWaitElement(driver, By.id("login-pad_pad"));
				// keypadele = getWaitElement(padele,
				// By.xpath(".//img[last()]"));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return keypadele;
	}

	private static char[] getpasscode(WebDriver driver, char[] passcode, infoBank bk) {
		System.out.println("getpasscode" + ":");// +passcode);

		if (!bk.displayLogin.equals("")) {
			WebElement pinpad = ((WebDriver) driver).findElement(By.id(bk.displayLogin));
			List<WebElement> sequence = pinpad.findElements(By.xpath(".//*"));
			char[] secpass = new char[] { 1, 2, 3 };
			int i = 0;
			int ii = 0;
			for (WebElement e : sequence) {
				if (!e.getAttribute("class").equals("plein")) {
					secpass[ii] = passcode[i];// substring(i - 1, i);
					ii++;
				}
				;
				i++;
			}
			return secpass;
		} else {
			return passcode;
		}
	}

	private static void validefirstscreen(WebDriver driver, infoBank bk) throws InterruptedException {
		System.out.println("validefirstscreen" + ":");
		// Now submit the form. WebDriver will find the form for us from the
		// element
		if (!bk.validefirst.equals("")) {
			WebElement a = driver.findElement(By.id(bk.validefirst));
			a.sendKeys(Keys.ENTER);

			waitForPageLoaded(driver);
		}

	}

	private static void setuserdob(WebDriver driver, infoBank bk) {
		// Find the text input element by its name
		try {
			Thread.sleep(500);
			driver.findElement(By.id(bk.loggintextbox)).click();
			Thread.sleep(500);
			driver.findElement(By.id(bk.loggintextbox)).sendKeys(String.copyValueOf(bk.username));

			if (String.copyValueOf(bk.password).replaceAll("\"", "").length()!=0) {
				driver.findElement(By.id("zone1Form:dateDay")).sendKeys(String.copyValueOf(bk.password).substring(0, 2));
				driver.findElement(By.id("zone1Form:dateMonth")).sendKeys(String.copyValueOf(bk.password).substring(2, 4));
				driver.findElement(By.id("zone1Form:dateYear")).sendKeys(String.copyValueOf(bk.password).substring(4, 8));
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void closebrowser(WebDriver driver) {
		System.out.println("closebrowser" + ":");
		((WebDriver) driver).quit();
	}

	private static void bankscraping(WebDriver driver) {
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

				PrintWriter writer = new PrintWriter("qif " + compte_account_number + " " + compte_title + " " + DateToStr + ".qif", "UTF-8");

				writer.println("!Type:Bank");
				nbcpt++;

				compte.click();
				waitForPageLoaded(driver);

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
			PrintWriter writer = new PrintWriter("tweet_sysout.txt", "UTF-8");
			writer.println(nbcpt + " comptes et " + nbtran + " transactions recuperées");
			writer.close();

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

	/*
	 * public static String stringToDateToString(String sDate) {
	 * System.out.println("stringToDateToString" + ":" + sDate); Date d = null;
	 * String dout = null; try { SimpleDateFormat formatter = new
	 * SimpleDateFormat("dd MMM yyyy"); SimpleDateFormat formatterout = new
	 * SimpleDateFormat("dd/MM/yyyy"); ParsePosition pos = new ParsePosition(0);
	 * d = formatter.parse(sDate, pos); dout = formatterout.format(d); } catch
	 * (RuntimeException e) { e.printStackTrace(); } return dout; }
	 */
	private static void validesecondscreen(WebDriver driver, infoBank bk) {
		System.out.println("validesecondscreen" + ":");
		try {
			// TODO Auto-generated method stub
			// mrc:mrg
			getWaitElement(driver, By.id(bk.validesecond)).click();
			waitForPageLoaded(driver);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void waitForPageLoaded(WebDriver driver) throws InterruptedException {
		Thread.sleep(5000);
		ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
		Wait<WebDriver> wait = new WebDriverWait(driver, 30);
		try {
			wait.until(expectation);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private static WebElement getElement(WebElement conteneur, By searchBy) throws InterruptedException {
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

	public static String clickClavierMobileing(char[] SequentielPass, WebDriver driver, WebElement elementkeypad_img) {
		System.out.println("clickClavierMobile" + ":");// + SequentielPass);
		try {
			String ret = "";

			InputStream resourceBuff1 = Scraper.class.getResourceAsStream("/clavierreferenceing.jsf"); //
			ImageIcon icon1 = (new ImageIcon(ImageIO.read(resourceBuff1)));
			BufferedImage img1 = (BufferedImage) icon1.getImage();
			//display(img1);
			BufferedImage img2 = WebElementExtender.captureElementPicture(elementkeypad_img);
			//display(img2);

			for (int l = 0; l <= SequentielPass.length - 1; ++l) {
				int y1 = 0;
				int x1 = 0;
				Integer[] y = new Integer[] { 2, 1, 1, 1, 2, 2, 2, 2, 1, 1 };
				Integer[] x = new Integer[] { 5, 2, 5, 1, 4, 1, 3, 2, 3, 4 };
				// y1 = y[Integer.parseInt(SequentielPass[l]];//.substring(l, l
				// + 1))];
				// x1 = x[Integer..parseInt(SequentielPass[l])];//.substring(l,
				// l + 1))];
				y1 = y[Integer.valueOf(String.valueOf(SequentielPass[l]))];// .substring(l,
																			// l
																			// +
																			// 1))];
				x1 = x[Integer.valueOf(String.valueOf(SequentielPass[l]))];// .substring(l,
																			// l
																			// +
																			// 1))];
				int w = 15;
				int h = 15;

				int tailley = 40;
				int taillex = 40;
				boolean getout = false;
				for (int d2 = -1; d2 <= 1 && !getout; ++d2) {
					BufferedImage img1part = img1.getSubimage((x1 * taillex) - w - ((taillex - w) / 2), (y1 * tailley) - h - ((tailley - h) / 2), w, h);
					for (int y2 = 1; y2 <= 2 && !getout; ++y2) {
						for (int x2 = 1; x2 <= 5 && !getout; ++x2) {
							BufferedImage img2part = img2.getSubimage(d2 + (x2 * taillex) - w - ((taillex - w) / 2), (y2 * tailley) - h - ((tailley - h) / 2), w, h);
							double res = imgDiffPercent(img1part, img2part);

							// System.out.println(SequentielPass[l] + " = " + y1
							// + " " + x1 + " : " + y2 + " " + x2 + " " + res);
							if (res < 1) {
								String complement;
								int xclick = d2 + (x2 * taillex) - w - ((taillex - w) / 2) + (w / 2);
								int yclick = (y2 * tailley) - h - ((tailley - h) / 2) + (h / 2);
								if (ret == "") {
									complement = "";
								} else {
									complement = ",";
								}
								ret += complement + xclick + "," + yclick;
								Actions builder = new Actions((WebDriver) driver);
								builder.moveToElement(elementkeypad_img, xclick, yclick).click().build().perform();
								// System.out.println(SequentielPass.substring(l,
								// l + 1) + " " + xclick + "*" + yclick + " " +
								// y1 + "-" + x1 + "/" + y2 + "-" + x2 + "=" +
								// res);
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

	public static String clickClavierMobileboursorama(char[] SequentielPass, WebDriver driver, WebElement elementkeypad_img) {
		System.out.println("clickClavierMobile" + ":");// + SequentielPass);
		try {
			String ret = "";

			InputStream resourceBuff1 = Scraper.class.getResourceAsStream("/clavierreferenceboursorama.jsf"); //
			ImageIcon icon1 = (new ImageIcon(ImageIO.read(resourceBuff1)));
			BufferedImage img1 = (BufferedImage) icon1.getImage();

			BufferedImage img2 = WebElementExtender.captureElementPicture(elementkeypad_img);
			// display(img2);

			for (int l = 0; l <= SequentielPass.length - 1; ++l) {
				int y1 = 0;
				int x1 = 0;
				Integer[] y = new Integer[] { 3, 2, 4, 3, 4, 1, 1, 3, 3, 4 };
				Integer[] x = new Integer[] { 1, 1, 1, 3, 3, 2, 1, 2, 1, 2 };
				// y1 = y[Integer.parseInt(SequentielPass[l]];//.substring(l, l
				// + 1))];
				// x1 = x[Integer..parseInt(SequentielPass[l])];//.substring(l,
				// l + 1))];
				y1 = y[Integer.valueOf(String.valueOf(SequentielPass[l]))];// .substring(l,
																			// l
																			// +
																			// 1))];
				x1 = x[Integer.valueOf(String.valueOf(SequentielPass[l]))];// .substring(l,
																			// l
																			// +
																			// 1))];
				int w = 40;
				int h = 40;

				int tailley = 60;
				int taillex = 100;
				boolean getout = false;
				for (int d2 = -1; d2 <= 1 && !getout; ++d2) {
					BufferedImage img1part = img1.getSubimage((x1 * taillex) - w - ((taillex - w) / 2), (y1 * tailley) - h - ((tailley - h) / 2), w, h);
					for (int y2 = 1; y2 <= 4 && !getout; ++y2) {
						for (int x2 = 1; x2 <= 3 && !getout; ++x2) {
							BufferedImage img2part = img2.getSubimage(d2 + (x2 * taillex) - w - ((taillex - w) / 2), (y2 * tailley) - h - ((tailley - h) / 2), w, h);
							double res = imgDiffPercent(img1part, img2part);

							// System.out.println(SequentielPass[l] + " = " + y1
							// + " " + x1 + " : " + y2 + " " + x2 + " " + res);
							if (res < 1) {
								String complement;
								int xclick = d2 + (x2 * taillex) - w - ((taillex - w) / 2) + (w / 2);
								int yclick = (y2 * tailley) - h - ((tailley - h) / 2) + (h / 2);
								if (ret == "") {
									complement = "";
								} else {
									complement = ",";
								}
								ret += complement + xclick + "," + yclick;
								Actions builder = new Actions((WebDriver) driver);
								builder.moveToElement(elementkeypad_img, xclick, yclick).click().build().perform();
								// System.out.println(SequentielPass.substring(l,
								// l + 1) + " " + xclick + "*" + yclick + " " +
								// y1 + "-" + x1 + "/" + y2 + "-" + x2 + "=" +
								// res);
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

	private static void bankscrapboursorama(WebDriver driver) {
		System.out.println("bankscrap" + ":");
		// TODO Auto-generated method stub
		try {
			// Thread.sleep(5000);
			getWaitElement(driver, By.className("account-name"));
			infocompte[] tabinfocpt = new infocompte[((WebDriver) driver).findElements(By.className("account-name")).size()];

			getallcompte(driver, tabinfocpt);

			getallreffrommonbudget(driver, tabinfocpt);

			listetransaction(driver, tabinfocpt);

			PrintWriter writer = new PrintWriter("tweet_sysout.txt", "UTF-8");
			writer.println(nbcpt + " comptes et " + nbtran + " transactions recuperées");
			writer.close();

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

	private static void getallreffrommonbudget(WebDriver driver, infocompte[] tabinfocpt) throws InterruptedException {
		// correctrion href avec budget
		if (driver.findElements(By.xpath("//a[@title='Mes services']")).size() != 0) {
			driver.findElements(By.xpath("//a[@title='Mes services']")).get(0).click();
			waitForPageLoaded(driver);
		}

		if (driver.findElements(By.xpath("//div[@class='title']/a[@href='/moneycenter/monbudget/']")).size() != 0) {
			driver.findElements(By.xpath("//div[@class='title']/a[@href='/moneycenter/monbudget/']")).get(0).click();
			waitForPageLoaded(driver);
			for (WebElement compte : ((WebDriver) driver).findElements(By.xpath(".//div[@class='bd']/ul/li"))) {
				String amountLabel = getWaitElement(compte, By.className("amountLabel")).getText();
				if (compte.findElements(By.xpath("//a[@class='selectaccount']")).size() != 0) {
					String newhref = getWaitElement(compte, By.xpath(".//a[@class='selectaccount']")).getAttribute("href");
					for (int i = 0; i < nbcpt; i++) {
						if (tabinfocpt[i].compte_account_number.equals(amountLabel)) {
							tabinfocpt[i].href = newhref;
						}
					}
				}
			}
		}
	}

	private static void getallcompte(WebDriver driver, infocompte[] tabinfocpt) throws InterruptedException {
		nbcpt = 0;
		for (WebElement compte : ((WebDriver) driver).findElements(By.className("account-name"))) {
			// String compte_title = getWaitElement(compte,
			// By.className("title")).getAttribute("innerHTML");
			tabinfocpt[nbcpt] = new infocompte();
			tabinfocpt[nbcpt].compte_title = getWaitElement(compte, By.className("label")).getText();
			// String compte_account_owner = getWaitElement(compte,
			// By.className("account-owner")).getAttribute("innerHTML");
			String[] tmp = tabinfocpt[nbcpt].compte_title.split(" ");
			tabinfocpt[nbcpt].compte_lbl = tmp[0];
			if (tmp.length == 2) {
				tabinfocpt[nbcpt].compte_account_owner = tmp[1];
			}
			if (tmp.length == 3) {
				tabinfocpt[nbcpt].compte_lbl = tabinfocpt[nbcpt].compte_lbl + " " + tmp[1];
				tabinfocpt[nbcpt].compte_account_owner = tmp[2];
			}

			if (compte.findElements(By.className("tooltip")).size() != 0) {

				String tmpcompte_account_number = getWaitElement(compte, By.className("tooltip")).getAttribute("onclick");
				Pattern p = Pattern.compile("('[^']*')+");
				Matcher m = p.matcher(tmpcompte_account_number);
				tabinfocpt[nbcpt].compte_account_number = "";
				while (m.find()) {
					tmp = m.group(1).split(" ");
					if (tmp.length > 2) {
						tabinfocpt[nbcpt].compte_account_number = tmp[2];
					}
				}
			}

			tabinfocpt[nbcpt].href = getWaitElement(compte, By.xpath(".//span[@class='label']/a")).getAttribute("href");

			if (tabinfocpt[nbcpt].compte_account_owner.equals("")) {
				tabinfocpt[nbcpt].compte_account_owner = tabinfocpt[nbcpt].compte_lbl;
				if (compte.findElements(By.xpath(".//div")).size() != 0) {
					tmp = compte.findElements(By.xpath(".//div")).get(0).getText().split("-");
					tabinfocpt[nbcpt].compte_lbl = tmp[0].trim();
					tabinfocpt[nbcpt].compte_title = tabinfocpt[nbcpt].compte_lbl + " " + tabinfocpt[nbcpt].compte_account_owner;
					tabinfocpt[nbcpt].compte_account_number = tmp[1].trim();
				}
			}
			System.out.println("compte_account_number" + ":" + tabinfocpt[nbcpt].compte_title + " " + tabinfocpt[nbcpt].compte_account_number);
			nbcpt++;

		}
	}

	private static void listetransaction(WebDriver driver, infocompte[] tabinfocpt) {
		// TODO Auto-generated method stub

		try {
			Date curDate = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String DateToStr = format.format(curDate);

			for (infocompte infocpt : tabinfocpt) {

				System.out.println("transaction of compte_account_number" + ":" + infocpt.compte_title + " " + infocpt.compte_account_number);

				PrintWriter writer;
				writer = new PrintWriter("qif " + infocpt.compte_account_number + " " + infocpt.compte_title + " " + DateToStr + ".qif", "UTF-8");

				writer.println("!Type:Bank");

				gotourl(driver, infocpt.href);
				waitForPageLoaded(driver);

				if (((WebDriver) driver).findElements(By.id("racine_ma-banque2_synthese_epargne_mouvements")).size() != 0) {

					Date cDate = new Date();
					Calendar c = Calendar.getInstance();
					SimpleDateFormat formatmm1 = new SimpleDateFormat("'?month='MM'&year='yyyy");
					String comphref;

					((WebDriver) driver).findElements(By.id("racine_ma-banque2_synthese_epargne_mouvements")).get(0).click();
					waitForPageLoaded(driver);
					writealltransaction(driver, writer);

					// mois -1
					c.setTime(cDate);
					c.add(Calendar.MONTH, -1);
					comphref = formatmm1.format(c.getTime());
					((WebDriver) driver).findElements(By.xpath("//a[@href='" + comphref + "']")).get(0).click();
					// https://www.boursorama.com/comptes/epargne/mouvements.phtml?month=10&year=2015
					// https://www.boursorama.com/comptes/epargne/mouvements.phtml?month=11&year=2015

					waitForPageLoaded(driver);
					writealltransaction(driver, writer);

					// mois -2
					c.setTime(cDate);
					c.add(Calendar.MONTH, -2);
					comphref = formatmm1.format(c.getTime());
					((WebDriver) driver).findElements(By.xpath("//a[@href='" + comphref + "']")).get(0).click();
					waitForPageLoaded(driver);
					writealltransaction(driver, writer);

				} else {
					if (((WebDriver) driver).findElements(By.className("account-on")).size() == 1) {
						writealltransaction(driver, writer);
					}
				}
				writer.close();

			}
		} catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writealltransaction(WebDriver driver, PrintWriter writer) throws InterruptedException {
		for (WebElement item : ((WebDriver) driver)
				.findElements(By.xpath(".//div[@id='content-gauche']/form/div/div[@class='bd']//table/tbody//tr[not(contains(@class, 'total'))]"))) {
			String item_date = stringToDateToString(getWaitElement(item, By.className("dateValeur")).getAttribute("innerHTML"));
			String item_lbl = getWaitElement(item, By.xpath(".//td[contains(@class,'label')]//span[not(contains(@class, 'DateOperation'))]")).getAttribute("innerHTML");
			String item_amount = getWaitElement(item, By.className("amount")).getAttribute("innerHTML").replaceAll("[^0-9,.+-]*", "");

			writer.println("D" + item_date);
			writer.println("P" + item_lbl);
			writer.println("T" + item_amount);
			writer.println("^");
			nbtran++;
		}

		String item_date = "";
		for (WebElement item : ((WebDriver) driver).findElements(By.xpath(".//tbody[@id='liste-operations-page']/tr[not(contains(@class, 'form_line'))]"))) {
			if (item.findElements(By.xpath(".//td")).size() == 1) {
				item_date = stringToDateToString2(getWaitElement(item, By.xpath(".//td")).getText());
			} else {
				String item_lbl = getWaitElement(item, By.className("userLabel")).getText();
				String item_amount = getWaitElement(item, By.xpath(".//span[@class='varup' or @class='vardown']")).getText().replaceAll("[^0-9,.+-]*", "");

				writer.println("D" + item_date);
				writer.println("P" + item_lbl);
				writer.println("T" + item_amount);
				writer.println("^");
				nbtran++;
			}
		}
	}

	public static String stringToDateToString(String sDate) {
		System.out.println("stringToDateToString" + ":" + sDate);
		Date d = null;
		String dout = "";
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat formatterout = new SimpleDateFormat("dd/MM/yyyy");
			ParsePosition pos = new ParsePosition(0);
			d = formatter.parse(sDate, pos);
			dout = formatterout.format(d);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return dout;
	}

	public static String stringToDateToString2(String sDate) {
		System.out.println("stringToDateToString" + ":" + sDate);
		Date d = null;
		String dout = null;
		Date curDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(curDate);
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("EEEE dd MMMM yyyy");
			SimpleDateFormat formatterout = new SimpleDateFormat("dd/MM/yyyy");

			if (sDate.toLowerCase().equals("aujourd'hui")) {
				return formatterout.format(c.getTime());
			}
			if (sDate.toLowerCase().equals("hier")) {
				c.add(Calendar.DATE, -1);
				return formatterout.format(c.getTime());
			}
			if (sDate.toLowerCase().equals("avant-hier")) {
				c.add(Calendar.DATE, -2);
				return formatterout.format(c.getTime());
			}
			if (sDate.toLowerCase().equals("demain")) {
				c.add(Calendar.DATE, 1);
				return formatterout.format(c.getTime());
			}
			if (sDate.toLowerCase().equals("aprés-demain")) {
				c.add(Calendar.DATE, 2);
				return formatterout.format(c.getTime());
			}
			ParsePosition pos = new ParsePosition(0);
			d = formatter.parse(sDate, pos);
			dout = formatterout.format(d);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return dout;
	}

	private static void GetParamBank(infoBank[] tabBank) {

		try {
			Bank[0] = new infoBank();
			Bank[0].title = "ing";
			Bank[0].loggintextbox = "zone1Form:numClient";
			Bank[0].validefirst = "zone1Form:submit";
			Bank[0].validesecond = "mrc:mrg";
			Bank[0].displayLogin = "digitpaddisplayLogin";

			Bank[1] = new infoBank();
			Bank[1].title = "boursorama";
			Bank[1].loggintextbox = "login";
			Bank[1].validefirst = "";
			Bank[1].validesecond = "btn-submit";
			Bank[1].displayLogin = "";

			for (infoBank bk : Bank) {
				propssecret = new Properties();
				String rep = props.getProperty("repertoire_secret");
				rep = rep.replace("~", System.getProperty("user.home"));
				if (SystemUtils.IS_OS_WINDOWS) {
					rep = rep.replace("/", "\\");
				}
				;
				rep = rep + bk.title + ".pass.properties";
				propssecret.load(new FileInputStream(rep));
				bk.username = propssecret.getProperty(bk.title + ".username").toCharArray();
				bk.password = propssecret.getProperty(bk.title + ".password").toCharArray();
				bk.passcode = propssecret.getProperty(bk.title + ".passcode").toCharArray();
				bk.url = propssecret.getProperty(bk.title + ".url");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

class infoBank {
	String title = "";
	char[] username;
	char[] password;
	char[] passcode;
	String loggintextbox = "";
	String validefirst = "";
	String validesecond = "";
	String displayLogin = "";
	String url = "";
}

class infocompte {
	String compte_title = "";
	String compte_lbl = "";
	String compte_account_number = "";
	String compte_account_owner = "";
	String href = "";
}
