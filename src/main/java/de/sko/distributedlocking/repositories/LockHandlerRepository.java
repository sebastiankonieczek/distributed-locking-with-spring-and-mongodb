package de.sko.distributedlocking.repositories;

import de.sko.distributedlocking.entities.Lock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.integration.jdbc.lock.LockRepository;

public class LockHandlerRepository
   implements LockRepository
{
   private final String clientId;
   private final de.sko.distributedlocking.repositories.LockRepository internalLockRepository;
   private final String region;
   private final Long ttlMillis;

   public LockHandlerRepository(
      final de.sko.distributedlocking.repositories.LockRepository internalLockRepository,
      final String region,
      final Long ttlMillis )
   {

      this.internalLockRepository = internalLockRepository;
      this.region = region;
      this.ttlMillis = ttlMillis;
      this.clientId = UUID.randomUUID().toString();
   }

//   @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
   @Override
   public boolean isAcquired( final String lock )
   {
      if( lock == null ) {
         return false;
      }

      return internalLockRepository.existsByRegionAndClientIdAndLockKey( region, clientId, lock );
   }

//   @Transactional(propagation = Propagation.REQUIRES_NEW)
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

//   @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
   @Override
   public boolean acquire( final String lock )
   {
      if( lock == null ) {
         return false;
      }

      try {
         final Instant creationTime = Instant.now();
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
      // not implemented
      return false;
   }

//   @Transactional(propagation = Propagation.REQUIRES_NEW)
   @Override
   public void close()
   {
      internalLockRepository.deleteAllByRegionAndClientId( region, clientId );
   }
}
