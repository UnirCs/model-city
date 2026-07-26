#!/bin/bash

# Generate Let's Encrypt certificate for model-city.example.org
# This script uses certbot in Docker to obtain a certificate via DNS challenge

set -e

DOMAIN="model-city.example.org"
EMAIL="unir.supplies@gmail.com"  # Change if needed
CERT_DIR="./letsencrypt-certs"

echo "=================================================="
echo "Let's Encrypt Certificate Generator"
echo "Domain: $DOMAIN"
echo "=================================================="

# Create output directory
mkdir -p "$CERT_DIR"

echo ""
echo "⚠️  IMPORTANT: You need to configure DNS at your provider manually"
echo ""
echo "Steps to obtain the certificate:"
echo ""
echo "1. Install certbot (if not already installed):"
echo "   macOS: brew install certbot"
echo "   Linux: sudo apt-get install certbot"
echo ""
echo "2. Run certbot in manual DNS mode:"
echo "   sudo certbot certonly --manual --preferred-challenges dns -d $DOMAIN"
echo ""
echo "3. Certbot will ask you to create a TXT record at your DNS provider:"
echo "   - Create a TXT record for _acme-challenge.$DOMAIN"
echo "   - Set its value to the string certbot gives you"
echo "   - Wait 1-2 minutes for DNS propagation"
echo "   - Press Enter in certbot to continue"
echo ""
echo "4. Your certificates will be saved in:"
echo "   /etc/letsencrypt/live/$DOMAIN/"
echo ""
echo "5. Copy certificates to this directory:"
echo "   sudo cp /etc/letsencrypt/live/$DOMAIN/fullchain.pem $CERT_DIR/"
echo "   sudo cp /etc/letsencrypt/live/$DOMAIN/privkey.pem $CERT_DIR/"
echo "   sudo chmod 644 $CERT_DIR/*.pem"
echo ""
echo "6. Run the import script:"
echo "   ./import-cert-to-acm.sh"
echo ""
echo "=================================================="
echo ""
echo "Alternative: automate the DNS-01 challenge with acme.sh, which supports"
echo "most DNS providers via --dns plugins (see https://github.com/acmesh-official/acme.sh/wiki/dnsapi):"
echo ""
echo "curl https://get.acme.sh | sh"
echo "~/.acme.sh/acme.sh --issue --dns dns_<your_provider> -d $DOMAIN --server letsencrypt"
echo ""
echo "You'll need to export your DNS provider's API credentials first."
echo ""

