package de.sko.distributedlocking;

import java.util.HashMap;
import java.util.Map;

public class MyCache
{
   private final Map<String, String> backingMap = new HashMap<>();

   String get(String key) {
      return backingMap.get( key );
   }

   void put(String key, String value) {
      backingMap.put( key, value );
   }

   boolean contains(String key) {
      return backingMap.containsKey( key );
   }
}
