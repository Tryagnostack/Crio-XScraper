package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;

    /*
     * TODO: Write your tests here with testng @Test annotation. 
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */

     
    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");
        // options.addArguments("--headless");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

    @Test
    void testCase01() throws InterruptedException{
        driver.get("https://www.scrapethissite.com/pages/");

        driver.findElement(By.partialLinkText("Hockey Teams:")).click();

        ArrayList<Map<String,String>> list = new ArrayList<>();

        WebElement nextPage;

        for(int i = 1; i<=4; i++){ //loop to iterate 4 pages
            nextPage = Wrappers.nextElementWrap(driver);
            Wrappers.scrollAndClick(driver, nextPage);
            // System.out.println("Iter: " + i);
            // System.out.println(driver.getCurrentUrl());

            List<WebElement> rows = driver.findElements(By.xpath("//tr[child::td]"));
            for(WebElement row : rows){
                // Wrappers.scrollWrapper(driver, row);

                //create a map to store relevant table values
                HashMap<String,String> map = new HashMap<>();
                WebElement Team = row.findElement(By.xpath("./td[1]"));
                WebElement Year = row.findElement(By.xpath("./td[2]"));
                WebElement Win = row.findElement(By.xpath("./td[6]"));
                if(Float.parseFloat(Win.getText()) > 0.4) //skip the row if not relevant
                    continue;
                
                //get and store epoch of the scrape
                String epoch = Long.toString(System.currentTimeMillis());
                map.put("Epoch of scrape", epoch);

                //store other details
                map.put("Team Name", Team.getText());
                map.put("Year", Year.getText());
                map.put("Win %", Win.getText());
                System.out.println("map item: " + map);

                //add the values to the map
                list.add(map);
                
                // System.out.println("size: " + list.size());
                // System.out.println("1st: " + list.get(0));
                // System.out.println("last: " + list.get(list.size()-1));
            }
        }

        File dir = new File("hockey-team-data.json");
        ObjectMapper mapper = new ObjectMapper();

        //output the list into the json file
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(dir, list);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread.sleep(2000);
    }

    @Test
    void testCase02() throws InterruptedException{
        driver.get("https://www.scrapethissite.com/pages/");

        driver.findElement(By.partialLinkText("Oscar")).click();

        File dir = new File("output");
        if(!dir.exists()){
            dir.mkdirs();
        }

        File output = new File(dir + "/oscar-winner-data.json");

        System.out.println(dir.getAbsolutePath());

        //list of year links on page
        List<WebElement> years = driver.findElements(By.xpath("//a[starts-with(text(),'20')]"));
        ArrayList<HashMap<String,String>> jsonList = new ArrayList<>();

        for(WebElement year : years){
            year.click();

            //wait for table to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5L));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("td")));

            List<WebElement> rows = driver.findElements(By.xpath("//tr[child::td]"));

            
            //only 5 rows needed
            for(int i = 0; i < 5; i++){
                WebElement row = rows.get(i);
                String epoch = Long.toString(System.currentTimeMillis());
                String yearString = year.getText();
                String title = row.findElement(By.xpath("./td[1]")).getText();
                String nominations = row.findElement(By.xpath("./td[2]")).getText();
                String awards = row.findElement(By.xpath("./td[3]")).getText();
                WebElement winnerField = row.findElement(By.xpath("./td[4]"));                
                Boolean isWinner = false;
                try{
                    winnerField.findElement(By.xpath("./*"));
                    isWinner = true;
                } catch (NoSuchElementException e){}

                //hashmap to store data before putting to list
                HashMap<String,String> map = new HashMap<>();
                map.put("Epoch Time of Scrap", epoch);
                map.put("Year", yearString);
                map.put("Title", title);
                map.put("Nominations", nominations);
                map.put("Awards", awards);
                map.put("isWinner", isWinner.toString());
                System.out.println(yearString + " iter: " + i);
                System.out.println(map);

                //add the map to list
                jsonList.add(map);
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        //output the list into the json file
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(output, jsonList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(output.exists(),"output file doesn't exist");
        Assert.assertNotEquals(output.length(),0,"File is empty");
        Thread.sleep(3000);
    }



    @AfterTest
    public void endTest()
    {
        // driver.close();
        driver.quit();

    }
}