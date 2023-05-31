package de.sko.distributedlocking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import de.sko.distributedlocking.repositories.InternalMongoLockRepository;
import de.sko.distributedlocking.repositories.MongoLockRepository;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest( classes = { DistributedLockingWithSpringAndMongodbApplication.class } )
public class LockRepositoryTest
{
   @Container
   private static final MongoDBContainer mongoDBContainer = new MongoDBContainer( "mongo:5.0.14" );

   @DynamicPropertySource
   static void registerMySQLProperties( DynamicPropertyRegistry registry )
   {
      final var conn = mongoDBContainer.getConnectionString() + "/distributed-locks";
      registry.add( "spring.data.mongodb.uri", () -> conn );
   }

   @Autowired
   @Qualifier( "otpLockRegistry" )
   private JdbcLockRegistry otpLockRegistry;

   @Autowired
   private InternalMongoLockRepository internalMongoLockRepository;

   @Test
   void test_that_distributed_locking_guarantees_access_order_in_same_client()
   {
      // this test simulates one processes accessing the same resource from two different threads
      test_distributed_locking_guarantees_order( otpLockRegistry, otpLockRegistry );
   }

   @Test
   void test_that_distributed_locking_guarantees_access_order_with_different_clients()
   {
      // this test simulates two processes accessing the same resource
      final var testRegion = "TEST_REGION";
      final var ttlMillis = 60000L;

      // simulate two clients by creating two lock registries for the same region
      final var lockRepositoryClientA =
         new JdbcLockRegistry( new MongoLockRepository( internalMongoLockRepository, testRegion, ttlMillis ) );
      final var lockRepositoryClientB =
         new JdbcLockRegistry( new MongoLockRepository( internalMongoLockRepository, testRegion, ttlMillis ) );

      test_distributed_locking_guarantees_order(
         lockRepositoryClientA,
         lockRepositoryClientB );
   }

   @NotNull
   private void test_distributed_locking_guarantees_order(
      final JdbcLockRegistry lockRepositoryClientA,
      final JdbcLockRegistry lockRepositoryClientB )
   {
      final var lockKey = "test";

      final var testDummy = new Dummy();
      testDummy.setValue( "" );
      final var countDownLatch = new CountDownLatch( 1 );

      // the first task sets the value of the dummy to one but reaches lock only when countDownLatch is 0
      final var taskOne = CompletableFuture.runAsync( () -> {
         final var lock = lockRepositoryClientA.obtain( lockKey );
         try {
            countDownLatch.await();
         }
         catch( InterruptedException e ) {
            throw new RuntimeException( e );
         }

         lock.lock();
         try {
            testDummy.setValue( "1" );
         }
         finally {
            lock.unlock();
         }
      } );

      // the second task sets the value of teh dummy to two and decrements countDownLatch to 0 after acquiring the lock
      final var taskTwo = CompletableFuture.runAsync( () -> {
         final var lock = lockRepositoryClientB.obtain( lockKey );
         lock.lock();
         countDownLatch.countDown();
         try {
            Thread.sleep( 500 ); // wait a bit to be sure task one reaches lock
         }
         catch( InterruptedException e ) {
            throw new RuntimeException( e );
         }
         try {
            testDummy.setValue( "2" );

         }
         finally {
            lock.unlock();
         }
      } );

      try {
         CompletableFuture.allOf( taskOne, taskTwo ).get( 1000, TimeUnit.MILLISECONDS );
      }
      catch( InterruptedException | ExecutionException | TimeoutException e ) {
         throw new RuntimeException( e );
      }

      // after waiting for all tasks to complete the value must be 1
      // this is because task one reached lock after task two already acquired the lock
      assertThat( testDummy.getValue() ).isEqualTo( "1" );
   }

   @Test
   void test_that_lock_is_reentrant()
   {
      final var task = CompletableFuture.runAsync( () -> {
         final var obtained = otpLockRegistry.obtain( "test" );
         obtained.lock();
         try {
            if( obtained.tryLock( 1000, TimeUnit.MILLISECONDS ) ) {
               obtained.unlock();
            }
         }
         catch( InterruptedException e ) {
            throw new RuntimeException( e );
         }
         finally {
            obtained.unlock();
         }
      } );

      try {
         task.get( 100, TimeUnit.MILLISECONDS );
      }
      catch( InterruptedException | ExecutionException | TimeoutException e ) {
         task.join(); //
         fail( "Deadlock!" );
      }
   }

   static class Dummy
   {
      private String value;

      private String getValue()
      {
         return value;
      }

      private void setValue( final String value )
      {
         this.value = value;
      }
   }
}
