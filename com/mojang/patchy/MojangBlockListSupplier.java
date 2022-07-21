package com.mojang.patchy;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.mojang.blocklist.BlockListSupplier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.function.Predicate;
import javax.annotation.Nullable;

@AutoService({BlockListSupplier.class})
public class MojangBlockListSupplier implements BlockListSupplier {
   @Nullable
   public Predicate<String> createBlockList() {
      try {
         URLConnection urlConnection = new URL("https://sessionserver.mojang.com/blockedservers").openConnection();
         InputStream is = urlConnection.getInputStream();
         Throwable var3 = null;

         BlockedServers var5;
         try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, BlockedServers.HASH_CHARSET));
            var5 = new BlockedServers((Collection<String>)reader.lines().collect(ImmutableSet.toImmutableSet()));
         } catch (Throwable var15) {
            var3 = var15;
            throw var15;
         } finally {
            if (is != null) {
               if (var3 != null) {
                  try {
                     is.close();
                  } catch (Throwable var14) {
                     var3.addSuppressed(var14);
                  }
               } else {
                  is.close();
               }
            }

         }

         return var5;
      } catch (IOException var17) {
         return null;
      }
   }
}
