package com.mojang.patchy;

import java.util.Hashtable;
import java.util.function.Predicate;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.spi.InitialContextFactory;

public class BlockingICF implements InitialContextFactory {
   private final Predicate<String> blockList;
   private final InitialContextFactory parent;

   public BlockingICF(Predicate<String> blockList, InitialContextFactory parent) {
      this.blockList = blockList;
      this.parent = parent;
   }

   public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
      return new BlockingDC(this.blockList, (DirContext)this.parent.getInitialContext(environment));
   }
}
