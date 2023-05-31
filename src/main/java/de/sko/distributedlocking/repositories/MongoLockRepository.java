package de.sko.distributedlocking.repositories;

import de.sko.distributedlocking.entities.Lock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.integration.jdbc.lock.LockRepository;

public class MongoLockRepository
   implements LockRepository
{
   private final String clientId;
   private final InternalMongoLockRepository internalLockRepository;
   private final String region;
   private final Long ttlMillis;

   public MongoLockRepository(
      final InternalMongoLockRepository internalInternalMongoLockRepository,
      final String region,
      final Long ttlMillis )
   {

      this.internalLockRepository = internalInternalMongoLockRepository;
      this.region = region;
      this.ttlMillis = ttlMillis;
      this.clientId = UUID.randomUUID().toString();
   }

   @Override
   public boolean isAcquired( final String lock )
   {
      if( lock == null ) {
         return false;
      }

      return internalLockRepository.existsByRegionAndClientIdAndLockKey( region, clientId, lock );
   }

   @Override
   public void delete( final String lock )
   {
      if( lock == null ) {
         return;
      }

      internalLockRepository.deleteByRegionAndClientIdAndLockKey( region, clientId, lock );
   }

   @Override
   public void deleteExpired()
   {
      // not implemented
   }

   @Override
   public boolean acquire( final String lock )
   {
      if( lock == null ) {
         return false;
      }

      try {
         final Instant creationTime = Instant.now();
         if( renew( lock ) ) {
            return true;
         }

         internalLockRepository.insert( new Lock( lock, region, clientId, creationTime, creationTime.plusMillis( ttlMillis ) ) );
         return true;
      }
      catch( DuplicateKeyException duplicateKeyException ) {
         return false;
      }
   }

   @Override
   public boolean renew( final String lock )
   {
      if( isAcquired( lock ) ) {
         // update internal lock
         final Instant creationTime = Instant.now();
         try {
            internalLockRepository.save( new Lock( lock, region, clientId, creationTime, creationTime.plusMillis( ttlMillis ) ) );
         }
         catch( DuplicateKeyException duplicateKeyException ) {
            return false;
         }

         return true;
      }

      return false;
   }

   @Override
   public void close()
   {
      internalLockRepository.deleteAllByRegionAndClientId( region, clientId );
   }
}
