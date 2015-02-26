/* jboss.org */
package org.jboss.errai.cli;

import javax.jms.CompletionListener;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java.util.Properties;

/**
 * Simple command line client to demo the JMS integration
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 16, 2010
 */
public class PubSubClient {
  TopicConnection conn = null;
  ConnectionFactory connectionFactory = null;
  JMSContext context = null;

  private static final String JNDI_HOST = "http-remoting://127.0.0.1:8080"; // =http-remoting://localhost:8080
  private static final String INBOUND_TOPIC = "jms/topic/inboundTopic";
  private static final String OUTBOUND_TOPIC = "jms/topic/outboundTopic";
  private static final String DEFAULT_USERNAME = "quickstartUser";
  private static final String DEFAULT_PASSWORD = "quickstartPwd1!";
  private static final String DEFAULT_CONNECTION_FACTORY = "jms/RemoteConnectionFactory";

  public void setupPubSub() throws JMSException, NamingException, InterruptedException {


    Properties props = new Properties();
    props.put(Context.PROVIDER_URL, JNDI_HOST);
    props.put(Context.INITIAL_CONTEXT_FACTORY,"org.jboss.naming.remote.client.InitialContextFactory");
    props.put(Context.SECURITY_PRINCIPAL, DEFAULT_USERNAME);
    props.put(Context.SECURITY_CREDENTIALS, DEFAULT_PASSWORD);
    
    InitialContext jndiContext = new InitialContext(props);
    connectionFactory = (ConnectionFactory) jndiContext.lookup(DEFAULT_CONNECTION_FACTORY); //ConnectionFactory
   // connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");//ConnectionFactory

    
    Topic topic = (Topic) jndiContext.lookup(INBOUND_TOPIC);
    context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);

    JMSProducer producer = context.createProducer();
  
    /**
     * On completion
     */
    producer.setAsync(new CompletionListener() {
      @Override
      public void onCompletion(Message msg) {
        TextMessage textMsg = (TextMessage) msg;
        try {
          System.out.println(" sent : " + textMsg.getText());
        } catch (JMSException e) {
          e.printStackTrace(System.err);
        }
      }

      @Override
      public void onException(Message msg, Exception ex) {
        ex.printStackTrace();
      }
    });
    
    producer.send(topic, "hello6");
    producer.send(topic, "world6");
    producer.send(topic, "asynchronously6");

    JMSConsumer jMSConsumer = context.createSharedConsumer(topic, "clientId6");
    Message message = jMSConsumer.receive();
    while (message != null) {
      System.out.println("Recive : " + ((TextMessage) message).getText());
      message = jMSConsumer.receive();
    }

    Thread.sleep(2000); // Delay before shutdown
    stop();
    System.out.println("Done");


  }

  public void sendRecvAsync() throws JMSException, NamingException, InterruptedException {
    greeting();
    setupPubSub();
  }

  private void greeting() {
    System.out.println("\n\n\n === JMS Client Demo ===");
    System.out.println("Connected to: " + JNDI_HOST);
    System.out.println("Listening on: " + OUTBOUND_TOPIC);
    System.out.println("Sending to: " + INBOUND_TOPIC);
    System.out.println("\n\n");
  }

  public void stop() throws JMSException {
    context.close();
  }

  public static void main(String args[]) throws Exception {
    PubSubClient client = new PubSubClient();
    client.sendRecvAsync();
  }

}
