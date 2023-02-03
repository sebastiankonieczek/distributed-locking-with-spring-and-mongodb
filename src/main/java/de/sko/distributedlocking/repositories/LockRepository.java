package de.sko.distributedlocking.repositories;

import de.sko.distributedlocking.entities.Lock;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LockRepository
   extends MongoRepository< Lock, String >
{
   void deleteAllByRegionAndClientId(
      String region,
      String clientId );

   void deleteByRegionAndClientIdAndLockKey(
      String region,
      String clientId,
      String lockKey );

   boolean existsByRegionAndClientIdAndLockKey(
      String region,
      String clientId,
      String lockKey );
}
