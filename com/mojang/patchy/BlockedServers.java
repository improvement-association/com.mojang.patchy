package com.mojang.patchy;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

public class BlockedServers {
   @VisibleForTesting
   static final Set<String> BLOCKED_SERVERS = Sets.newHashSet();
   private static final String SRV_PREFIX = "_minecraft._tcp.";
   private static final Joiner DOT_JOINER = Joiner.on('.');
   private static final Splitter DOT_SPLITTER = Splitter.on('.');
   private static final Charset HASH_CHARSET = StandardCharsets.ISO_8859_1;

   public static boolean isBlockedServer(String server) {
      if (server != null && !server.isEmpty()) {
         if (server.startsWith("_minecraft._tcp.")) {
            server = server.substring("_minecraft._tcp.".length());
         }

         while(server.charAt(server.length() - 1) == '.') {
            server = server.substring(0, server.length() - 1);
         }

         if (isBlockedServerHostName(server)) {
            return true;
         } else {
            List<String> parts = Lists.newArrayList(DOT_SPLITTER.split(server));
            boolean isIp = isIp(parts);
            if (!isIp && isBlockedServerHostName("*." + server)) {
               return true;
            } else {
               while(parts.size() > 1) {
                  parts.remove(isIp ? parts.size() - 1 : 0);
                  String starredPart = isIp ? DOT_JOINER.join(parts) + ".*" : "*." + DOT_JOINER.join(parts);
                  if (isBlockedServerHostName(starredPart)) {
                     return true;
                  }
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   private static boolean isIp(List<String> address) {
      if (address.size() != 4) {
         return false;
      } else {
         for(String s : address) {
            try {
               int part = Integer.parseInt(s);
               if (part < 0 || part > 255) {
                  return false;
               }
            } catch (NumberFormatException var4) {
               return false;
            }
         }

         return true;
      }
   }

   private static boolean isBlockedServerHostName(String server) {
      return BLOCKED_SERVERS.contains(Hashing.sha1().hashBytes(server.toLowerCase().getBytes(HASH_CHARSET)).toString());
   }

   static {
      try {
         URLConnection urlConnection = new URL("https://sessionserver.mojang.com/blockedservers").openConnection();
         InputStream is = urlConnection.getInputStream();
         Throwable var2 = null;

         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, HASH_CHARSET));
            reader.lines().forEach(BLOCKED_SERVERS::add);
         } catch (Throwable var12) {
            var2 = var12;
            throw var12;
         } finally {
            if (is != null) {
               if (var2 != null) {
                  try {
                     is.close();
                  } catch (Throwable var11) {
                     var2.addSuppressed(var11);
                  }
               } else {
                  is.close();
               }
            }

         }
      } catch (IOException var14) {
      }

   }
}
