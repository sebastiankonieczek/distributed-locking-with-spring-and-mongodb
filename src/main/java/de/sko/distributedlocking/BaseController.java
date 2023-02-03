package de.sko.distributedlocking;

import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( path = "base", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE )
public class BaseController
{
   private final LockRegistry lockRegistry;
   private final MyCache cache;
   private final ReentrantLock reentrantLock = new ReentrantLock();

   BaseController(
      @Qualifier("myCacheLockRegistry") final LockRegistry lockRegistry,
      final MyCache cache )
   {
      this.lockRegistry = lockRegistry;
      this.cache = cache;
   }

   @PostMapping( "put" )
   ResponseEntity< String > changeResource( @RequestBody CacheValue cacheValue )
   {
      final var lock = lockRegistry.obtain( cacheValue.key );
      lock.lock();
      try {
         cache.put( cacheValue.key, cacheValue.value );
         try {
            Thread.sleep( 20000 );
         }
         catch( InterruptedException e ) {
            throw new RuntimeException( e );
         }
         return ResponseEntity.ok( "Success" );
      }
      finally {
         lock.unlock();
      }
   }

   @GetMapping( "read" )
   ResponseEntity< String > readResource( @RequestBody String key )
   {
      System.out.println(key);
      final var lock = lockRegistry.obtain( key );
      lock.lock();
      try {
         final var value = cache.get( key );

         try {
            Thread.sleep( 10000 );
         }
         catch( InterruptedException e ) {
            throw new RuntimeException( e );
         }

         if( value == null ) return ResponseEntity.notFound().build();

         return ResponseEntity.ok( value );
      }
      finally {
         lock.unlock();
      }
   }

   record CacheValue( String key, String value )
   {
   }
}
