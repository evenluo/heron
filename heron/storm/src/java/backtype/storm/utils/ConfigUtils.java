package backtype.storm.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.twitter.heron.api.Config;

import backtype.storm.hooks.ITaskHookDelegate;

public class ConfigUtils {
  public static Config translateConfig(Map stormConfig) {
    Config heronConfig = new Config(stormConfig);
    // Look at serialization stuff first
    doSerializationTranslation(heronConfig);

    // Now look at supported apis
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS)) {
      heronConfig.put(backtype.storm.Config.TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS,
          heronConfig.get(backtype.storm.Config.TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS).toString());
    }
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_WORKERS)) {
      Integer nWorkers = (Integer) heronConfig.get(backtype.storm.Config.TOPOLOGY_WORKERS);
      com.twitter.heron.api.Config.setNumStmgrs(heronConfig, nWorkers);
    }
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_ACKER_EXECUTORS)) {
      Integer nAckers = (Integer) heronConfig.get(backtype.storm.Config.TOPOLOGY_ACKER_EXECUTORS);
      com.twitter.heron.api.Config.setEnableAcking(heronConfig, nAckers > 0);
    }
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS)) {
      Integer nSecs = (Integer) heronConfig.get(backtype.storm.Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS);
      com.twitter.heron.api.Config.setMessageTimeoutSecs(heronConfig, nSecs);
    }
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_MAX_SPOUT_PENDING)) {
      Integer nPending = Integer.parseInt(heronConfig.get(backtype.storm.Config.TOPOLOGY_MAX_SPOUT_PENDING).toString());
      com.twitter.heron.api.Config.setMaxSpoutPending(heronConfig, nPending);
    }
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS)) {
      Integer tSecs = Integer.parseInt(heronConfig.get(backtype.storm.Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS).toString());
      com.twitter.heron.api.Config.setTickTupleFrequency(heronConfig, tSecs);
    }
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_DEBUG)) {
      Boolean dBg = Boolean.parseBoolean(heronConfig.get(backtype.storm.Config.TOPOLOGY_DEBUG).toString());
      com.twitter.heron.api.Config.setDebug(heronConfig, dBg);
    }

    doTaskHooksTranslation(heronConfig);

    return heronConfig;
  }

  private static void doSerializationTranslation(Config heronConfig) {
    if (heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_FALL_BACK_ON_JAVA_SERIALIZATION) &&
        (heronConfig.get(backtype.storm.Config.TOPOLOGY_FALL_BACK_ON_JAVA_SERIALIZATION) instanceof Boolean) &&
        ((Boolean) heronConfig.get(backtype.storm.Config.TOPOLOGY_FALL_BACK_ON_JAVA_SERIALIZATION))) {
      com.twitter.heron.api.Config.setSerializationClassName(heronConfig,
          "com.twitter.heron.api.serializer.JavaSerializer");
    } else {
      heronConfig.put(backtype.storm.Config.TOPOLOGY_FALL_BACK_ON_JAVA_SERIALIZATION, false);
      com.twitter.heron.api.Config.setSerializationClassName(heronConfig,
          "backtype.storm.serialization.HeronPluggableSerializerDelegate");
      if (!heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_KRYO_FACTORY)) {
        heronConfig.put(backtype.storm.Config.TOPOLOGY_KRYO_FACTORY, "backtype.storm.serialization.DefaultKryoFactory");
      } else if (!(heronConfig.get(backtype.storm.Config.TOPOLOGY_KRYO_FACTORY) instanceof String)) {
        throw new RuntimeException(backtype.storm.Config.TOPOLOGY_KRYO_FACTORY + " has to be set to a class name");
      }
      if (!heronConfig.containsKey(backtype.storm.Config.TOPOLOGY_SKIP_MISSING_KRYO_REGISTRATIONS)) {
        heronConfig.put(backtype.storm.Config.TOPOLOGY_SKIP_MISSING_KRYO_REGISTRATIONS, false);
      } else if (!(heronConfig.get(backtype.storm.Config.TOPOLOGY_SKIP_MISSING_KRYO_REGISTRATIONS) instanceof Boolean)) {
        throw new RuntimeException(backtype.storm.Config.TOPOLOGY_SKIP_MISSING_KRYO_REGISTRATIONS + " has to be boolean");
      }
    }
  }

  /* We need to play a little dance here.
   * task hooks are a list of class names that need to be invoked at various times during a topology run.
   * However because Heron operates in com.twitter world and Strom in backtype.storm world, we
   * pass a ITaskHookDelegate to Heron and remember the actual task hooks in an internal
   * variable STORMCOMPAT_TOPOLOGY_AUTO_TASK_HOOKS
   */
  private static void doTaskHooksTranslation(Config heronConfig) {
    List<String> hooks = heronConfig.getAutoTaskHooks();
    if (hooks != null) {
      heronConfig.put(backtype.storm.Config.STORMCOMPAT_TOPOLOGY_AUTO_TASK_HOOKS, hooks);
      List<String> translationHooks = new LinkedList<String>();
      translationHooks.add(ITaskHookDelegate.class.getName());
      heronConfig.setAutoTaskHooks(translationHooks);
    }
  }
}