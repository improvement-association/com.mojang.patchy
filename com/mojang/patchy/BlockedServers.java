package com.mojang.patchy;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class BlockedServers implements Predicate<String> {
   private final Set<String> blockedServers;
   private static final String SRV_PREFIX = "_minecraft._tcp.";
   private static final Joiner DOT_JOINER = Joiner.on('.');
   private static final Splitter DOT_SPLITTER = Splitter.on('.');
   public static final Charset HASH_CHARSET = StandardCharsets.ISO_8859_1;

   public BlockedServers(Collection<String> blockedServers) {
      this.blockedServers = ImmutableSet.copyOf(blockedServers);
   }

   public boolean apply(@Nullable String server) {
      if (server != null && !server.isEmpty()) {
         if (server.startsWith("_minecraft._tcp.")) {
            server = server.substring("_minecraft._tcp.".length());
         }

         while(server.charAt(server.length() - 1) == '.') {
            server = server.substring(0, server.length() - 1);
         }

         if (this.isBlockedServerHostName(server)) {
            return true;
         } else {
            List<String> parts = Lists.newArrayList(DOT_SPLITTER.split(server));
            boolean isIp = isIp(parts);
            if (!isIp && this.isBlockedServerHostName("*." + server)) {
               return true;
            } else {
               while(parts.size() > 1) {
                  parts.remove(isIp ? parts.size() - 1 : 0);
                  String starredPart = isIp ? DOT_JOINER.join(parts) + ".*" : "*." + DOT_JOINER.join(parts);
                  if (this.isBlockedServerHostName(starredPart)) {
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

   private boolean isBlockedServerHostName(String server) {
      return this.blockedServers.contains(Hashing.sha1().hashBytes(server.toLowerCase().getBytes(HASH_CHARSET)).toString());
   }
}
