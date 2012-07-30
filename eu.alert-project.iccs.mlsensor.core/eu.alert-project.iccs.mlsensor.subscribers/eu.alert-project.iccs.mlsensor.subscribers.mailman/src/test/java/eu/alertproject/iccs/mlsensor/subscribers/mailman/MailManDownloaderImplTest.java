package eu.alertproject.iccs.mlsensor.subscribers.mailman;

import eu.alertproject.iccs.mlsensor.subscribers.api.MailDownloader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.List;

/**
 * User: fotis
 * Date: 02/11/11
 * Time: 20:33
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mailmanTestApplicationContext.xml","classpath:testApplicationContext.xml"})
public class MailManDownloaderImplTest {

    @Autowired
    private MailDownloader mailDownloader;

    @Test
    public void fetchUrl(){

        try {
            List<URL> urls = mailDownloader.fetchUrls("http://mail.kde.org/pipermail/kde-hardware-devel");

            Assert.assertNotNull(urls);

            //The 10 magic number is a random number , which came out of the assumption that
            // there must be at least 10 downloadable urls
            Assert.assertTrue(urls.size() > 10);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Test
    public void loadUrls(){



        try {
            List<URL> urls = mailDownloader.fetchUrls("http://mail.kde.org/pipermail/kde-hardware-devel");

            Assert.assertNotNull(urls);

            //The 10 magic number is a random number , which came out of the assumption that
            // there must be at least 10 downloadable urls
            Assert.assertTrue(urls.size() > 10);

            mailDownloader.loadMessages(urls);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }



}
