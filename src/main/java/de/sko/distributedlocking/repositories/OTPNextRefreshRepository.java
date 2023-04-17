package de.sko.distributedlocking.repositories;

import de.sko.distributedlocking.entities.OTPNextRefresh;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface OTPNextRefreshRepository extends MongoRepository< OTPNextRefresh, String >
{
   @Query("{'_id':  '" + OTPNextRefresh.NEXT_REFRESH + "'}")
   Optional<OTPNextRefresh> find();
}
