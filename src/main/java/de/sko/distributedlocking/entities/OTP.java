package de.sko.distributedlocking.entities;

import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document( collection = "OTP" )
public class OTP
{
      @Id
      final String clientId;
      final String otp;
      boolean isValid;
      @Indexed( expireAfterSeconds = 60 )
      final Instant createdAt;

   public OTP(
      final String clientId,
      final String otp,
      final boolean isValid,
      final Instant createdAt )
   {
      this.clientId = clientId;
      this.otp = otp;
      this.isValid = isValid;
      this.createdAt = createdAt;
   }

   public String getClientId()
   {
      return clientId;
   }

   public String getOtp()
   {
      return otp;
   }

   public boolean isValid()
   {
      return isValid;
   }

   public void setValid( final boolean valid )
   {
      isValid = valid;
   }

   @Override
   public boolean equals( final Object o )
   {
      if( this == o ) {
         return true;
      }
      if( o == null || getClass() != o.getClass() ) {
         return false;
      }
      final OTP otp1 = (OTP)o;
      return isValid == otp1.isValid
             && Objects.equals( clientId, otp1.clientId )
             && Objects.equals( otp, otp1.otp );
   }

   @Override
   public int hashCode()
   {
      return Objects.hash( clientId, otp, isValid );
   }
}
