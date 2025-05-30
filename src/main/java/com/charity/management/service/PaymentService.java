package com.charity.management.service;

import com.charity.management.entity.Donation;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public PaymentService(@Value("${stripe.api.key:sk_test_51NXXXXXXXXXXXXXXXXXXXXXXx}") String stripeApiKey) {
        this.stripeApiKey = stripeApiKey;
        Stripe.apiKey = this.stripeApiKey;
    }

    public String createPaymentIntent(Donation donation) throws StripeException {
        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = donation.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();
        
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amountInCents);
        params.put("currency", "usd");
        params.put("description", "Donation to " + donation.getCharityAction().getTitle());
        params.put("payment_method_types", java.util.Arrays.asList("card"));
        
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.getClientSecret();
    }

    public Charge processPayment(String token, Donation donation) throws StripeException {
        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = donation.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();
        
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", amountInCents);
        chargeParams.put("currency", "usd");
        chargeParams.put("description", "Donation to " + donation.getCharityAction().getTitle());
        chargeParams.put("source", token);
        
        return Charge.create(chargeParams);
    }

    // Simulate payment processing for testing purposes
    public String simulatePaymentProcessing(Donation donation) {
        // Generate a fake transaction ID
        String transactionId = "sim_" + System.currentTimeMillis();
        return transactionId;
    }
}
