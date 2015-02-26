/* jboss.org */
package org.jboss.errai.jms;

import java.util.Properties;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 16, 2010
 */
public final class JMSBinding {
  private String topicName;
  private boolean sender;
  private Properties properties;

  public JMSBinding(String topicName, boolean sender) {
    this.topicName = topicName;
    this.sender = sender;
  }

  public String getTopicName() {
    return topicName;
  }

  public boolean isSender() {
    return sender;
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }
  
}
