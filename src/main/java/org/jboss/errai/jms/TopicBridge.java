/* jboss.org */
package org.jboss.errai.jms;

import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 19, 2010
 */
public abstract class TopicBridge implements JMSBridge {
  protected final MessageBus bus;
  protected final JMSBinding binding;
  protected TopicConnection topicConnection;
  protected TopicSession topicSession;
  protected TopicConnectionFactory topicConnectionFactory;
  protected Topic topic;

  private static final Logger log = LoggerFactory.getLogger(TopicBridge.class);

  public TopicBridge(JMSBinding binding, final MessageBus bus) {
    this.binding = binding;
    this.bus = bus;
    initConnection();
  }

  protected void initConnection() {
    InitialContext jndiContext = null;
    try {
      jndiContext = new InitialContext(binding.getProperties());
      topicConnectionFactory =
          (TopicConnectionFactory) jndiContext.lookup(binding.getProperties().getProperty(
              "java.connection.factory"));
      topic = (Topic) jndiContext.lookup(binding.getTopicName());
      topicConnection = topicConnectionFactory.createTopicConnection();
      topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    } catch (NamingException | JMSException e) {
      throw new RuntimeException("Failed to prepare topic connection", e);
    }
  }

  public JMSBinding getBinding() {
    return binding;
  }

  public void activate() {
    try {
      if (topicConnection == null)
        initConnection();
      topicConnection.start();
    } catch (JMSException e) {
      throw new RuntimeException("Failed to start topic connection", e);
    }
  }

  public void deactivate() {
    try {
      if (topicConnection != null)
        topicConnection.close();
    } catch (JMSException e) {
      throw new RuntimeException("Faile to close topic connection", e);
    }
  }
}
