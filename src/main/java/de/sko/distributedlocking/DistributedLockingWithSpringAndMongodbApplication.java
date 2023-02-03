package de.sko.distributedlocking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DistributedLockingWithSpringAndMongodbApplication
{

   public static void main( String[] args )
   {
      SpringApplication.run( DistributedLockingWithSpringAndMongodbApplication.class, args );
   }
}
