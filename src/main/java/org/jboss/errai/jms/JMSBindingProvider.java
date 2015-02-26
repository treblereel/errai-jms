/* jboss.org */
package org.jboss.errai.jms;


import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.server.api.ErraiConfig;
import org.jboss.errai.common.server.api.annotations.ExtensionComponent;
import org.jboss.errai.jms.JMSBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import javax.inject.Inject;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 16, 2010
 */
@ExtensionComponent
public class JMSBindingProvider implements ErraiConfig {
  private static final Logger log = LoggerFactory.getLogger(JMSBindingProvider.class);
  private static final String JMS_PREFIX = "jms.";
  private static final String JNDI_PREFIX = "jndi.";
  private static final String JNDI_KEY_PREFIX = "java.";


  @Inject
  MessageBus bus;

  @Inject
  public void init() {
    configure();
  }


  public void configure() {

    try {
      List<JMSBinding> jmsBindings = parseConfig();

      // Setup jms adapter
      for (final JMSBinding jmsBinding : jmsBindings) {
        if (jmsBinding.isSender()) {
          // create a publisher
          log.info("Errai TopicPublisher: {}", jmsBinding.getTopicName());
          new TopicPublisher(bus, jmsBinding).activate();
        } else {
          // Create subscription
          log.info("Errai TopicSubscription: {}", jmsBinding.getTopicName());
          new TopicSubscription(bus, jmsBinding).activate();
        }
      }
    } catch (Exception e) {
      log.error("Failed to process errai-jms bindings", e);
    }

  }

  private List<JMSBinding> parseConfig() {
    try {
      List<JMSBinding> jmsBindings = new ArrayList<JMSBinding>();
      Properties jndiConfig = new Properties();

      // Parse config
      log.info("Process JMS binding declarations in ErraiApp.properties");
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Enumeration<URL> cfgs = loader.getResources("ErraiApp.properties");

      while (cfgs.hasMoreElements()) {
        URL cfg = cfgs.nextElement();

        Properties config = new Properties();
        config.load(cfg.openStream());

        Set<String> processed = new HashSet<String>();
        for (String key : config.stringPropertyNames()) {
          if (key.startsWith(JMS_PREFIX)) {
            String suffix = key.substring(key.indexOf(JMS_PREFIX) + 4, key.length());
            String distinctKey = JMS_PREFIX + suffix.substring(0, suffix.indexOf("."));
            if (processed.contains(distinctKey))
              continue;
            else
              processed.add(distinctKey);
            jmsBindings.add(new JMSBinding(config.getProperty(distinctKey + ".topic"), Boolean
                .valueOf(config.getProperty(distinctKey + ".send"))));
          } else if (key.startsWith(JNDI_PREFIX)) {
            String k = JNDI_KEY_PREFIX + key.substring(JNDI_PREFIX.length(), key.length());
            jndiConfig.setProperty(k, config.getProperty(key));
          }
        }
      }

      for (JMSBinding j : jmsBindings) {
        j.setProperties(jndiConfig);
      }
      return jmsBindings;
    } catch (Exception e) {
      throw new RuntimeException("Failed to read JMS binding declarations in ErraiApp.properties");
    }
  }

  @Override
  public void addBinding(Class<?> type, ResourceProvider provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addResourceProvider(String name, ResourceProvider provider) {
    // TODO Auto-generated method stub

  }
}
