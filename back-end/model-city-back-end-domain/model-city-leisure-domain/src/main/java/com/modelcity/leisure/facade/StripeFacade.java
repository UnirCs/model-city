package com.modelcity.leisure.facade;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

/** Wraps the Stripe API calls needed to support paid events (web Checkout Session flow). */
@Slf4j
@Component
public class StripeFacade {

    /**
     * Creates a Stripe Product + one-time Price for the given event.
     *
     * @param eventName display name used as the Stripe product name
     * @param amount    price in major currency units (e.g. 25.00 for €25)
     * @param currency  ISO 4217 currency code (e.g. "EUR")
     * @return the Stripe Price ID (e.g. {@code price_xxx})
     */
    public String createPrice(String eventName, BigDecimal amount, String currency) {
        try {
            Product product = Product.create(
                    ProductCreateParams.builder()
                            .setName(eventName)
                            .build());
            log.debug("Stripe product created id={} name={}", product.getId(), eventName);

            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
            Price price = Price.create(
                    PriceCreateParams.builder()
                            .setProduct(product.getId())
                            .setUnitAmount(amountInCents)
                            .setCurrency(currency.toLowerCase())
                            .build());
            log.info("Stripe price created id={} product={} amount={} {}",
                    price.getId(), product.getId(), amountInCents, currency);
            return price.getId();
        } catch (StripeException ex) {
            log.error("Stripe error while creating price for '{}': {}", eventName, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not create Stripe price: " + ex.getMessage());
        }
    }

    /**
     * Issues a full refund for the payment associated with a Checkout Session (web flow).
     *
     * @param checkoutSessionId Stripe Checkout Session ID stored on the ticket
     * @param amount            original amount paid in major currency units
     * @param currency          ISO 4217 currency code (for logging only)
     */
    public void refund(String checkoutSessionId, BigDecimal amount, String currency) {
        try {
            Session session = Session.retrieve(checkoutSessionId);
            String paymentIntentId = session.getPaymentIntent();
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
            Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(amountInCents)
                    .build());
            log.info("Stripe refund issued checkoutSession={} paymentIntent={} amount={} {}",
                    checkoutSessionId, paymentIntentId, amountInCents, currency);
        } catch (StripeException ex) {
            log.error("Stripe error while refunding checkoutSession={}: {}", checkoutSessionId, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not issue Stripe refund: " + ex.getMessage());
        }
    }

}
