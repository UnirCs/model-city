#!/bin/bash

# Import Let's Encrypt certificate to AWS ACM

set -e

CERT_DIR="./letsencrypt-certs"
AWS_REGION="us-east-1"
DOMAIN="model-city.example.org"

echo "=================================================="
echo "Import Let's Encrypt Certificate to ACM"
echo "Region: $AWS_REGION"
echo "Domain: $DOMAIN"
echo "=================================================="

# Check if certificates exist
if [ ! -f "$CERT_DIR/privkey.pem" ]; then
    echo "❌ Error: Private key not found in $CERT_DIR/privkey.pem"
    echo "Please run generate-letsencrypt-cert.sh first"
    exit 1
fi

# Check for cert.pem and chain.pem (preferred)
if [ -f "$CERT_DIR/cert.pem" ] && [ -f "$CERT_DIR/chain.pem" ]; then
    CERT_FILE="$CERT_DIR/cert.pem"
    CHAIN_FILE="$CERT_DIR/chain.pem"
    echo "✓ Using cert.pem and chain.pem"
elif [ -f "$CERT_DIR/fullchain.pem" ]; then
    # Extract cert and chain from fullchain.pem
    echo "⚠ Extracting certificate and chain from fullchain.pem..."

    # The fullchain.pem contains:
    # 1. Server certificate (first cert)
    # 2. Intermediate certificates (remaining certs)

    # Extract first certificate (server cert)
    awk 'BEGIN {first=1} /-----BEGIN CERTIFICATE-----/{if(first){p=1; first=0}} /-----END CERTIFICATE-----/{if(p){print; p=0; exit}} p' \
        "$CERT_DIR/fullchain.pem" > "$CERT_DIR/cert.pem"

    # Extract remaining certificates (chain)
    awk 'BEGIN {first=1; p=0} /-----BEGIN CERTIFICATE-----/{if(!first){p=1} else {first=0}} p' \
        "$CERT_DIR/fullchain.pem" > "$CERT_DIR/chain.pem"

    CERT_FILE="$CERT_DIR/cert.pem"
    CHAIN_FILE="$CERT_DIR/chain.pem"
    echo "✓ Extracted cert.pem and chain.pem"
else
    echo "❌ Error: No certificate files found"
    echo "Expected either:"
    echo "  - cert.pem + chain.pem"
    echo "  - fullchain.pem"
    exit 1
fi

echo ""
echo "Importing certificate to ACM..."
CERT_ARN=$(aws acm import-certificate \
    --certificate fileb://"$CERT_FILE" \
    --certificate-chain fileb://"$CHAIN_FILE" \
    --private-key fileb://"$CERT_DIR/privkey.pem" \
    --region "$AWS_REGION" \
    --query 'CertificateArn' \
    --output text)

if [ -z "$CERT_ARN" ]; then
    echo "❌ Error: Failed to import certificate"
    exit 1
fi

echo ""
echo "✅ Certificate imported successfully!"
echo ""
echo "Certificate ARN:"
echo "$CERT_ARN"
echo ""
echo "=================================================="
echo "Next Steps:"
echo "=================================================="
echo ""
echo "1. Update your terraform.tfvars with the new ARN:"
echo "   acm_server_certificate_arn = \"$CERT_ARN\""
echo ""
echo "2. Update your DNS to point to your ALB:"
echo "   - Get your ALB DNS name from Terraform output or AWS Console"
echo "   - At your DNS provider, point the domain (A/CNAME record) to the ALB"
echo ""
echo "3. Apply the Terraform changes:"
echo "   cd ../terraform/aws"
echo "   terraform apply"
echo ""
echo "4. IMPORTANT: Let's Encrypt certificates expire in 90 days"
echo "   Set a reminder to renew before expiration!"
echo ""
echo "   To renew:"
echo "   sudo certbot renew"
echo "   ./import-cert-to-acm.sh  # Re-run this script with renewed cert"
echo ""


