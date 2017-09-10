import io.github.bonigarcia.wdm.ChromeDriverManager
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.DesiredCapabilities

cacheDriver = false

waiting {
    timeout = 7
    retryInterval = 0.5
}

environments {

    chrome {
        ChromeDriverManager.getInstance().setup()

        driver = {
            ChromeOptions options = new ChromeOptions()

            options.addArguments('window-size=1200,1000')
            //TODO downloading files doesn't work headlessly. In Chrome 62 support will be added: https://bugs.chromium.org/p/chromium/issues/detail?id=696481#c39
            if (System.getProperty('headless') == 'true') {
                options.addArguments('headless', 'disable-gpu')
            }

            if (System.getProperty('disableJavaScript') == 'true') {
                options.setExperimentalOption('prefs',['profile.managed_default_content_settings.javascript': 2])
            }

            DesiredCapabilities capabilities = DesiredCapabilities.chrome()
            capabilities.setCapability(ChromeOptions.CAPABILITY, options)

            new ChromeDriver(capabilities)
        }
    }


}