package com.malicia.mrg.banking.scrapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
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
import org.openqa.selenium.chrome.ChromeDriver;
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
	private static Properties propssecret;
	static WebDriverWait wait;

	static List<InfoCompte> listcompte = new ArrayList<>();
	static int indicelistcomptecurrent;

	static List<infoBank> Bank = new ArrayList<>();

	public static void main(String[] args) {

		try {

			GetParamBank(Bank);
			System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

			listcompte = readDataFiles();
			for (infoBank bk : Bank) {
				WebDriver driver = new ChromeDriver();
				// WebDriver driver = new FirefoxDriver();
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

				allBankgrab(bk, driver);

				closebrowser(driver);
			}

			writeFiles(listcompte);
			writeDataFiles(listcompte);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void allBankgrab(infoBank bk, WebDriver driver) {
		System.out.println("bankscrap" + ":");
		// TODO Auto-generated method stub
		Date curDate = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String DateToStr = format.format(curDate);

		if (bk.title.equals("ing")) {
			bankscraping(driver);
		}
		if (bk.title.equals("boursorama")) {
			bankscrapboursorama(driver);
		}
	}

	private static void writeFiles(List<InfoCompte> listcompte2) {

		PrintWriter writer;
		nbcpt = 0;
		nbtran = 0;
		for (InfoCompte cpt : listcompte2) {
			if (cpt.getNbNouvelleTransaction() > 0) {
				nbcpt++;
				Collections.sort(cpt.trans);
				System.out.println("compte_account_number" + ":" + cpt.compte_account_number + " => "
						+ cpt.getNbNouvelleTransaction());
				try {
					System.out.println(cpt.getNomFichier());
					// File fout = new File(nfout, "UTF-8");
					File fout = new File(cpt.getNomFichier());
					boolean newfile = false;
					if (!fout.exists()) {
						fout.createNewFile();
						newfile = true;
					} else {

					}
					writer = new PrintWriter(
							new FileOutputStream(fout, true /* append = true */));
					if (newfile) {
						writer.println("!Type:Bank");
					}
					Collections.sort(cpt.trans);
					for (String tr : cpt.trans) {
						nbtran++;
						String[] trsplit = tr.split("#");
						writer.println("D" + trsplit[0]);
						writer.println("P" + trsplit[2]);
						writer.println("T" + trsplit[1]);
						writer.println("^");
					}
					writer.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		if (nbtran > 0) {
			try {
				writer = new PrintWriter("tweet_sysout.txt", "UTF-8");
				writer.println(nbcpt + " comptes et " + nbtran + " transactions recuperees");
				writer.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static void writeDataFiles(List<InfoCompte> listcpt) {
		try {
			FileOutputStream fos = new FileOutputStream("InfoCompteData.data");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(listcpt);
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<InfoCompte> readDataFiles() {
		List<InfoCompte> listcompte2 = new ArrayList();
		FileInputStream fis;
		try {
			fis = new FileInputStream("InfoCompteData.data");
			ObjectInputStream ois = new ObjectInputStream(fis);
			listcompte2 = (List<InfoCompte>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return listcompte2;
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

			if (String.copyValueOf(bk.password).replaceAll("\"", "").length() != 0) {
				driver.findElement(By.id("zone1Form:dateDay"))
						.sendKeys(String.copyValueOf(bk.password).substring(0, 2));
				driver.findElement(By.id("zone1Form:dateMonth"))
						.sendKeys(String.copyValueOf(bk.password).substring(2, 4));
				driver.findElement(By.id("zone1Form:dateYear"))
						.sendKeys(String.copyValueOf(bk.password).substring(4, 8));
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

		try {
			Thread.sleep(5000);
			getWaitElement(driver, By.className("mainclic"));

			for (WebElement compte : ((WebDriver) driver).findElements(By.className("mainclic"))) {

				InfoCompte currentcpt = new InfoCompte();
				currentcpt.compte_title = getWaitElement(compte, By.className("title")).getAttribute("innerHTML");
				currentcpt.compte_lbl = getWaitElement(compte, By.className("lbl")).getAttribute("innerHTML");
				currentcpt.compte_account_number = getWaitElement(compte, By.className("account-number"))
						.getAttribute("innerHTML");
				currentcpt.compte_account_owner = getWaitElement(compte, By.className("account-owner"))
						.getAttribute("innerHTML");
				WebElement compte_solde = getWaitElement(compte, By.className("solde"));
				String compte_solde_digits = getWaitElement(compte_solde, By.className("digits"))
						.getAttribute("innerHTML");

				int indicelistcomptecurrent = -1;
				for (int i = 0; i < listcompte.size() - 1; i++) {
					if (listcompte.get(i).compte_account_number.equals(currentcpt.compte_account_number)) {
						listcompte.get(i).maj(currentcpt);
						indicelistcomptecurrent = i;
					}
				}
				if (indicelistcomptecurrent == -1) {
					listcompte.add(currentcpt);
					indicelistcomptecurrent = listcompte.size() - 1;
				}

				// nbcpt++;

				compte.click();
				waitForPageLoaded(driver);

				for (WebElement item : ((WebDriver) driver).findElements(By.className("isotope-item"))) {
					String item_date = stringToDateToString(
							getWaitElement(item, By.xpath(".//span[@n='o']")).getAttribute("innerHTML"));
					String item_lbl = getWaitElement(item, By.xpath(".//span[@n='v']")).getAttribute("innerHTML");
					String item_amount = getWaitElement(item, By.className("amount")).getAttribute("innerHTML")
							.replaceAll("[^0-9,.+-]*", "");

					listcompte.get(indicelistcomptecurrent).addtran(stringToDateToStringTri(item_date), item_date,
							item_lbl, item_amount);

					// writer.println("D" + item_date);
					// writer.println("P" + item_lbl);
					// writer.println("T" + item_amount);
					// writer.println("^");
					nbtran++;
				}

				// writer.close();
			}

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
		if (url != "") {
			System.out.println("gotourl" + ":" + url);
			((WebDriver) driver).get(url);
			Thread.sleep(5000);
		}

	}

	public static String clickClavierMobileing(char[] SequentielPass, WebDriver driver, WebElement elementkeypad_img) {
		System.out.println("clickClavierMobile" + ":");// + SequentielPass);
		try {
			String ret = "";

			InputStream resourceBuff1 = Scraper.class.getResourceAsStream("/clavierreferenceing.jsf"); //
			ImageIcon icon1 = (new ImageIcon(ImageIO.read(resourceBuff1)));
			BufferedImage img1 = (BufferedImage) icon1.getImage();
			// display(img1);
			BufferedImage img2 = WebElementExtender.captureElementPicture(elementkeypad_img);
			// display(img2);

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
					BufferedImage img1part = img1.getSubimage((x1 * taillex) - w - ((taillex - w) / 2),
							(y1 * tailley) - h - ((tailley - h) / 2), w, h);
					for (int y2 = 1; y2 <= 2 && !getout; ++y2) {
						for (int x2 = 1; x2 <= 5 && !getout; ++x2) {
							BufferedImage img2part = img2.getSubimage(d2 + (x2 * taillex) - w - ((taillex - w) / 2),
									(y2 * tailley) - h - ((tailley - h) / 2), w, h);
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

	public static String clickClavierMobileboursorama(char[] SequentielPass, WebDriver driver,
			WebElement elementkeypad_img) {
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
					BufferedImage img1part = img1.getSubimage((x1 * taillex) - w - ((taillex - w) / 2),
							(y1 * tailley) - h - ((tailley - h) / 2), w, h);
					for (int y2 = 1; y2 <= 4 && !getout; ++y2) {
						for (int x2 = 1; x2 <= 3 && !getout; ++x2) {
							BufferedImage img2part = img2.getSubimage(d2 + (x2 * taillex) - w - ((taillex - w) / 2),
									(y2 * tailley) - h - ((tailley - h) / 2), w, h);
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

		try {
			// Thread.sleep(5000);
			getWaitElement(driver, By.className("account-name"));

			getallcompte(driver);

			getallreffrommonbudget(driver);

			listetransaction(driver);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void getallreffrommonbudget(WebDriver driver) throws InterruptedException {
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
					String newhref = getWaitElement(compte, By.xpath(".//a[@class='selectaccount']"))
							.getAttribute("href");
					for (InfoCompte cpt : listcompte) {
						if (cpt.compte_account_number.equals(amountLabel)) {
							cpt.href = newhref;
						}
					}
				}
			}
		}
	}

	private static void getallcompte(WebDriver driver) throws InterruptedException {

		// nbcpt = 0;
		for (WebElement compte : ((WebDriver) driver).findElements(By.className("account-name"))) {
			// String compte_title = getWaitElement(compte,
			// By.className("title")).getAttribute("innerHTML");

			InfoCompte currentcpt = new InfoCompte();
			currentcpt.compte_title = getWaitElement(compte, By.className("label")).getText();
			// String compte_account_owner = getWaitElement(compte,
			// By.className("account-owner")).getAttribute("innerHTML");
			String[] tmp = currentcpt.compte_title.split(" ");
			currentcpt.compte_lbl = tmp[0];
			if (tmp.length == 2) {
				currentcpt.compte_account_owner = tmp[1];
			}
			if (tmp.length == 3) {
				currentcpt.compte_lbl = currentcpt.compte_lbl + " " + tmp[1];
				currentcpt.compte_account_owner = tmp[2];
			}

			if (compte.findElements(By.className("tooltip")).size() != 0) {

				String tmpcompte_account_number = getWaitElement(compte, By.className("tooltip"))
						.getAttribute("onclick");
				Pattern p = Pattern.compile("('[^']*')+");
				Matcher m = p.matcher(tmpcompte_account_number);
				currentcpt.compte_account_number = "";
				while (m.find()) {
					tmp = m.group(1).split(" ");
					if (tmp.length > 2) {
						currentcpt.compte_account_number = tmp[2];
					}
				}
			}

			currentcpt.href = (getWaitElement(compte, By.xpath(".//span[@class='label']/a")).getAttribute("href"));

			if (currentcpt.compte_account_owner.equals("")) {
				currentcpt.compte_account_owner = currentcpt.compte_lbl;
				if (compte.findElements(By.xpath(".//div")).size() != 0) {
					tmp = compte.findElements(By.xpath(".//div")).get(0).getText().split("-");
					currentcpt.compte_lbl = tmp[0].trim();
					currentcpt.compte_title = currentcpt.compte_lbl + " " + currentcpt.compte_account_owner;
					currentcpt.compte_account_number = tmp[1].trim();
				}
			}
			System.out.println(
					"compte_account_number" + ":" + currentcpt.compte_title + " " + currentcpt.compte_account_number);

			indicelistcomptecurrent = -1;
			for (int i = 0; i < listcompte.size() - 1; i++) {
				if (listcompte.get(i).compte_account_number.equals(currentcpt.compte_account_number)) {
					listcompte.get(i).maj(currentcpt);
					indicelistcomptecurrent = i;
				}
			}
			if (indicelistcomptecurrent == -1) {
				listcompte.add(currentcpt);
				indicelistcomptecurrent = listcompte.size() - 1;
			}

			// nbcpt++;

		}
	}

	private static void listetransaction(WebDriver driver) {
		// TODO Auto-generated method stub

		try {
			Date curDate = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String DateToStr = format.format(curDate);

			// for (infocompte infocpt : tabinfocpt) {
			for (indicelistcomptecurrent = 0; indicelistcomptecurrent < listcompte.size()
					- 1; indicelistcomptecurrent++) {
				InfoCompte infocpt = listcompte.get(indicelistcomptecurrent);
				if (infocpt.href != "") {

					System.out.println("transaction of compte_account_number" + ":" + infocpt.compte_title + " "
							+ infocpt.compte_account_number);

					gotourl(driver, infocpt.href);
					waitForPageLoaded(driver);

					if (((WebDriver) driver).findElements(By.id("racine_ma-banque2_synthese_epargne_mouvements"))
							.size() != 0) {

						Date cDate = new Date();
						Calendar c = Calendar.getInstance();
						SimpleDateFormat formatmm1 = new SimpleDateFormat("'?month='MM'&year='yyyy");
						String comphref;

						((WebDriver) driver).findElements(By.id("racine_ma-banque2_synthese_epargne_mouvements")).get(0)
								.click();
						waitForPageLoaded(driver);
						writealltransaction(driver, infocpt);

						// mois -1
						c.setTime(cDate);
						c.add(Calendar.MONTH, -1);
						comphref = formatmm1.format(c.getTime());
						((WebDriver) driver).findElements(By.xpath("//a[@href='" + comphref + "']")).get(0).click();
						// https://www.boursorama.com/comptes/epargne/mouvements.phtml?month=10&year=2015
						// https://www.boursorama.com/comptes/epargne/mouvements.phtml?month=11&year=2015

						waitForPageLoaded(driver);
						writealltransaction(driver, infocpt);

						// mois -2
						c.setTime(cDate);
						c.add(Calendar.MONTH, -2);
						comphref = formatmm1.format(c.getTime());
						((WebDriver) driver).findElements(By.xpath("//a[@href='" + comphref + "']")).get(0).click();
						waitForPageLoaded(driver);
						writealltransaction(driver, infocpt);

					} else {
						if (((WebDriver) driver).findElements(By.className("account-on")).size() == 1) {
							writealltransaction(driver, infocpt);
						}
					}
					// writer.close();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writealltransaction(WebDriver driver, InfoCompte currentcpt) throws InterruptedException {
		for (WebElement item : ((WebDriver) driver).findElements(By.xpath(
				".//div[@id='content-gauche']/form/div/div[@class='bd']//table/tbody//tr[not(contains(@class, 'total'))]"))) {
			String item_date = stringToDateToString(
					getWaitElement(item, By.className("dateValeur")).getAttribute("innerHTML"));
			String item_lbl = getWaitElement(item,
					By.xpath(".//td[contains(@class,'label')]//span[not(contains(@class, 'DateOperation'))]"))
							.getAttribute("innerHTML");
			String item_amount = getWaitElement(item, By.className("amount")).getAttribute("innerHTML")
					.replaceAll("[^0-9,.+-]*", "");

			currentcpt.addtran(stringToDateToStringTri(item_date), item_date, item_lbl, item_amount);
			// writer.println("D" + item_date);
			// writer.println("P" + item_lbl);
			// writer.println("T" + item_amount);
			// writer.println("^");
			// nbtran++;
		}

		String item_date = "";
		for (WebElement item : ((WebDriver) driver).findElements(
				By.xpath(".//tbody[@id='liste-operations-page']/tr[not(contains(@class, 'form_line'))]"))) {
			if (item.findElements(By.xpath(".//td")).size() == 1) {
				item_date = stringToDateToString2(getWaitElement(item, By.xpath(".//td")).getText());
			} else {
				String item_lbl = getWaitElement(item, By.className("userLabel")).getText();
				String item_amount = getWaitElement(item, By.xpath(".//span[@class='varup' or @class='vardown']"))
						.getText().replaceAll("[^0-9,.+-]*", "");

				currentcpt.addtran(stringToDateToStringTri(item_date), item_date, item_lbl, item_amount);
				// writer.println("D" + item_date);
				// writer.println("P" + item_lbl);
				// writer.println("T" + item_amount);
				// writer.println("^");
				// nbtran++;
			}
		}
	}

	public static String stringToDateToString(String sDate) {
		System.out.println("stringToDateToString" + ":" + sDate);
		Date d = null;
		String dout = "";
		try {
			// boursorama// SimpleDateFormat formatter = new
			// SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");// ing
																				// chrome
																				// 01/02/2016
			SimpleDateFormat formatterout = new SimpleDateFormat("dd/MM/yyyy");
			ParsePosition pos = new ParsePosition(0);
			d = formatter.parse(sDate, pos);
			dout = formatterout.format(d);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return dout;
	}

	public static String stringToDateToStringTri(String sDate) {
		System.out.println("stringToDateToString" + ":" + sDate);
		Date d = null;
		String dout = "";
		try {
			// boursorama// SimpleDateFormat formatter = new
			// SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");// ing
																			// chrome
																			// 01/02/2016
			SimpleDateFormat formatterout = new SimpleDateFormat("yyyy-MM-dd");
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

	private static void GetParamBank(List<infoBank> bank2) {

		try {
//			infoBank ing = new infoBank();
//			ing.title = "ing";
//			ing.loggintextbox = "zone1Form:numClient";
//			ing.validefirst = "zone1Form:submit";
//			ing.validesecond = "mrc:mrg";
//			ing.displayLogin = "digitpaddisplayLogin";
//			Bank.add(ing);

			infoBank Bours = new infoBank();
			Bours.title = "boursorama";
			Bours.loggintextbox = "login";
			Bours.validefirst = "";
			Bours.validesecond = "btn-submit";
			Bours.displayLogin = "";
			Bank.add(Bours);

			for (infoBank bk : Bank) {
				propssecret = new Properties();
				String rep = app.getString("repertoire_secret");
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
