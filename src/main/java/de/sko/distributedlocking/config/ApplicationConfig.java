package de.sko.distributedlocking.config;

import de.sko.distributedlocking.entities.Lock;
import de.sko.distributedlocking.entities.OTP;
import de.sko.distributedlocking.repositories.InternalMongoLockRepository;
import de.sko.distributedlocking.repositories.MongoLockRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
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
   @Qualifier( "otpLockRegistry" )
   public JdbcLockRegistry getOtpLockRegistry( InternalMongoLockRepository internalRepository )
   {
      var region = "OTP"; // only manage locks for "OTP" use case
      var timeToLiveMillis = 60000L; // free lock in shared mongo collection after 60 seconds

      var lockRepository = new MongoLockRepository( internalRepository, region, timeToLiveMillis );
      return new JdbcLockRegistry( lockRepository );
   }

   @Bean
   @Qualifier( "otpRefreshLockRegistry" )
   public JdbcLockRegistry getOtpRefreshLockRegistry( InternalMongoLockRepository internalRepository )
   {
      var region = "OTPRefresh";
      var timeToLiveMillis = 60000L;

      var lockRepository = new MongoLockRepository( internalRepository, region, timeToLiveMillis );
      return new JdbcLockRegistry( lockRepository );
   }


   @EventListener( ApplicationReadyEvent.class )
   public void index()
   {
      final var indexOperations = mongoTemplate.indexOps( Lock.class );
      final var indexResolver = new MongoPersistentEntityIndexResolver( mongoMappingContext );
      indexResolver.resolveIndexFor( Lock.class ).forEach( indexOperations::ensureIndex );
      indexResolver.resolveIndexFor( OTP.class ).forEach( indexOperations::ensureIndex );
   }
}
