package eu.alertproject.iccs.mlsensor.connector.producer;

import eu.alertproject.iccs.events.alert.MailingList;
import eu.alertproject.iccs.events.api.EventFactory;
import eu.alertproject.iccs.events.api.Topics;
import eu.alertproject.iccs.mlsensor.connector.services.ForumUrlExtractionService;
import eu.alertproject.iccs.mlsensor.mail.api.MailService;
import eu.alertproject.iccs.mlsensor.mail.api.MailServiceVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.mail.Message;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: fotis
 * Date: 18/07/12
 * Time: 08:11
 */
public class ActiveMQRTNableMessagePublisher extends AbstractMLRealTimeMessagePublisher{

    private Logger logger = LoggerFactory.getLogger(ActiveMQRTNableMessagePublisher.class);

    @Autowired
    private JmsTemplate template;

    @Autowired
    private ForumUrlExtractionService forumUrlExtractionService;

    @Autowired
    MailService mailService;

    private int messageCount =0;


    @Override
    @Scheduled(fixedDelay = 15000)
    public void readAndSend() {

        if(!isRealTimeEnabled()){
            logger.trace("void readAndSend() Realtime is not enabled {}",isRealTimeEnabled());
        }
        logger.trace("void readAndSend()");

        mailService.getUnreadMessages(new MailServiceVisitor() {
            @Override
            public boolean handle(Message message) {

                boolean handle = false;
                try {
                    template.send(
                            Topics.ALERT_MLSensor_Mail_New,
                            new JavaxMailMessageCreator(message){

                                @Override
                                public TextMessage handle(Session session, int id, int sequence, long start, String from, Date date, String subject, String content, String messageId, String references) throws JMSException {
                                    TextMessage m = session.createTextMessage();


                                    MailingList mailingList = new MailingList();
                                    mailingList.setFrom(from);
                                    mailingList.setDate(date);
                                    mailingList.setSubject(subject);
                                    mailingList.setContent(content);
                                    mailingList.setMessageId(messageId);
                                    mailingList.setReference(references);


                                    //extract forum from
                                    mailingList.setUrl(forumUrlExtractionService.extractUrl(content));

                                    String mlSensorMailNewEvent = EventFactory.createMlSensorForumNewEvent(
                                            id,
                                            start,
                                            System.currentTimeMillis(),
                                            sequence,
                                            mailingList);

                                    logger.trace("TextMessage handle() Generated {} ",mlSensorMailNewEvent);
                                    m.setText(mlSensorMailNewEvent);




                                    return m;
                                }
                            }
                    );
                    messageCount++;
                    logger.debug("Sending message {} ",messageCount);

                    handle=true;
                } catch (JmsException e) {
                    logger.error("Error sending message {} ",message,e);
                }

                return handle;

            }
        });
    }
}