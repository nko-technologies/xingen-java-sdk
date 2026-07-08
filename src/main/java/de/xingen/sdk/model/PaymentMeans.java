package de.xingen.sdk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class PaymentMeans {
    private String typeCode;
    private String paymentMeansText;
    private String remittanceInformation;
    private String creditTransferAccountId;
    private String accountName;
    private String serviceProviderId;
    private String mandateReferenceId;
    private String cardAccountNumber;
    private String cardHolderName;
    private String creditorId;
    private String debitedAccountId;
}
