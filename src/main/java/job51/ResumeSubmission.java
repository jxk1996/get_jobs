import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 前程无忧自动投递简历
 *
 * @author loks666
 * @date 2023-05-15-05:58
 */
@Slf4j
public class ResumeSubmission {

    static boolean EnableNotifications = true;
    static Integer page = 1;
    static Integer maxPage = 50;
    static String loginUrl = "https://login.51job.com/login.php?lang=c&url=http%3A%2F%2Fwww.51job.com%2F&qrlogin=2";
    static String baseUrl = "https://we.51job.com/pc/search?keyword=%s&jobArea=020000&searchType=2&sortType=0&metro=";
    static Map<Integer, String> jobs = new HashMap<>() {{
        put(0, "综合排序");
        put(1, "活跃职位优先");
        put(2, "最新优先");
        put(3, "薪资优先");
        put(4, "距离优先");
    }};
    static ChromeDriver driver;
    static WebDriverWait wait15s;
    static Actions actions;
    static List<String> returnList = new ArrayList<>();
    static Map<Integer, String> keywords = new HashMap<>() {{
        put(0, "java");
        put(1, "python");
        put(2, "go");
        put(3, "golang");
        put(4, "大模型");
        put(5, "软件工程师");
    }};

    public static void main(String[] args) {
        initDriver();
        Date sdate = new Date();
        scanLogin();
//        keywords.forEach((k, v) -> {
//            resume(String.format(baseUrl, v));
//            try {
//                log.info("投完关键词【{}】休息30秒！", v);
//                Thread.sleep(30000);
//            } catch (InterruptedException e) {
//                log.error("投完关键词休息期间出现异常:", e);
//            }
//        });
        resume(String.format(baseUrl, keywords.get(1)));
        Date edate = new Date();
        log.info("共投递{}个简历,用时{}分", returnList.size(),
                ((edate.getTime() - sdate.getTime()) / 1000) / 60);
        if (EnableNotifications) {
            String message = "共投递" + returnList.size() + "个简历," +
                    "用时" + ((edate.getTime() - sdate.getTime()) / 1000) / 60 + "分";
            System.out.println(message);
//            new TelegramNotificationBot().sendMessageWithList(message, returnList, "前程无忧投递");
        }
        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            log.error("投完简历休息期间出现异常:", e);
        } finally {
            driver.quit();
        }
    }

    private static void initDriver() {
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:/Program Files/Google/Chrome/Application/chrome.exe");
        System.setProperty("webdriver.chrome.driver", "./src/chromedriver.exe");
//        options.addArguments("--window-position=2600,750"); // 将窗口移动到副屏的起始位置
//        options.addArguments("--window-size=1600,1000"); // 设置窗口大小以适应副屏分辨率
        options.addArguments("--start-maximized"); // 最大化窗口
        driver = new ChromeDriver(options);
        actions = new Actions(driver);
        wait15s = new WebDriverWait(driver, 15000);
    }

    static boolean isLatest = false;

    @SneakyThrows
    private static void resume(String url) {
        driver.get(url);
        Thread.sleep(1000);
        int i = 0;
        try {
            driver.findElements(By.className("ss")).get(i).click();
        } catch (Exception e) {
            findAnomaly();
        }
        for (int j = page; j <= maxPage; j++) {
            findAnomaly();
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    WebElement mytxt = wait15s.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#jump_page")));
                    mytxt.clear();
                    mytxt.sendKeys(String.valueOf(j));
                    wait15s.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#app > div > div.post > div > div > div.j_result > div > div:nth-child(2) > div > div.bottom-page > div > div > span.jumpPage"))).click();
                    actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).keyUp(Keys.CONTROL).perform();
                    log.info("{} 中，第 {} 页", jobs.get(i), j);
                    break;
                } catch (Exception e) {
                    TimeUnit.SECONDS.sleep(1);
                    log.error("mytxt.clear()可能异常！信息:{},完整异常:", e.getMessage(), e);
                }
            }
            if (!page()) {
                break;
            }
        }
    }


    private static void findAnomaly() {
        try {
            String verify = driver.findElement(By.cssSelector("#WAF_NC_WRAPPER > p.waf-nc-title")).getText();
            if (verify.contains("访问验证")) {
                //关闭弹窗
                log.error("出现访问验证了！");
                driver.quit(); // 关闭之前的ChromeDriver实例
                System.exit(0);
            }
        } catch (Exception ignored) {
            log.info("未出现访问验证，继续运行！");
        }
    }


    @SneakyThrows
    private static Boolean page() {
        Thread.sleep(1000);
        // 选择所有岗位，批量投递
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("div.ick"));
        if (checkboxes.isEmpty()) {
            return true;
        }
        List<WebElement> titles = driver.findElements(By.cssSelector("[class*='jname text-cut']"));
        List<WebElement> companies = driver.findElements(By.cssSelector("[class*='cname text-cut']"));

        JavascriptExecutor executor = driver;

        for (int i = 0; i < checkboxes.size(); i++) {
            WebElement checkbox = checkboxes.get(i);
            executor.executeScript("arguments[0].click();", checkbox);
            String title = titles.get(i).getText();
            String company = companies.get(i).getText();
            returnList.add(company + " | " + title);
            log.info("选中:{} | {} 职位", company, title);
        }
        Thread.sleep(3000);
        actions.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).keyUp(Keys.CONTROL).perform();
        boolean success = false;
        while (!success) {
            try {
                // 查询按钮是否存在
                WebElement parent = driver.findElement(By.cssSelector("div.tabs_in"));
                List<WebElement> button = parent.findElements(By.cssSelector("button.p_but"));
                // 如果按钮存在，则点击
                if (button != null && !button.isEmpty()) {
                    Thread.sleep(1000);
                    button.get(1).click();
                    success = true;
                }
            } catch (ElementClickInterceptedException e) {
                log.error("失败，1s后重试..");
                Thread.sleep(1000);
            }
        }

        Thread.sleep(1000);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < 10000) {
            try {
                String text = driver.findElement(By.cssSelector("[class*='van-popup van-popup--center']")).getText();
                if (text.contains("快来扫码下载~")) {
                    //关闭弹窗
                    driver.findElement(By.cssSelector("[class*='van-icon van-icon-cross van-popup__close-icon van-popup__close-icon--top-right']")).click();
                    return true;
                }
            } catch (Exception ignored) {
                log.info("未找到投递成功弹窗！可能为单独投递申请弹窗！");
            }
            try {
                String particularly = driver.findElement(By.xpath("//div[@class='el-dialog__body']/span")).getText();
                if (particularly.contains("需要到企业招聘平台单独申请")) {
                    //关闭弹窗
                    driver.findElement(By.cssSelector("#app > div > div.post > div > div > div.j_result > div > div:nth-child(2) > div > div:nth-child(2) > div:nth-child(2) > div > div.el-dialog__header > button > i")).click();
                    log.info("关闭单独投递申请弹窗成功！");
                    return true;
                }
            } catch (Exception ignored) {
                driver.navigate().refresh();
                TimeUnit.SECONDS.sleep(1);
                return true;
            }
            log.error("非常规投递弹窗！");
            return true;
        }
        return false;
    }


    private static void scanLogin() {
        driver.get(loginUrl);
        log.info("等待登陆..");
        wait15s.until(ExpectedConditions.presenceOfElementLocated(By.id("choose_best_list")));
    }

    private static void inputLogin() {
        driver.get(loginUrl);
        log.info("等待登陆..");
        driver.findElement(By.cssSelector("i[data-sensor-id='sensor_login_wechatScan']")).click();
        driver.findElement(By.cssSelector("a[data-sensor-id='sensor_login_passwordLogin']")).click();
        driver.findElement(By.id("loginname")).sendKeys("你的账号");
        driver.findElement(By.id("password")).sendKeys("你的密码");
        driver.findElement(By.id("isread_em")).click();
        driver.findElement(By.id("login_btn_withPwd")).click();
        // 手动点击登录按钮过验证登录
        wait15s.until(ExpectedConditions.presenceOfElementLocated(By.id("choose_best_list")));
    }

    public static void updateProxy() {
        // 定义代理信息
        String proxyHost = "proxy.proxy-mesh.com";
        int proxyPort = 31280;
        String username = "your-username";
        String password = "your-password";

        // 创建ChromeOptions
        ChromeOptions options = new ChromeOptions();

        // 创建代理对象
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyHost + ":" + proxyPort);
        proxy.setSslProxy(proxyHost + ":" + proxyPort);

        // 设置代理认证信息
        proxy.setProxyType(Proxy.ProxyType.MANUAL);
        String proxyAuth = username + ":" + password;
        proxy.setHttpProxy(proxyAuth);
        proxy.setSslProxy(proxyAuth);

        // 设置代理
        options.setProxy(proxy);

        // 更新ChromeDriver对象的代理设置
        driver.quit(); // 关闭之前的ChromeDriver实例
        driver = new ChromeDriver(options); // 创建新的ChromeDriver实例，应用更新后的代理设置
    }

}
