package de.sko.distributedlocking.entities;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "INT_LOCK")
@CompoundIndex(def = "{'lockKey': 1, 'region': 1}", unique = true)
public record Lock
(
   @Id
   String lockKey,
   String region,
   String clientId,
   @CreatedDate
   Instant createdDate,
   @Indexed( expireAfterSeconds = 0 )
   Instant expiresAt
) {}
