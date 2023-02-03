package de.sko.distributedlocking.config;

import de.sko.distributedlocking.MyCache;
import de.sko.distributedlocking.entities.Lock;
import de.sko.distributedlocking.repositories.LockHandlerRepository;
import de.sko.distributedlocking.repositories.LockRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;

@Configuration
public class ApplicationConfig
{
   private final MongoTemplate mongoTemplate;
   private final MongoMappingContext mongoMappingContext;

   ApplicationConfig(
      final MongoTemplate mongoTemplate,
      final MongoMappingContext mongoMappingContext )
   {
      this.mongoTemplate = mongoTemplate;
      this.mongoMappingContext = mongoMappingContext;
   }

   @Bean
   public MyCache getCache()
   {
      return new MyCache();
   }

   @Bean
   @Qualifier( "myCacheLockHandlerRepository" )
   public LockHandlerRepository getMyCacheLockHandlerRepository( LockRepository lockRepository )
   {
      return new LockHandlerRepository( lockRepository, "MY_CACHE", 30000L );
   }

   @Bean
   @Qualifier( "myCacheLockRegistry" )
   public JdbcLockRegistry getMyCacheLockRegistry(
      @Qualifier( "myCacheLockHandlerRepository" ) org.springframework.integration.jdbc.lock.LockRepository lockRepository
   )
   {
      return new JdbcLockRegistry( lockRepository );
   }

   @EventListener( ApplicationReadyEvent.class )
   public void index()
   {
      final var indexOperations = mongoTemplate.indexOps( Lock.class );
      final var indexResolver = new MongoPersistentEntityIndexResolver( mongoMappingContext );
      indexResolver.resolveIndexFor( Lock.class ).forEach( indexOperations::ensureIndex );
   }

   @Bean
   public MongoTransactionManager transactionManager( MongoDatabaseFactory mongoDatabaseFactory )
   {
      return new MongoTransactionManager( ( mongoDatabaseFactory ) );
   }
}
