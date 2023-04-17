package de.sko.distributedlocking.entities;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document( collection = "OTP_NEXT_REFRESH" )
public class OTPNextRefresh
{
   public static final String NEXT_REFRESH = "NEXT_REFRESH";

   @Id
   private String id;
   private Instant nextRefresh;

   public OTPNextRefresh(
      final Instant nextRefresh )
   {
      this.id = NEXT_REFRESH;
      this.nextRefresh = nextRefresh;
   }

   public Instant getNextRefresh()
   {
      return nextRefresh;
   }

   public void setNextRefreshTime( final Instant nextRefresh )
   {
      this.nextRefresh = nextRefresh;
   }

   /**
    * For internal use of springframework
    */
   void setId( String id )
   {
      this.id = id;
   }
}
