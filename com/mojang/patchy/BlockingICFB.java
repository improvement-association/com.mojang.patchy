package com.mojang.patchy;

import java.util.Hashtable;
import java.util.function.Predicate;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;

public class BlockingICFB implements InitialContextFactoryBuilder {
   private final Predicate<String> blockList;

   public BlockingICFB(Predicate<String> blockList) {
      this.blockList = blockList;
   }

   public static void install() {
      try {
         System.getProperties().setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
         NamingManager.setInitialContextFactoryBuilder(new BlockingICFB(BlockedServers::isBlockedServer));
      } catch (Throwable var1) {
         System.out.println("Block failed :(");
         var1.printStackTrace();
      }

   }

   public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> env) throws NamingException {
      String className = (String)env.get("java.naming.factory.initial");

      try {
         InitialContextFactory original = (InitialContextFactory)Class.forName(className).newInstance();
         return (InitialContextFactory)("com.sun.jndi.dns.DnsContextFactory".equals(className) ? new BlockingICF(this.blockList, original) : original);
      } catch (Exception var5) {
         NoInitialContextException ne = new NoInitialContextException("Cannot instantiate class: " + className);
         ne.setRootCause(var5);
         throw ne;
      }
   }
}
