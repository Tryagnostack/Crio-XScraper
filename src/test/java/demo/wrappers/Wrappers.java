package demo.wrappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import bsh.commands.dir;

import java.time.Duration;

public class Wrappers {
    /*
     * Write your selenium wrappers here
     */
    public static void scrollWrapper(WebDriver driver, WebElement e){
        new Actions(driver).scrollToElement(e);
    }

    public static WebElement nextElementWrap(WebDriver driver){
        return driver.findElement(By.xpath("//li[child::a[child::span]][last()]/a"));
    }

    public static void scrollAndClick(WebDriver d, WebElement e){
        scrollWrapper(d, e);
        e.click();
    }
}
