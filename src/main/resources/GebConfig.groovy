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

            if (System.getProperty('headless') == 'true') {
                options.addArguments('--headless')
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